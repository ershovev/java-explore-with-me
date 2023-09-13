package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventPrivateService;
import ru.practicum.participationrequest.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participationrequest.dto.EventRequestStatusUpdateResult;
import ru.practicum.participationrequest.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
@Slf4j
@Validated
public class EventControllerPrivate {
    private final EventPrivateService eventPrivateService;

    @GetMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsOfUser(@PathVariable long userId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("получен запрос на получение собственных событий пользователем с id = " + userId);

        return eventPrivateService.getEventsOfUser(userId, from, size);
    }

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("получен запрос на добавление события от пользователя с id = " + userId);

        return eventPrivateService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullCommentDto getFullEventOfUser(@PathVariable long userId, @PathVariable long eventId) {
        log.info("получен запрос на получение полной информации по событию с id = " + eventId
                + " от пользователя с id = " + userId);

        return eventPrivateService.getFullEventOfUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateUserEvent(@PathVariable long userId, @PathVariable long eventId,
                                        @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("получен запрос на обновление события с id = " + eventId + " от пользователя c id = " + userId);

        return eventPrivateService.updateUserEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventParticipationRequests(@PathVariable long userId, @PathVariable long eventId) {
        log.info("получен запрос на получение информации о запросах на участие в событии с id = "
                + eventId + " пользователя c id = " + userId);

        return eventPrivateService.getEventParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable long userId, @PathVariable long eventId,
                                                              @RequestBody @Valid
                                                              EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("получен запрос на изменение статуса заявки на участие в событии с id = " + userId
                + " от пользователя с id = " + userId);

        return eventPrivateService.changeRequestStatus(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
