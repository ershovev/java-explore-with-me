package ru.practicum.participationrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.participationrequest.dto.ParticipationRequestDto;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
@Slf4j
public class ParticipationRequestController {
    private final ParticipationRequestService participationRequestService;

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsOfUser(@PathVariable long userId) {
        log.info("получен запрос на получение запросов на участие в событиях от пользователя с  id = " + userId);

        return participationRequestService.getRequestsOfUser(userId);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable long userId, @RequestParam long eventId) {
        log.info("получен запрос на создание запроса на участие в событии с id = " + eventId + " от пользователя с id = " + userId);

        return participationRequestService.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable long userId, @PathVariable long requestId) {
        log.info("получен запрос на отмену запроса c id = " + requestId + "от пользователя с id = " + userId);

        return participationRequestService.cancelParticipationRequest(userId, requestId);
    }
}
