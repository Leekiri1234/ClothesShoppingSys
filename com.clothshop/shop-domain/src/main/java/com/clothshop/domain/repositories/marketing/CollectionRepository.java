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

    // Tìm kiếm Collection theo tên có phân trang (Dùng cho ô Search ở màn hình List)
    // Dùng LOWER để tìm kiếm không phân biệt hoa thường, kết hợp CONCAT để hỗ trợ LIKE an toàn
    @Query("SELECT c FROM Collection c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Collection> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // Kiểm tra trùng lặp tên khi Create (Chống lỗi Unique Constraint từ database)
    boolean existsByName(String name);

    // Kiểm tra trùng lặp tên khi Update (Bỏ qua chính bản thân Collection đang sửa)
    boolean existsByNameAndIdNot(String name, Long id);

    // Kiểm tra trùng lặp slug khi Create
    boolean existsBySlug(String slug);

    // Kiểm tra trùng lặp slug khi Update (Bỏ qua chính Collection đang sửa)
    boolean existsBySlugAndIdNot(String slug, Long id);

    // Tìm collection theo slug (fallback cho các slug cũ không có ID)

    List<Collection> findByIsActiveTrue();

    @Query("SELECT DISTINCT c FROM Collection c " +
            "LEFT JOIN FETCH c.items ci " +
            "LEFT JOIN FETCH ci.product p " +
            "LEFT JOIN FETCH p.category cat " +
            "WHERE c.slug = :slug AND c.isActive = true")
    Optional<Collection> findBySlugAndIsActiveTrue(@Param("slug") String slug);

    @Query("SELECT DISTINCT c FROM Collection c " +
            "WHERE c.isActive = true " +
            "ORDER BY c.createdAt DESC")
    List<Collection> findTop4ByIsActiveTrueOrderByCreatedAtDesc();
}