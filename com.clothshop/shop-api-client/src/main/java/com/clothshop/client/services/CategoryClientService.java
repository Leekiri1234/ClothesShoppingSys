package com.clothshop.client.services;

import com.clothshop.client.dtos.response.CategoryResponse;
import com.clothshop.domain.repositories.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryClientService {
    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findAllByIsActiveTrue().stream()
                .map(cat -> {
                    CategoryResponse res = new CategoryResponse();
                    res.setId(cat.getId());
                    res.setName(cat.getCategoryName());
                    res.setSlug(cat.getCategorySlug());
                    return res;
                })
                .collect(Collectors.toList());
    }
}