package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryMapper;
import ru.practicum.category.CategoryRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.exception.CategoryNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryPublicServiceImpl implements CategoryPublicService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of((from / size), size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        return CategoryMapper.toCategoryDtoList(categories);
    }

    @Override
    public CategoryDto getCategory(long catId) {
        Category category = findCategoryById(catId);

        return CategoryMapper.toCategoryDto(category);
    }

    private Category findCategoryById(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
    }
}