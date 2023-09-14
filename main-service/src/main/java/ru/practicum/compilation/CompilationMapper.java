package ru.practicum.compilation;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;

import java.util.List;
import java.util.stream.Collectors;

public class CompilationMapper {
    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .events(EventMapper.toEventShortDtoList(compilation.getEvents()))
                .title(compilation.getTitle())
                .build();
    }

    public static List<CompilationDto> toCompilationDtoList(List<Compilation> compilationList) {
        return compilationList.stream()
                .map(compilation -> CompilationDto.builder()
                        .id(compilation.getId())
                        .pinned(compilation.getPinned())
                        .events(EventMapper.toEventShortDtoList(compilation.getEvents()))
                        .title(compilation.getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    public static Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events) {
        Boolean isPinned = false;

        if (newCompilationDto.getPinned() != null) {
            isPinned = newCompilationDto.getPinned();
        }

        return Compilation.builder()
                .pinned(isPinned)
                .title(newCompilationDto.getTitle())
                .events(events)
                .build();
    }
}
