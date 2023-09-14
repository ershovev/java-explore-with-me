package ru.practicum.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.DateTimeException;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<ViewStats> viewStatsList = new ArrayList<>();

        if (end.isBefore(start)) {
            throw new DateTimeException("Дата и время начала не может быть позже даты и времени конца");
        }

        if (uris != null) {
            if (unique) {
                viewStatsList = statsRepository.getViewStatsByUrisUnique(start, end, uris);
            } else {
                viewStatsList = statsRepository.getViewStatsByUris(start, end, uris);
            }
        } else {
            if (unique) {
                viewStatsList = statsRepository.getViewStatsUnique(start, end);
            } else {
                viewStatsList = statsRepository.getViewStats(start, end);
            }
        }

        if (!viewStatsList.isEmpty()) {
            return viewStatsList.stream().map(StatsMapper::toViewStatsDto).collect(Collectors.toList());
        } else {
            return new ArrayList<ViewStatsDto>();
        }
    }

    @Override
    @Transactional
    public EndpointHitDto add(EndpointHitDto endpointHitDto) {
        EndpointHit endPointHitToSave = StatsMapper.toEndPointHit(endpointHitDto);
        EndpointHit savedEndPointHit = statsRepository.save(endPointHitToSave);

        return StatsMapper.toEndpointHitDto(savedEndPointHit);
    }
}
