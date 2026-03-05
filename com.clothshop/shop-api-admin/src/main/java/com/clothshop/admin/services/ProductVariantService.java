package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.products.VariantCreateRequest;
import com.clothshop.admin.dtos.request.products.StockUpdateRequest;
import com.clothshop.admin.dtos.request.products.VariantPriceUpdateRequest;
import com.clothshop.admin.dtos.response.products.VariantResponse;
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

import java.util.List;
import java.util.stream.Collectors;

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
        // Dùng luôn stock ban đầu làm delta
        inventoryLogService.logChange(
                savedVariant,
                request.getStock(),
                request.getStock(),
                "INITIAL_STOCK",
                username
        );

        log.info("Variant created successfully with SKU: {}", generatedSku);
    }

    /**
     * Cập nhật số lượng tồn kho thủ công từ Admin.
     * @param variantId ID của biến thể cần cập nhật
     * @param request DTO chứa số lượng mới và lý do
     * @param username Người thực hiện
     */
    @Transactional
    public void updateStock(Long variantId, StockUpdateRequest request, String username) {
        log.info("Updating stock for variant ID: {}. New stock: {}, Reason: {}",
                variantId, request.getNewStock(), request.getReason());

        // 1. Tìm variant, validate tồn tại
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy biến thể sản phẩm"));

        // 2. Tính toán độ chênh lệch (Delta)
        // Delta = Số mới - Số cũ.
        // Ví dụ: Đang có 10, cập nhật thành 15 -> delta = +5 (Nhập thêm)
        // Đang có 10, cập nhật thành 8 -> delta = -2 (Xuất bỏ/Hao hụt)
        int currentStock = variant.getStockQuantity();
        int newStock = request.getNewStock();
        int delta = newStock - currentStock;

        // Nếu không có sự thay đổi, không cần làm gì cả để tiết kiệm CPU và I/O
        if (delta == 0) {
            log.warn("Update stock called but no change in quantity for variant ID: {}", variantId);
            return;
        }

        // 3. Cập nhật số lượng mới vào Entity
        variant.setStockQuantity(newStock);
        variant.setUpdatedBy(username);
        variantRepository.save(variant);

        // 4. Ghi nhật ký biến động kho
        // Truyền chính xác delta, newStock, lý do và ghi chú (nếu có) để đối soát sau này.
        inventoryLogService.logChange(
                variant,
                delta,
                newStock,
                request.getReason(),
                request.getNote(),
                username
        );

        log.info("Stock updated successfully for SKU: {}. Delta: {}", variant.getSku(), delta);
    }

    /**
     * Get all variants for a specific product.
     */
    @Transactional(readOnly = true)
    public List<VariantResponse> getVariantsByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy sản phẩm"));

        return product.getVariants().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get variant by ID.
     */
    @Transactional(readOnly = true)
    public VariantResponse getVariantById(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy biến thể sản phẩm"));
        return mapToResponse(variant);
    }

    /**
     * Update variant price.
     */
    @Transactional
    public void updatePrice(Long variantId, VariantPriceUpdateRequest request, String username) {
        log.info("Updating price for variant ID: {}. New price: {}", variantId, request.getPrice());

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy biến thể sản phẩm"));

        if (request.getPrice() != null) {
            variant.setRetailPrice(request.getPrice());
            variant.setUpdatedBy(username);
            variantRepository.save(variant);

            log.info("Price updated successfully for SKU: {}. New price: {}", variant.getSku(), request.getPrice());
        } else {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Giá mới không được để trống");
        }
    }

    /**
     * Map entity to response DTO.
     */
    private VariantResponse mapToResponse(ProductVariant variant) {
        return VariantResponse.builder()
                .variantId(variant.getId())
                .sku(variant.getSku())
                .color(variant.getColor())
                .sizeValue(variant.getSizeValue())
                .retailPrice(variant.getRetailPrice())
                .stockQuantity(variant.getStockQuantity())
                .imageUrl(variant.getImageUrl())
                .isActive(variant.getIsActive())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getProductName())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .createdBy(variant.getCreatedBy())
                .updatedBy(variant.getUpdatedBy())
                .build();
    }
}