package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

@Data
@Builder
public class EventShortDto {
    private long id;
    private String annotation;
    private CategoryDto category;
    private int confirmedRequests;
    private String eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private long views;
}
