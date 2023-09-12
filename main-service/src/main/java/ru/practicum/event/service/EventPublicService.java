package ru.practicum.event.service;

import ru.practicum.enums.EventSort;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventPublicService {
    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, int from, int size,
                                  HttpServletRequest request);

    EventFullDto getEventById(long eventId, HttpServletRequest request);
}
