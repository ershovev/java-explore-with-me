package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;

import java.util.List;

public interface CompilationPublicService {
    List<CompilationDto> findAll(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(long compId);
}
