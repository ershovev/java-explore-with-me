package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.State;
import ru.practicum.enums.StateAction;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.exception.*;
import ru.practicum.location.Location;
import ru.practicum.location.LocationRepository;
import ru.practicum.participationrequest.ParticipationRequest;
import ru.practicum.participationrequest.ParticipationRequestMapper;
import ru.practicum.participationrequest.ParticipationRequestRepository;
import ru.practicum.participationrequest.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participationrequest.dto.EventRequestStatusUpdateResult;
import ru.practicum.participationrequest.dto.ParticipationRequestDto;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventPrivateServiceImpl implements EventPrivateService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRequestRepository participationRequestRepository;

    @Override
    public List<EventShortDto> getEventsOfUser(long userId, int from, int size) {
        findUserById(userId);

        Pageable pageable = PageRequest.of((from / size), size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        return EventMapper.toEventShortDtoList(events);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        User user = findUserById(userId);

        Category category = findCategoryById(newEventDto.getCategory());

        if (!newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException("Событие не может иметь начало ранее чем через 2 два часа от текущего времени");
        }

        Location location = locationRepository.save(newEventDto.getLocation());
        Event savedEvent = eventRepository.save(EventMapper.toEvent(newEventDto, user, category, location));

        log.info("Добавлено событие: {}", savedEvent.toString());

        return EventMapper.toEventFullDto(savedEvent);
    }

    @Override
    public EventFullDto getFullEventOfUser(long userId, long eventId) {
        findUserById(userId);
        Event event = findEventById(eventId);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        findUserById(userId);
        Event event = findEventById(eventId);
        validateStateForEventUpdate(event.getState());
        updateEventState(event, updateEventUserRequest.getStateAction());
        setEventDateForUpdate(event, updateEventUserRequest.getEventDate());
        setLocationForUpdate(event, updateEventUserRequest.getLocation());

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            Category category = findCategoryById(updateEventUserRequest.getCategory());
            event.setCategory(category);
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Обновлено событие: {}", updatedEvent.toString());

        return EventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipationRequests(long userId, long eventId) {
        findUserById(userId);
        Event event = findEventById(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new EventAccessException("Пользователь не инициатор события");
        }

        List<ParticipationRequest> participationRequestList = participationRequestRepository.findAllByEvent(event);

        return ParticipationRequestMapper.toParticipationRequestDtoList(participationRequestList);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(long userId, long eventId,
                                                              EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        findUserById(userId);
        Event event = findEventById(eventId);

        validateModerationAndParticipantLimit(event);
        validateRequestIds(eventRequestStatusUpdateRequest.getRequestIds(), eventId);
        List<Long> requestIds = eventRequestStatusUpdateRequest.getRequestIds();
        RequestStatus status = eventRequestStatusUpdateRequest.getStatus();

        List<ParticipationRequest> confirmedRequests;
        List<ParticipationRequest> rejectedRequests;

        if (status == RequestStatus.CONFIRMED) {
            confirmedRequests = confirmRequests(requestIds, event);
            rejectedRequests = rejectRemainingRequests(requestIds);
        } else if (status == RequestStatus.REJECTED) {
            confirmedRequests = Collections.emptyList();
            rejectedRequests = rejectRequests(requestIds);
        } else {
            throw new RequestStatusException("Некорректный статус заявки");
        }

        List<ParticipationRequest> savedConfirmedRequests = participationRequestRepository.saveAll(confirmedRequests);
        List<ParticipationRequest> savedRejectedRequests = participationRequestRepository.saveAll(rejectedRequests);
        eventRepository.save(event);

        return ParticipationRequestMapper.toEventRequestStatusUpdateResult(savedConfirmedRequests, savedRejectedRequests);
    }

    private Event findEventById(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
    }

    private Category findCategoryById(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    private void validateStateForEventUpdate(State state) {
        if (state != State.PENDING && state != State.CANCELED) {
            throw new EventUpdateException("Изменить можно только отмененные события или события в состоянии ожидания модерации ");
        }
    }

    private void updateEventState(Event event, StateAction stateAction) {
        if (stateAction != null) {
            if (stateAction == StateAction.SEND_TO_REVIEW) {
                event.setState(State.PENDING);
            } else if (stateAction == StateAction.CANCEL_REVIEW) {
                event.setState(State.CANCELED);
            } else {
                throw new EventStateException("StateAction может принимать значения: SEND_TO_REVIEW или CANCEL_REVIEW");
            }
        }
    }

    private void setEventDateForUpdate(Event event, LocalDateTime eventDate) {
        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new EventDateException("Время начала события не может быть ранее чем через 2 часа от текущего");
            }
            event.setEventDate(eventDate);
        }
    }

    private void setLocationForUpdate(Event event, Location location) {
        if (location != null) {
            float lat = location.getLat();
            float lon = location.getLon();
            Location foundLocation = locationRepository.findByLatAndLon(lat, lon);
            if (foundLocation == null) {
                location = locationRepository.save(Location.builder().lat(lat).lon(lon).build());
            }
            event.setLocation(location);
        }
    }

    private void validateModerationAndParticipantLimit(Event event) {
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new EventModerationException("Подтверждение заявок для события не требуется");
        }

        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            throw new EventFullParticipantLimit("Достигнут лимит участников события");
        }
    }

    private void validateRequestIds(List<Long> requestIds, long eventId) {
        List<ParticipationRequest> requestsList = participationRequestRepository.findAllByIdIn(requestIds);

        for (ParticipationRequest request : requestsList) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new RequestStatusException("Статус заявки с id " + request.getId() + " не является ожидающим");
            }
            if (request.getEvent().getId() != eventId) {
                throw new RequestNotBelongToEventException("Заявка с id " + request.getId()
                        + " не относится к событию с id " + eventId);
            }
        }
    }

    private List<ParticipationRequest> confirmRequests(List<Long> requestIds, Event event) {
        long availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();
        long numOfConfirmedRequests = Math.min(requestIds.size(), availableSlots);

        List<ParticipationRequest> requestsList = participationRequestRepository.findAllByIdIn(requestIds);

        List<ParticipationRequest> requestsToConfirm = requestsList.stream()
                .filter(request -> request.getStatus() != RequestStatus.CONFIRMED)
                .limit(numOfConfirmedRequests)
                .peek(request -> request.setStatus(RequestStatus.CONFIRMED))
                .collect(Collectors.toList());

        event.setConfirmedRequests(event.getConfirmedRequests() + requestsToConfirm.size());

        return requestsToConfirm;
    }

    private List<ParticipationRequest> rejectRemainingRequests(List<Long> requestIds) {
        List<ParticipationRequest> requestsList = participationRequestRepository.findAllByIdIn(requestIds);

        List<ParticipationRequest> requestsToReject = requestsList.stream()
                .filter(request -> !request.getStatus().equals(RequestStatus.CONFIRMED))
                .peek(request -> request.setStatus(RequestStatus.REJECTED))
                .collect(Collectors.toList());

        return requestsToReject;
    }

    private List<ParticipationRequest> rejectRequests(List<Long> requestIds) {
        List<ParticipationRequest> requestsList = participationRequestRepository.findAllByIdIn(requestIds);

        List<ParticipationRequest> requestsToReject = requestsList.stream()
                .filter(request -> request.getStatus() != RequestStatus.REJECTED)
                .peek(request -> request.setStatus(RequestStatus.REJECTED))
                .collect(Collectors.toList());

        return requestsToReject;
    }
}
