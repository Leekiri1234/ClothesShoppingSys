package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.FeaturedCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeaturedCollectionRepository extends JpaRepository<FeaturedCollection, Long> {

    // Tối ưu N+1 Query bằng JOIN FETCH.
    // Lấy danh sách Collection nổi bật đang active, sắp xếp theo thứ tự hiển thị.
    // Kéo luôn cả thông tin bảng cha (collection) lên RAM trong 1 câu SQL duy nhất.
    @Query("SELECT fc FROM FeaturedCollection fc JOIN FETCH fc.collection c WHERE fc.isActive = true AND c.isActive = true ORDER BY fc.displayOrder ASC")
    List<FeaturedCollection> findAllActiveFeaturedCollections();

    // Lấy thứ tự lớn nhất hiện tại để auto-increment khi thêm mới
    @Query("SELECT MAX(fc.displayOrder) FROM FeaturedCollection fc WHERE fc.isActive = true")
    Optional<Integer> findMaxDisplayOrder();

    // Kiểm tra xem Collection này đã được set làm Featured chưa (Chống trùng lặp)
    boolean existsByCollectionIdAndIsActiveTrue(Long collectionId);

    // Xóa mềm một FeaturedCollection thông qua Collection ID
    @Modifying
    @Query("UPDATE FeaturedCollection fc SET fc.isActive = false WHERE fc.collection.id = :collectionId")
    void deactivateByCollectionId(@Param("collectionId") Long collectionId);
}