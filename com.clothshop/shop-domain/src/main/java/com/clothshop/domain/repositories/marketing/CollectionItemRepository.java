package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {

    @Query("SELECT MAX(ci.displayOrder) FROM CollectionItem ci WHERE ci.collection.id = :collectionId AND ci.isActive = true")
    Optional<Integer> findMaxDisplayOrderByCollectionId(@Param("collectionId") Long collectionId);

    @Query("SELECT ci.product.id FROM CollectionItem ci WHERE ci.collection.id = :collectionId AND ci.isActive = true")
    List<Long> findProductIdsByCollectionId(@Param("collectionId") Long collectionId);

    @Query("SELECT COUNT(ci) FROM CollectionItem ci WHERE ci.collection.id = :collectionId AND ci.isActive = true")
    Long countActiveItemsByCollectionId(@Param("collectionId") Long collectionId);

    @Modifying
    @Query("UPDATE CollectionItem ci SET ci.isActive = false WHERE ci.collection.id = :collectionId")
    void deactivateAllItemsByCollectionId(@Param("collectionId") Long collectionId);

    @Query("SELECT DISTINCT ci FROM CollectionItem ci " +
            "JOIN FETCH ci.product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE ci.collection.id = :collectionId AND ci.isActive = true " +
            "ORDER BY ci.displayOrder ASC")
    List<CollectionItem> findActiveItemsWithProductByCollectionId(@Param("collectionId") Long collectionId);

    // VINH LẬP CHÙA FIX: Dùng Native Query để "xuyên thủng" @SQLRestriction.
    // Dùng IN (:productIds) để kéo toàn bộ lịch sử gán sản phẩm lên RAM trong 1 câu SQL duy nhất (Chống N+1)
    @Query(value = "SELECT * FROM collection_items WHERE collection_id = :collectionId AND product_id IN :productIds", nativeQuery = true)
    List<CollectionItem> findAllHistoryByCollectionIdAndProductIds(
            @Param("collectionId") Long collectionId,
            @Param("productIds") List<Long> productIds);
}