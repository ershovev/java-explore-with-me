package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.enums.State;
import ru.practicum.enums.StateAction;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventFullCommentDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventSearchParams;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.eventAdminComment.EventAdminComment;
import ru.practicum.event.eventAdminComment.EventAdminCommentDto;
import ru.practicum.event.eventAdminComment.EventAdminCommentMapper;
import ru.practicum.event.eventAdminComment.EventAdminCommentRepository;
import ru.practicum.exception.*;
import ru.practicum.location.Location;
import ru.practicum.location.LocationRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventAdminServiceImpl implements EventAdminService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final EventAdminCommentRepository eventAdminCommentRepository;

    @Override
    public List<EventFullDto> getEvents(EventSearchParams params) {
        if (params.getUsers() != null) {
            List<User> userList = userRepository.findAllByIdIn(params.getUsers());
            if (userList.size() != params.getUsers().size()) {
                throw new UserNotFoundException("Пользователь/пользователи не найдены");
            }
        }

        if (params.getCategories() != null) {
            List<Category> categoryList = categoryRepository.findAllByIdIn(params.getCategories());
            if (categoryList.size() != params.getCategories().size()) {
                throw new CategoryNotFoundException("Категория/категории не найдены");
            }
        }

        if (params.getRangeStart() == null) {
            params.setRangeStart(LocalDateTime.now().minusYears(100));
        }

        if (params.getRangeEnd() == null) {
            params.setRangeEnd(LocalDateTime.now().plusYears(100));
        }

        Pageable pageable = PageRequest.of((params.getFrom() / params.getSize()), params.getSize());

        return EventMapper.toEventFullDtoList(eventRepository.findAllByParams(params.getUsers(), params.getStates(),
                params.getCategories(), params.getRangeStart(), params.getRangeEnd(), pageable));
    }

    @Override
    @Transactional
    public EventFullCommentDto updateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = findEvent(eventId);
        validateStateAction(updateEventAdminRequest.getStateAction());
        updateEventDate(event, updateEventAdminRequest.getEventDate());
        updateLocation(event, updateEventAdminRequest.getLocation());
        updateStateAction(event, updateEventAdminRequest.getStateAction());

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getCategory() != null) {
            Category category = findCategory(updateEventAdminRequest.getCategory());
            event.setCategory(category);
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }

        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        if (updateEventAdminRequest.getAdminComment() != null) {
            eventAdminCommentRepository.save(EventAdminCommentMapper
                    .toEventAdminComment(updateEventAdminRequest.getAdminComment(), event));
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Обновлено событие: {}", updatedEvent.toString());

        EventFullCommentDto eventFullCommentDto = EventMapper.toEventFullCommentDto(updatedEvent);
        eventFullCommentDto.setAdminComments(findAdminCommentsToEvent(eventId));

        return eventFullCommentDto;
    }

    private Event findEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
    }

    private Category findCategory(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
    }

    private void validateStateAction(StateAction stateAction) {
        if ((stateAction != null) && (stateAction != StateAction.PUBLISH_EVENT
                && stateAction != StateAction.REJECT_EVENT && stateAction != StateAction.SEND_TO_REVISION)) {
            throw new EventStateActionException("StateAction может принимать значения: " + StateAction.PUBLISH_EVENT +
                    ", " + StateAction.REJECT_EVENT + ", " + StateAction.SEND_TO_REVISION);
        }
    }

    private void updateEventDate(Event event, LocalDateTime evenDate) {
        if (evenDate != null) {
            if (evenDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new EventDateException("Время начала события не может быть ранее чем через 2 часа от текущего");
            }
            event.setEventDate(evenDate);
        }
    }

    private void updateLocation(Event event, Location location) {
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

    private void updateStateAction(Event event, StateAction stateAction) {
        if (stateAction != null) {
            if (stateAction.equals(StateAction.REJECT_EVENT)
                    && event.getState().equals(State.PUBLISHED)) {
                throw new EventStateException("Нельзя отклонить уже опубликованное событие");
            } else if (!event.getState().equals(State.PENDING)
                    && (stateAction.equals(StateAction.PUBLISH_EVENT) || stateAction.equals(StateAction.REJECT_EVENT))) {
                throw new EventStateException("Чтобы опубликовать или отменить событие оно должно быть в статусе PENDING");
            }

            if (stateAction.equals(StateAction.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            } else if (stateAction.equals(StateAction.PUBLISH_EVENT)) {
                event.setState(State.PUBLISHED);
            } else if (stateAction.equals(StateAction.SEND_TO_REVISION)) {
                event.setState(State.REVISION);
            }
        }
    }

    private List<EventAdminCommentDto> findAdminCommentsToEvent(long eventId) {
        List<EventAdminComment> commentList = eventAdminCommentRepository.findAllByEventId(eventId);

        return EventAdminCommentMapper.toEventAdminCommentDtoList(commentList);
    }
}