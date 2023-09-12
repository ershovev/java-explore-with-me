package ru.practicum.participationrequest;

import ru.practicum.participationrequest.dto.EventRequestStatusUpdateResult;
import ru.practicum.participationrequest.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.MainDateTimeFormatter.mainDateTimeFormatter;

public class ParticipationRequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(Optional.ofNullable(request.getCreated())
                        .map(dateTime -> dateTime.format(mainDateTimeFormatter))
                        .orElse(null))
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public static List<ParticipationRequestDto> toParticipationRequestDtoList(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    public static EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
            List<ParticipationRequest> confirmedRequests, List<ParticipationRequest> rejectedRequests) {

        List<ParticipationRequestDto> confirmedRequestsDtoList = confirmedRequests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto).collect(Collectors.toList());

        List<ParticipationRequestDto> rejectedRequestsDtoList = rejectedRequests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto).collect(Collectors.toList());

        return new EventRequestStatusUpdateResult(confirmedRequestsDtoList, rejectedRequestsDtoList);
    }
}
