package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    // 1. TÌM KIẾM (TRANG LIST ADMIN): Chỉ lấy thông tin cơ bản
    @Query("SELECT c FROM Collection c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Collection> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // 2. TÌM CHI TIẾT (TRANG CLIENT): Dùng LOWER để tránh lỗi không tìm thấy do chữ hoa/thường
    // Dùng EntityGraph để nạp sẵn items và product, tránh LazyInitializationException
    @EntityGraph(attributePaths = {"items", "items.product", "items.product.category", "items.product.images", "items.product.variants"})
    @Query("SELECT c FROM Collection c WHERE LOWER(c.slug) = LOWER(:slug) AND c.isActive = true")
    Optional<Collection> findBySlugAndIsActiveTrue(@Param("slug") String slug);;

    // 3. CÁC HÀM KIỂM TRA (VALIDATION)
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);

    // 4. TRANG CHỦ & MENU
    List<Collection> findByIsActiveTrue();

    @Query("SELECT c FROM Collection c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<Collection> findTop4ByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
}