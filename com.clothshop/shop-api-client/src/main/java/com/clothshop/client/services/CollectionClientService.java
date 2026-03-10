package com.clothshop.client.services;

import com.clothshop.client.dtos.response.CollectionResponse;
import com.clothshop.client.mappers.CollectionMapper;
import com.clothshop.domain.repositories.marketing.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionClientService {
    private final CollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;

    public List<CollectionResponse> getAllActiveCollections() {
        return collectionRepository.findByIsActiveTrue().stream()
                .map(collectionMapper::toCollectionResponse) // Gọn hơn rất nhiều
                .collect(Collectors.toList());
    }

    public CollectionResponse getCollectionBySlug(String slug) {
        return collectionRepository.findBySlugAndIsActiveTrue(slug)
                .map(collectionMapper::toCollectionResponse)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
    }
}
