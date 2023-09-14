package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;

import java.util.List;

public interface CategoryPublicService {
    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(long catId);
}
