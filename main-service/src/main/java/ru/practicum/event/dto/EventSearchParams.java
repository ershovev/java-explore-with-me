package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.State;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class EventSearchParams {
    private List<Long> users;
    private List<State> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private int from;
    private int size;
    private String text;
    private Boolean paid;
    private Boolean onlyAvailable;
    private EventSort sort;
}
