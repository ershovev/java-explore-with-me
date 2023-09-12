package ru.practicum.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.event.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {
    private final StatsClient statsClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<Event> findAndSetViewsToEvents(List<Event> eventList, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd) {
        String start = rangeStart.format(formatter);
        String end = rangeEnd.format(formatter);
        Boolean unique = true;
        List<String> uris = new ArrayList<>();

        for (Event event : eventList) {
            uris.add("/events/" + event.getId());
        }

        ResponseEntity<Object> response = statsClient.getStats(start, end, uris, unique);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<ViewStatsDto> viewStatsDtoList = new ArrayList<>();
            List<HashMap<String, Object>> responseList = (List<HashMap<String, Object>>) response.getBody();

            for (HashMap<String, Object> responseItem : responseList) {
                ViewStatsDto viewStatsDto = new ViewStatsDto();
                viewStatsDto.setUri((String) responseItem.get("uri"));
                viewStatsDto.setHits(((Integer) responseItem.get("hits")).longValue());
                viewStatsDtoList.add(viewStatsDto);
            }

            Map<Long, Long> hitsMap = new HashMap<>();

            for (ViewStatsDto viewStatsDto : viewStatsDtoList) {
                String uri = viewStatsDto.getUri();
                Long hits = viewStatsDto.getHits();
                long eventId = extractEventIdFromUri(uri);
                hitsMap.put(eventId, hits);
            }

            for (Event event : eventList) {
                Long eventId = event.getId();
                if (hitsMap.containsKey(eventId)) {
                    Long hits = hitsMap.get(eventId);
                    event.setViews(hits);
                } else {
                    event.setViews(0);
                }
            }
        }

        return eventList;
    }

    @Override
    public List<Long> getPopularFilteredEvents(List<Long> eventsIds, int from, int size, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd) {
        String start = rangeStart.format(formatter);
        String end = rangeEnd.format(formatter);
        Boolean unique = true;
        List<String> uris = new ArrayList<>();
        Set<Long> filteredEventsIds = new HashSet<>();
        int index = 0;

        for (Long eventId : eventsIds) {
            uris.add("/events/" + eventId);
        }

        ResponseEntity<Object> response = statsClient.getStats(start, end, uris, unique);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<ViewStatsDto> viewStatsDtoList = new ArrayList<>();
            List<HashMap<String, Object>> responseList = (List<HashMap<String, Object>>) response.getBody();

            for (HashMap<String, Object> responseItem : responseList) {
                ViewStatsDto viewStatsDto = new ViewStatsDto();
                viewStatsDto.setUri((String) responseItem.get("uri"));
                viewStatsDto.setHits(((Integer) responseItem.get("hits")).longValue());
                viewStatsDtoList.add(viewStatsDto);
            }

            if (viewStatsDtoList != null) {
                viewStatsDtoList.sort(Comparator.comparingLong(ViewStatsDto::getHits));
                List<ViewStatsDto> filteredViewStatsDtoList = viewStatsDtoList.stream()
                        .skip(from)
                        .limit(size)
                        .collect(Collectors.toList());

                for (ViewStatsDto viewStatsDto : filteredViewStatsDtoList) {
                    filteredEventsIds.add(extractEventIdFromUri(viewStatsDto.getUri()));
                }
            }
        }

        while (filteredEventsIds.size() < size && index < eventsIds.size()) {
            filteredEventsIds.add(eventsIds.get(index));
            index++;
        }

        return new ArrayList<Long>(filteredEventsIds);
    }

    @Override
    @Transactional
    public void addEndpointHit(HttpServletRequest request) {
        String app = "ewm-main-service";
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        EndpointHitDto endpointHitDto = EndpointHitDto.builder().app(app).uri(uri).ip(ip)
                .timestamp(LocalDateTime.now().format(formatter)).build();

        statsClient.add(endpointHitDto);
    }

    private long extractEventIdFromUri(String uri) {
        String eventIdString = uri.substring(uri.lastIndexOf("/") + 1);
        return Long.parseLong(eventIdString);
    }
}