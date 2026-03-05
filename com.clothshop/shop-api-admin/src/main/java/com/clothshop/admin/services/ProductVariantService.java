package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.products.VariantCreateRequest;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.common.utils.SlugUtils;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.entities.product.ProductVariant;
import com.clothshop.domain.repositories.product.ProductRepository;
import com.clothshop.domain.repositories.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ProductVariantService - Quản lý biến thể sản phẩm.
 * Logic tạo SKU tự động và ghi log kho ngay khi khởi tạo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final InventoryLogService inventoryLogService;

    /**
     * Thêm biến thể mới cho sản phẩm.
     * @param request Thông tin biến thể
     * @param username Người thực hiện
     */
    @Transactional
    public void addVariant(VariantCreateRequest request, String username) {
        log.info("Adding new variant for product ID: {}, color: {}, size: {}",
                request.getProductId(), request.getColor(), request.getSizeValue());

        // 1. Kiểm tra sản phẩm cha có tồn tại không
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy sản phẩm cha"));

        // 2. Tạo SKU chuẩn hóa
        String colorSlug = SlugUtils.makeSlug(request.getColor())
                .replace("-", "")
                .toUpperCase();

        String sizeSlug = SlugUtils.makeSlug(request.getSizeValue())
                .replace("-", "")
                .toUpperCase();

        String generatedSku = String.format("PROD_%d_COL%s_SZ%s", product.getId(), colorSlug, sizeSlug);
        // 3. Kiểm tra trùng lặp SKU (Quan trọng: Tránh lỗi Unique Constraint DB)
        if (variantRepository.existsBySku(generatedSku)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Biến thể với SKU " + generatedSku + " đã tồn tại");
        }

        // 4. Tạo Entity và Map dữ liệu
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(generatedSku)
                .color(request.getColor())
                .sizeValue(request.getSizeValue())
                .retailPrice(request.getPrice())
                .stockQuantity(request.getStock()) // Số lượng ban đầu
                .isActive(true)
                .createdBy(username)
                .build();

        // 5. Lưu Variant vào DB
        ProductVariant savedVariant = variantRepository.save(variant);

        // 6. Ghi log kho lần đầu (Initial Stock)
        // Vinh Lập Chùa: Dùng luôn stock ban đầu làm delta
        inventoryLogService.logChange(
                savedVariant,
                request.getStock(),
                request.getStock(),
                "INITIAL_STOCK",
                username
        );

        log.info("Variant created successfully with SKU: {}", generatedSku);
    }
}