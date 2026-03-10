package com.clothshop.client.dtos.response;

import lombok.*;

/**
 * Response DTO cho danh sách sản phẩm (Client/Public side).
 * Dùng chung cho: Trang chủ, Danh mục, Tìm kiếm và Bộ sưu tập.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {

    // --- Nhóm 1: Các trường cũ (Giữ nguyên để tránh lỗi các trang đang chạy) ---
    private Long productId;
    private String productName;
    private String productSlug;
    private String categoryName;
    private Double price;       // Thường là giá base hoặc giá hiển thị mặc định
    private String imageUrl;    // Ảnh đại diện cũ
    private Boolean available;  // Trạng thái còn hàng (true/false)

    // --- Nhóm 2: Các trường bổ sung cho Collection & SEO ---
    private Long id;            // Alias của productId (Dùng cho các logic JS dùng .id)
    private String name;        // Alias của productName
    private String slug;        // Alias của productSlug (Chuẩn SEO)
    private String thumbnail;   // Alias của imageUrl (Dùng cho giao diện Card mới)

    // --- Nhóm 3: Thông tin tính toán (Dành cho hiển thị giá Range hoặc Stock) ---
    private Double originalPrice; // Giá gốc (chưa giảm hoặc giá niêm yết)
    private Double minPrice;      // Giá thấp nhất trong các phiên bản (Variants)
    private Integer totalStock;   // Tổng số lượng tồn kho của tất cả các size/màu
    private Integer discountPercent; // % Giảm giá (Nếu sau này em làm tính năng khuyến mãi)
}