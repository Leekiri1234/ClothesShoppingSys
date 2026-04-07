package com.clothshop.client.services;

import com.clothshop.client.dtos.response.CategoryResponse;
import com.clothshop.client.mappers.CategoryClientMapper;
import com.clothshop.domain.repositories.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    public List<CategoryResponse> getCategoryTree() {
        List<CategoryResponse> allCategories = getAllActiveCategories();

        // Group by parentId
        Map<Long, List<CategoryResponse>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryResponse::getParentId));

        // Assign children and filter root categories
        return allCategories.stream()
                .peek(c -> {
                    List<CategoryResponse> children = childrenMap.getOrDefault(c.getId(), new ArrayList<>());
                    c.setChildren(children);
                })
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
    }
}