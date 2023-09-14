package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventSearchParams;
import ru.practicum.event.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventPublicService {
    List<EventShortDto> getEvents(EventSearchParams params,
                                  HttpServletRequest request);

    EventFullDto getEventById(long eventId, HttpServletRequest request);
}
