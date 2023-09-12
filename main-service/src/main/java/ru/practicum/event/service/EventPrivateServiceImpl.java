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
import java.util.ArrayList;
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
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Pageable pageable = PageRequest.of((from / size), size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        return EventMapper.toEventShortDtoList(events);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));

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
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        if (event.getState() != State.PENDING && event.getState() != State.CANCELED) {
            throw new EventUpdateException("Изменить можно только отмененные события или события в состоянии ожидания модерации ");
        }

        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction() == StateAction.SEND_TO_REVIEW) {
                event.setState(State.PENDING);
            } else if (updateEventUserRequest.getStateAction() == StateAction.CANCEL_REVIEW) {
                event.setState(State.CANCELED);
            } else {
                throw new EventStateException("StateAction может принимать значения: SEND_TO_REVIEW или CANCEL_REVIEW");
            }
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new EventDateException("Время начала события не может быть ранее чем через 2 часа от текущего");
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.getLocation() != null) {
            float lat = updateEventUserRequest.getLocation().getLat();
            float lon = updateEventUserRequest.getLocation().getLon();
            Location location = locationRepository.findByLatAndLon(lat, lon);
            if (location == null) {
                location = locationRepository.save(Location.builder().lat(lat).lon(lon).build());
            }
            event.setLocation(location);
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
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
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
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        if (event.getParticipantLimit() == 0 || event.getRequestModeration().equals(false)) {
            throw new EventModerationException("Подтверждение заявок для события не требуется");
        }
        if (event.getParticipantLimit() == event.getConfirmedRequests() &&
                eventRequestStatusUpdateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new EventFullParticipantLimit("Достигнут лимит участников события");
        }

        List<ParticipationRequest> requestsList = participationRequestRepository
                .findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        for (ParticipationRequest request : requestsList) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new RequestStatusException("Статус заявки с id " + request.getId() + " не является ожидающим");
            }
            if (request.getEvent().getId() != eventId) {
                throw new RequestNotBelongToEventException("Заявка с id " + request.getId()
                        + " не относится к событию с id " + eventId);
            }
        }

        List<Long> requestIds = eventRequestStatusUpdateRequest.getRequestIds();
        RequestStatus status = eventRequestStatusUpdateRequest.getStatus();
        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        if (status == RequestStatus.CONFIRMED) {
            long availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();
            long numOfConfirmedRequests = Math.min(requestIds.size(), availableSlots);

            List<ParticipationRequest> requestsToConfirm = requestsList.stream()
                    .filter(request -> requestIds.contains(request.getId()) && request.getStatus() != RequestStatus.CONFIRMED)
                    .limit(numOfConfirmedRequests)
                    .collect(Collectors.toList());

            requestsToConfirm.forEach(request -> {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
            });

            event.setConfirmedRequests(event.getConfirmedRequests() + requestsToConfirm.size());

            List<ParticipationRequest> requestsToReject = requestsList.stream()
                    .filter(request -> !requestsToConfirm.contains(request))
                    .collect(Collectors.toList());

            requestsToReject.forEach(request -> {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            });

        } else if (status == RequestStatus.REJECTED) {
            List<ParticipationRequest> requestsToReject = requestsList.stream()
                    .filter(request -> requestIds.contains(request.getId()) && request.getStatus() != RequestStatus.REJECTED)
                    .collect(Collectors.toList());

            requestsToReject.forEach(request -> {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            });
        }

        List<ParticipationRequest> savedConfirmedRequests = participationRequestRepository.saveAll(confirmedRequests);
        List<ParticipationRequest> savedRejectedRequests = participationRequestRepository.saveAll(rejectedRequests);
        eventRepository.save(event);

        return ParticipationRequestMapper.toEventRequestStatusUpdateResult(savedConfirmedRequests, savedRejectedRequests);
    }
}
