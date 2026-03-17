package com.clothshop.client.services;

import com.clothshop.client.dtos.response.CollectionResponse;
import com.clothshop.client.mappers.CollectionClientMapper;
import com.clothshop.domain.entities.marketing.Collection;
import com.clothshop.domain.repositories.marketing.CollectionRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final CollectionClientMapper collectionClientMapper;

    public List<CollectionResponse> getAllActiveCollections() {
        return collectionRepository.findByIsActiveTrue().stream()
                .map(collectionClientMapper::toCollectionResponse) // Gọn hơn rất nhiều
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionResponse getCollectionBySlug(String slug) {
        Collection collection = collectionRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        return collectionClientMapper.toCollectionResponse(collection);
    }
}
