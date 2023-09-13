package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.participationrequest.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participationrequest.dto.EventRequestStatusUpdateResult;
import ru.practicum.participationrequest.dto.ParticipationRequestDto;

import java.util.List;

public interface EventPrivateService {
    List<EventShortDto> getEventsOfUser(long userId, int from, int size);

    EventFullDto addEvent(long userId, NewEventDto newEventDto);

    EventFullCommentDto getFullEventOfUser(long userId, long eventId);

    EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getEventParticipationRequests(long userId, long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(long userId, long eventId,
                                                       EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
