package ru.practicum.participationrequest;

import ru.practicum.participationrequest.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getRequestsOfUser(long userId);

    ParticipationRequestDto addParticipationRequest(long userId, long eventId);

    ParticipationRequestDto cancelParticipationRequest(long userId, long requestId);
}
