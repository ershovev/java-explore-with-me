package ru.practicum.event;

import ru.practicum.category.Category;
import ru.practicum.category.CategoryMapper;
import ru.practicum.enums.State;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.location.Location;
import ru.practicum.user.User;
import ru.practicum.user.UserMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.MainDateTimeFormatter.mainDateTimeFormatter;

public class EventMapper {
    public static Event toEvent(NewEventDto newEventDto, User user, Category category, Location location) {
        if (newEventDto.getPaid() == null) {
            newEventDto.setPaid(false);
        }

        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(Long.valueOf(0));
        }

        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }


        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(location)
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .views(0)
                .initiator(user)
                .createdOn(LocalDateTime.now())
                .state(State.PENDING)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        if (event.getPaid() == null) {
            event.setPaid(false);
        }

        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(Long.valueOf(0));
        }

        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        return EventShortDto.builder()
                .id(event.getId())
                .views(event.getViews())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .eventDate(Optional.ofNullable(event.getEventDate())
                        .map(dateTime -> dateTime.format(mainDateTimeFormatter))
                        .orElse(null))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public static List<EventShortDto> toEventShortDtoList(List<Event> eventList) {
        return eventList.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public static EventFullDto toEventFullDto(Event event) {
        //    if (event.getPublishedOn() != null) {
        //        publishedOn = event.getPublishedOn().toString();
        //    }

        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .paid(event.getPaid())
                .views(event.getViews())
                .eventDate(Optional.ofNullable(event.getEventDate())
                        .map(dateTime -> dateTime.format(mainDateTimeFormatter))
                        .orElse(null))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .state(event.getState().toString())
                .createdOn(Optional.ofNullable(event.getCreatedOn())
                        .map(dateTime -> dateTime.format(mainDateTimeFormatter))
                        .orElse(null))
                .location(event.getLocation())
                .requestModeration(event.getRequestModeration())
                .publishedOn(Optional.ofNullable(event.getPublishedOn())
                        .map(dateTime -> dateTime.format(mainDateTimeFormatter))
                        .orElse(null))
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public static List<EventFullDto> toEventFullDtoList(List<Event> eventList) {
        return eventList.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }
}
