package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Tìm theo Slug (Dùng cho đường dẫn URL: /category/ao-so-mi)
    Optional<Category> findByCategorySlug(String slug);

    // Lấy danh sách các danh mục gốc (không có parent) để build Menu chính
    List<Category> findByParentIsNull();

    // Kiểm tra slug đã tồn tại chưa trước khi tạo mới
    boolean existsByCategorySlug(String slug);

    // Tìm các danh mục con của một danh mục cụ thể
    List<Category> findByParentId(Long parentId);
}