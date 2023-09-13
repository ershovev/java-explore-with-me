package ru.practicum.event.eventAdminComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAdminCommentDto {
    private String created;
    private String description;
}
