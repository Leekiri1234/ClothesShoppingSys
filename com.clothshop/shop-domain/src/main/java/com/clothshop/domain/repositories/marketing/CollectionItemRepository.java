package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {

    List<CollectionItem> findByCollectionIdOrderByDisplayOrderAsc(Long collectionId);

    List<CollectionItem> findByProductId(Long productId);

    void deleteByCollectionIdAndProductId(Long collectionId, Long productId);
}
