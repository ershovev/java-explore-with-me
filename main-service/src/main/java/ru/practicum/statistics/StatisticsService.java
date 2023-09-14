package ru.practicum.statistics;

import ru.practicum.event.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    List<Event> findAndSetViewsToEvents(List<Event> eventList, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd);

    List<Long> getPopularFilteredEvents(List<Long> eventsIds, int from, int size, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd);

    void addEndpointHit(HttpServletRequest request);
}
