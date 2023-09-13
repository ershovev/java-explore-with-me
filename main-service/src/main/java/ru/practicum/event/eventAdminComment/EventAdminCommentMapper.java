package ru.practicum.event.eventAdminComment;

import ru.practicum.event.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.MainDateTimeFormatter.mainDateTimeFormatter;

public class EventAdminCommentMapper {
    public static EventAdminComment toEventAdminComment(String text, Event event) {
        return EventAdminComment.builder()
                .created(LocalDateTime.now())
                .description(text)
                .event(event)
                .build();
    }

    public static EventAdminCommentDto toEventAdminCommentDto(EventAdminComment eventAdminComment) {
        return EventAdminCommentDto.builder()
                .created(Optional.ofNullable(eventAdminComment.getCreated())
                        .map(dateTime -> dateTime.format(mainDateTimeFormatter))
                        .orElse(null))
                .description(eventAdminComment.getDescription())
                .build();
    }

    public static List<EventAdminCommentDto> toEventAdminCommentDtoList(List<EventAdminComment> commentList) {
        return commentList.stream()
                .map(EventAdminCommentMapper::toEventAdminCommentDto)
                .collect(Collectors.toList());
    }
}
