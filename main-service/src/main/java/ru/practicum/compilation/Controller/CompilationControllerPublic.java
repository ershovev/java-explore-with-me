package ru.practicum.compilation.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationPublicService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/compilations")
@Slf4j
@Validated
public class CompilationControllerPublic {
    private final CompilationPublicService compilationPublicService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> findAll(@RequestParam(required = false) Boolean pinned,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("получен запрос на получение подборок событий");

        return compilationPublicService.findAll(pinned, from, size);
    }

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto findById(@PathVariable long compId) {
        log.info("получен запрос на получение подборки событий с id = " + compId);

        return compilationPublicService.getCompilationById(compId);
    }
}
