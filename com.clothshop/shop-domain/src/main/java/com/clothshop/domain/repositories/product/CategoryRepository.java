package com.clothshop.domain.repositories.product;

import com.clothshop.domain.models.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 1. Lấy tất cả danh mục đang hoạt động (Dùng cho Sidebar/Menu)
    List<Category> findAllByIsActiveTrue();

    // 2. Lấy danh mục gốc đang hoạt động (Build Menu chính)
    List<Category> findByParentIsNullAndIsActiveTrue();

    // 3. Tìm theo Slug và phải đang hoạt động (Dùng cho trang Category Detail)
    Optional<Category> findByCategorySlugAndIsActiveTrue(String slug);

    // 4. Tìm các danh mục con đang hoạt động
    List<Category> findByParentIdAndIsActiveTrue(Long parentId);

    // 5. Query lấy danh mục kèm theo số lượng sản phẩm (Nếu em muốn hiện số lượng bên cạnh tên ở Sidebar)
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p " +
            "WHERE c.isActive = true GROUP BY c")
    List<Object[]> findAllWithProductCount();

    Optional<Category> findByCategorySlug(String slug);
    List<Category> findByParentIsNull();
    boolean existsByCategorySlug(String slug);
    List<Category> findByParentId(Long parentId);

    @Query(value = "SELECT * FROM categories", nativeQuery = true)
    List<Category> findAllIncludingInactive();

    /**
     * Bypass @SQLRestriction — tìm category theo ID kể cả đã bị soft-delete.
     * Dùng cho thao tác toggle-status (restore) trên các danh mục đã ẩn.
     */
    @Query(value = "SELECT * FROM categories WHERE category_id = :id", nativeQuery = true)
    Optional<Category> findByIdIncludingInactive(@Param("id") Long id);

    /**
     * Bypass @SQLRestriction — lấy tất cả children kể cả đã bị ẩn.
     * Dùng khi cascade restore để recover toàn bộ cây danh mục.
     */
    @Query(value = "SELECT * FROM categories WHERE parent_id = :parentId", nativeQuery = true)
    List<Category> findChildrenByParentIdIncludingInactive(@Param("parentId") Long parentId);
}
