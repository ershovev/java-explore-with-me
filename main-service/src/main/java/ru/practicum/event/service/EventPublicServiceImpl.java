package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.State;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventSearchParams;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exception.CategoryDoesntExistException;
import ru.practicum.exception.EventNotFoundException;
import ru.practicum.statistics.StatisticsService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPublicServiceImpl implements EventPublicService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final StatisticsService statisticsService;

    @Override
    public List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request) {

        Pageable pageable;
        List<Event> eventList = new ArrayList<>();

        if (params.getCategories() != null) {
            List<Category> categoryList = categoryRepository.findAllByIdIn(params.getCategories());
            if (categoryList.size() != params.getCategories().size()) {
                throw new CategoryDoesntExistException("Категория/категории не найдены");
            }
        }

        if (params.getRangeStart() == null) {
            params.setRangeStart(LocalDateTime.now());
        }

        if (params.getRangeEnd() == null) {
            params.setRangeEnd(LocalDateTime.now().plusYears(100));
        }

        if (params.getSort() == EventSort.EVENT_DATE) {
            pageable = PageRequest.of((params.getFrom() / params.getSize()), params.getSize(), Sort.by("eventDate").ascending());
            eventList = eventRepository.findPublishedEventsByParams(params.getText(), params.getCategories(), params.getPaid(),
                    params.getRangeStart(), params.getRangeEnd(), params.getOnlyAvailable(), pageable);
        } else {
            List<Long> allEventsIds = eventRepository.findAllPublishedEventsIdsByParams(params.getText(), params.getCategories(),
                    params.getPaid(), params.getRangeStart(), params.getRangeEnd(), params.getOnlyAvailable());
            List<Long> filteredEventsIds = statisticsService.getPopularFilteredEvents(allEventsIds, params.getFrom(),
                    params.getSize(), LocalDateTime.now().minusYears(100), LocalDateTime.now().plusYears(100));
            eventList = eventRepository.findAllByIdIn(filteredEventsIds);
            eventList.sort(Comparator.comparing(Event::getViews, Comparator.reverseOrder()));
        }

        eventList = statisticsService.findAndSetViewsToEvents(eventList, LocalDateTime.now().minusYears(100), LocalDateTime.now().plusYears(100));
        statisticsService.addEndpointHit(request);

        return EventMapper.toEventShortDtoList(eventList);
    }

    @Override
    public EventFullDto getEventById(long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new EventNotFoundException("Событие не найдено");
        }

        List<Event> eventList = new ArrayList<>();
        eventList = statisticsService.findAndSetViewsToEvents(List.of(event), LocalDateTime.now().minusYears(100), LocalDateTime.now().plusYears(100));
        statisticsService.addEndpointHit(request);

        return EventMapper.toEventFullDto(eventList.get(0));
    }
}
