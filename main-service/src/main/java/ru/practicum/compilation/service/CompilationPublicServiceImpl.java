package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.Compilation;
import ru.practicum.compilation.CompilationMapper;
import ru.practicum.compilation.CompilationRepository;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.exception.CompilationNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationPublicServiceImpl implements CompilationPublicService {
    private final CompilationRepository compilationRepository;

    @Override
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of((from / size), size);
        Page<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable);
        }

        List<Compilation> compilationsList = compilations.getContent();

        return CompilationMapper.toCompilationDtoList(compilationsList);
    }

    @Override
    public CompilationDto getCompilationById(long compId) {
        Compilation compilation = findCompilationById(compId);

        return CompilationMapper.toCompilationDto(compilation);
    }

    private Compilation findCompilationById(long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("подборка событий не найдена"));
    }
}
