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
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
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
    public List<EventFullDto> getEvents(List<Long> users, List<State> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        if (users != null) {
            List<User> userList = userRepository.findAllByIdIn(users);
            if (userList.size() != users.size()) {
                throw new UserNotFoundException("Пользователь/пользователи не найдены");
            }
        }

        if (categories != null) {
            List<Category> categoryList = categoryRepository.findAllByIdIn(categories);
            if (categoryList.size() != categories.size()) {
                throw new CategoryNotFoundException("Категория/категории не найдены");
            }
        }

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(100);
        }

        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        Pageable pageable = PageRequest.of((from / size), size);

        return EventMapper.toEventFullDtoList(eventRepository.findAllByParams(users, states, categories,
                rangeStart, rangeEnd, pageable));
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        if ((updateEventAdminRequest.getStateAction() != null) && (updateEventAdminRequest.getStateAction() != StateAction.PUBLISH_EVENT
                && updateEventAdminRequest.getStateAction() != StateAction.REJECT_EVENT)) {
            throw new EventStateActionException("StateAction может принимать значения: PUBLISH_EVENT или REJECT_EVENT");
        }

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new EventDateException("Время начала события не может быть ранее чем через 2 часа от текущего");
            }
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.getLocation() != null) {
            float lat = updateEventAdminRequest.getLocation().getLat();
            float lon = updateEventAdminRequest.getLocation().getLon();
            Location location = locationRepository.findByLatAndLon(lat, lon);
            if (location == null) {
                location = locationRepository.save(Location.builder().lat(lat).lon(lon).build());
            }
            event.setLocation(location);
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

        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals(StateAction.REJECT_EVENT)
                    && event.getState().equals(State.PUBLISHED)) {
                throw new EventStateException("Нельзя отклонить уже опубликованное событие");
            } else if (!event.getState().equals(State.PENDING)
                    && updateEventAdminRequest.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
                throw new EventStateException("Чтобы опубликовать событие оно должно быть в статусе PENDING");
            }
            if (updateEventAdminRequest.getStateAction().equals(StateAction.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            } else if (updateEventAdminRequest.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
                event.setState(State.PUBLISHED);
            }
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

        return EventMapper.toEventFullDto(updatedEvent);
    }
}