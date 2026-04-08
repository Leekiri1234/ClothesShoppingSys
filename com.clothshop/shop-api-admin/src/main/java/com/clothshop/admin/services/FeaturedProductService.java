package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.marketing.FeaturedProductRequest;
import com.clothshop.domain.models.marketing.FeaturedProduct;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.repositories.marketing.FeaturedProductRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeaturedProductService {

    private final FeaturedProductRepository featuredProductRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<FeaturedProduct> getActiveFeaturedProducts() {
        return featuredProductRepository.findAllActiveFeaturedProducts();
    }

    @Transactional
    public void updateFeaturedList(List<FeaturedProductRequest> productRequests, String username) {
        log.info("Bắt đầu cập nhật danh sách sản phẩm nổi bật bởi: {}", username);

        // 1. Tạm thời ẩn TẤT CẢ các sản phẩm đang nổi bật hiện tại
        // Việc này giúp reset trạng thái, những SP nào còn trong list mới sẽ được bật lại ở bước sau
        featuredProductRepository.deactivateAllFeaturedProducts(username);

        if (productRequests == null || productRequests.isEmpty()) {
            log.info("Danh sách gửi lên trống, đã ẩn toàn bộ sản phẩm nổi bật.");
            return;
        }

        List<FeaturedProduct> toSave = new ArrayList<>();

        // 2. Duyệt qua danh sách mới gửi từ UI (đã đúng thứ tự kéo thả)
        for (int i = 0; i < productRequests.size(); i++) {
            FeaturedProductRequest req = productRequests.get(i);
            Long productId = req.getProductId();

            // Luôn ưu tiên thứ tự index trong mảng gửi về để đảm bảo tính chính xác của kéo thả
            int finalOrder = i + 1;

            // Tìm record cũ (bao gồm cả đã ẩn)
            FeaturedProduct fp = featuredProductRepository.findByProductIdIncludingInactive(productId)
                    .orElseGet(() -> {
                        // Nếu chưa có thì tạo mới
                        Product productProxy = productRepository.getReferenceById(productId);
                        return FeaturedProduct.builder()
                                .product(productProxy)
                                .createdBy(username)
                                .build();
                    });

            // Cập nhật thông tin
            fp.setIsActive(true); // Bật lại nếu trước đó bị ẩn
            fp.setDisplayOrder(finalOrder);
            fp.setUpdatedBy(username);

            toSave.add(fp);
        }

        // 3. Lưu toàn bộ chỉ với 1-2 câu lệnh database (tối ưu performance)
        featuredProductRepository.saveAll(toSave);
        log.info("Đã cập nhật thành công {} sản phẩm nổi bật.", toSave.size());
    }
}