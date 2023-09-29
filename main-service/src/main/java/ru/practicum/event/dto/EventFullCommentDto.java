package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.eventAdminComment.EventAdminCommentDto;
import ru.practicum.location.Location;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@Data
@Builder
public class EventFullCommentDto {
    private long id;
    private String annotation;
    private CategoryDto category;
    private long confirmedRequests;
    private String createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    private String state;
    private String title;
    private long views;
    private List<EventAdminCommentDto> adminComments;
}
