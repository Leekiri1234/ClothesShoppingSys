package com.clothshop.client.services;

import com.clothshop.client.dtos.response.CategoryResponse;
import com.clothshop.client.mappers.CategoryMapper;
import com.clothshop.domain.repositories.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryClientService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getAllActiveCategories() {
        return categoryMapper.toCategoryResponseList(
                categoryRepository.findAllByIsActiveTrue()
        );
    }
}