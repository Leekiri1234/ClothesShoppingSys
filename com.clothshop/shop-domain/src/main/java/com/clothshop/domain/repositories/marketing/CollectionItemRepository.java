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

    // Lấy thứ tự lớn nhất hiện tại để cộng dồn khi gán thêm sản phẩm
    @Query("SELECT MAX(ci.displayOrder) FROM CollectionItem ci WHERE ci.collection.id = :collectionId AND ci.isActive = true")
    Optional<Integer> findMaxDisplayOrderByCollectionId(@Param("collectionId") Long collectionId);

    // Lấy danh sách Product ID đã tồn tại trong Collection để chống lưu trùng (tiết kiệm bộ nhớ so với lấy cả list object)
    @Query("SELECT ci.product.id FROM CollectionItem ci WHERE ci.collection.id = :collectionId AND ci.isActive = true")
    List<Long> findProductIdsByCollectionId(@Param("collectionId") Long collectionId);

    // Truy vấn đếm số lượng sản phẩm đang có trong collection (Dùng cho List API để tránh quá tải RAM)
    @Query("SELECT COUNT(ci) FROM CollectionItem ci WHERE ci.collection.id = :collectionId AND ci.isActive = true")
    Long countActiveItemsByCollectionId(@Param("collectionId") Long collectionId);

    // Phục vụ Soft Delete: Ẩn tất cả sản phẩm khỏi bộ sưu tập bằng 1 câu lệnh UPDATE duy nhất
    @Modifying
    @Query("UPDATE CollectionItem ci SET ci.isActive = false WHERE ci.collection.id = :collectionId")
    void deactivateAllItemsByCollectionId(@Param("collectionId") Long collectionId);
}