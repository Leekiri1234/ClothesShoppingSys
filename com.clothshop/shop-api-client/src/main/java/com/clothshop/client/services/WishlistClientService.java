package com.clothshop.client.services;

import com.clothshop.client.dtos.response.WishlistItemResponse;
import com.clothshop.client.dtos.response.WishlistResponse;
import com.clothshop.client.mappers.WishlistClientMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.customer.Wishlist;
import com.clothshop.domain.entities.customer.WishlistItem; // Gộp lại 1 cái thôi
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.customer.WishlistItemRepository;
import com.clothshop.domain.repositories.customer.WishlistRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistClientService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final WishlistClientMapper wishlistMapper;

    private Customer getCustomerByUsername(String username) {
        Account account = accountRepository.findByUsernameWithCustomer(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
        return account.getCustomer();
    }

    // Hàm này dùng để đảm bảo luôn có Wishlist để thao tác
    @Transactional // Phải có Transaction ghi ở đây
    public Wishlist getOrCreateWishlist(Customer customer) {
        return wishlistRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    log.info("Creating new wishlist for customer: {}", customer.getId());
                    Wishlist newWishlist = Wishlist.builder()
                            .customer(customer)
                            .items(new ArrayList<>())
                            .build();
                    return wishlistRepository.save(newWishlist);
                });
    }

    @Transactional
    public boolean toggleWishlist(String username, Long productId) {
        Customer customer = getCustomerByUsername(username);
        Wishlist wishlist = getOrCreateWishlist(customer);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Sản phẩm không tồn tại"));

        // Tìm item hiện tại (bao gồm cả item đã bị de-active nếu shop bạn dùng soft delete)
        WishlistItem item = wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId);

        if (item != null) {
            if (item.getIsActive()) {
                // Nếu đang active thì bỏ khỏi wishlist (Hard delete hoặc set isActive = false tùy Repository của bạn)
                wishlistItemRepository.deleteByWishlistIdAndProductId(wishlist.getId(), productId);
                log.info("Removed product {} from wishlist of user {}", productId, username);
                return false;
            } else {
                // Nếu tồn tại nhưng đang ẩn thì bật lại
                item.setIsActive(true);
                wishlistItemRepository.save(item);
                log.info("Re-activated product {} in wishlist of user {}", productId, username);
                return true;
            }
        } else {
            // Nếu chưa từng tồn tại thì tạo mới hoàn toàn
            WishlistItem newItem = WishlistItem.builder()
                    .wishlist(wishlist)
                    .product(product)
                    .build();
            wishlistItemRepository.save(newItem);
            log.info("Added new product {} to wishlist of user {}", productId, username);
            return true;
        }
    }

    @Transactional // CHỐT: Bỏ readOnly = true vì hàm này gọi getOrCreateWishlist (có thể gây INSERT)
    public WishlistResponse getWishlistItems(String username) {
        Customer customer = getCustomerByUsername(username);
        Wishlist wishlist = getOrCreateWishlist(customer);

        List<WishlistItemResponse> items = wishlist.getItems().stream()
                .filter(item -> item.getIsActive() != null && item.getIsActive())
                .map(wishlistMapper::toItemResponse)
                .collect(Collectors.toList());

        return WishlistResponse.builder()
                .items(items)
                .totalItems(items.size())
                .build();
    }

    @Transactional(readOnly = true)
    public int getWishlistCount(String username) {
        Customer customer = getCustomerByUsername(username);
        // Ở đây dùng count trực tiếp từ DB sẽ nhanh hơn là load cả danh sách lên rồi filter
        return wishlistRepository.findByCustomerId(customer.getId())
                .map(w -> (int) w.getItems().stream().filter(WishlistItem::getIsActive).count())
                .orElse(0);
    }

    @Transactional
    public void removeFromWishlist(String username, Long productId) {
        Customer customer = getCustomerByUsername(username);
        Wishlist wishlist = wishlistRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Wishlist không tồn tại"));

        wishlistItemRepository.deleteByWishlistIdAndProductId(wishlist.getId(), productId);
    }

    @Transactional(readOnly = true)
    public boolean isProductInWishlist(String username, Long productId) {
        Customer customer = getCustomerByUsername(username);
        return wishlistRepository.findByCustomerId(customer.getId())
                .map(w -> wishlistItemRepository.existsByWishlistIdAndProductId(w.getId(), productId))
                .orElse(false);
    }
}