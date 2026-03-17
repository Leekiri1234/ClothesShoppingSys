package com.clothshop.client.services;

import com.clothshop.client.dtos.response.CategoryResponse;
import com.clothshop.client.mappers.CategoryClientMapper;
import com.clothshop.domain.repositories.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryClientService {
    private final CategoryRepository categoryRepository;
    private final CategoryClientMapper categoryClientMapper;

    public List<CategoryResponse> getAllActiveCategories() {
        return categoryClientMapper.toCategoryResponseList(
                categoryRepository.findAllByIsActiveTrue()
        );
    }
}