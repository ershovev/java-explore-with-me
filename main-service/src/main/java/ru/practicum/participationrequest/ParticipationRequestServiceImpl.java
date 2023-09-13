package ru.practicum.participationrequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.State;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.*;
import ru.practicum.participationrequest.dto.ParticipationRequestDto;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsOfUser(long userId) {
        findUser(userId);
        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);

        return ParticipationRequestMapper.toParticipationRequestDtoList(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(long userId, long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        RequestStatus requestStatus;

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId) != null) {
            throw new RequestSecondAttemptException("нельзя добавить повторный запрос");
        }

        if (event.getInitiator().getId() == userId) {
            throw new RequestSelfAttemptException("инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new RequestNotPublishedEventException("нельзя участвовать в неопубликованном событии");
        }

        if (event.getConfirmedRequests() == event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new RequestFullOccupiedException("у события достигнут лимит запросов на участие");
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            requestStatus = RequestStatus.CONFIRMED;
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        } else {
            requestStatus = RequestStatus.PENDING;
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(requestStatus)
                .build();

        return ParticipationRequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(long userId, long requestId) {
        findUser(userId);
        ParticipationRequest request = findParticipationRequest(requestId);

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.CANCELED);

        return ParticipationRequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    private Event findEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
    }

    private ParticipationRequest findParticipationRequest(long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ParticipationRequestNotFoundException("Запрос на участие в событии не найден"));
    }
}
