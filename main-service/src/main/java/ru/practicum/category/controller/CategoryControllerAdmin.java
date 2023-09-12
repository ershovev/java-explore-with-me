package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryAdminService;
import ru.practicum.exception.IntegrityConstraintViolation;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/admin/categories")
@Slf4j
@Validated
public class CategoryControllerAdmin {
    private final CategoryAdminService categoryAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("получен запрос на добавление категории: " + newCategoryDto.getName());

        try {
            return categoryAdminService.addCategory(newCategoryDto);
        } catch (DataIntegrityViolationException e) {
            throw new IntegrityConstraintViolation(e.getMessage());
        }
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long catId) {
        log.info("получен запрос на удалении категории c id = " + catId);

        try {
            categoryAdminService.deleteCategory(catId);
        } catch (DataIntegrityViolationException e) {
            throw new IntegrityConstraintViolation(e.getMessage());
        }
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable long catId, @RequestBody @Valid CategoryDto categoryDto) {
        log.info("получен запрос на обновление категории с id = " + catId);

        try {
            return categoryAdminService.updateCategory(catId, categoryDto);
        } catch (DataIntegrityViolationException e) {
            throw new IntegrityConstraintViolation(e.getMessage());
        }
    }
}
