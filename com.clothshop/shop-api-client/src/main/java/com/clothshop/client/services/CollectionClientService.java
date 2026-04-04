package com.clothshop.client.services;

import com.clothshop.client.dtos.response.CollectionResponse;
import com.clothshop.client.mappers.CollectionClientMapper;
import com.clothshop.domain.entities.marketing.Collection;
import com.clothshop.domain.repositories.marketing.CollectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CollectionClientService {
    private final CollectionRepository collectionRepository;
    private final CollectionClientMapper collectionClientMapper;

    public List<CollectionResponse> getAllActiveCollections() {
        return collectionRepository.findByIsActiveTrue().stream()
                .map(collectionClientMapper::toCollectionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionResponse getCollectionBySlug(String slug) {
        // Log để kiểm tra giá trị thực tế truyền từ URL
        log.info("Request nhận được slug: [{}]", slug);

        // Chặn lỗi nếu frontend gửi chuỗi "undefined" do sai sót JavaScript
        if (slug == null || "undefined".equalsIgnoreCase(slug)) {
            throw new EntityNotFoundException("Lỗi hệ thống: Slug bị undefined từ giao diện");
        }

        Collection collection = collectionRepository.findBySlugAndIsActiveTrue(slug.toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("Collection not found: " + slug));

        return collectionClientMapper.toCollectionResponse(collection);
    }
}