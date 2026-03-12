package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.Category;
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
}