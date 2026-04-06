package com.clothshop.client.services;

import com.clothshop.domain.entities.customer.WishlistItem;
import com.clothshop.client.dtos.response.WishlistItemResponse;
import com.clothshop.client.dtos.response.WishlistResponse;
import com.clothshop.client.mappers.WishlistClientMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.customer.Wishlist;
import com.clothshop.domain.entities.customer.WishlistItem;
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

    private Wishlist getOrCreateWishlist(Customer customer) {
        return wishlistRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
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

        WishlistItem item = wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId);

        // Nếu chưa tồn tại → tạo mới
        if (item == null) {
            WishlistItem newItem = WishlistItem.builder()
                    .wishlist(wishlist)
                    .product(product)
                    .isActive(true)
                    .build();

            wishlistItemRepository.save(newItem);
            log.info("Added product {} to wishlist of user {}", productId, username);
            return true;
        }

        // Nếu tồn tại → toggle
        boolean newStatus = !Boolean.TRUE.equals(item.getIsActive());
        item.setIsActive(newStatus);
        wishlistItemRepository.save(item);

        log.info("Toggle product {} in wishlist of user {} → {}", productId, username, newStatus);
        return newStatus;
    }

    @Transactional(readOnly = true)
    public WishlistResponse getWishlistItems(String username) {
        Customer customer = getCustomerByUsername(username);
        Wishlist wishlist = getOrCreateWishlist(customer);

        List<WishlistItemResponse> items = wishlist.getItems().stream()
                .filter(WishlistItem::getIsActive) // Filter only active items
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
        Wishlist wishlist = wishlistRepository.findByCustomerId(customer.getId()).orElse(null);
        if (wishlist == null || wishlist.getItems() == null){
            return 0;
        } else {
            List<WishlistItemResponse> items = wishlist.getItems().stream()
                    .filter(WishlistItem::getIsActive) // Filter only active items
                    .map(wishlistMapper::toItemResponse)
                    .collect(Collectors.toList());
            return items.size();
        }
    }

    @Transactional
    public void removeFromWishlist(String username, Long productId) {
        Customer customer = getCustomerByUsername(username);
        Wishlist wishlist = wishlistRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Wishlist không tồn tại"));

        wishlistItemRepository.deleteByWishlistIdAndProductId(wishlist.getId(), productId);
        log.info("Removed product {} from wishlist of user {}", productId, username);
    }

    @Transactional(readOnly = true)
    public boolean isProductInWishlist(String username, Long productId) {
        Customer customer = getCustomerByUsername(username);
        Wishlist wishlist = wishlistRepository.findByCustomerId(customer.getId()).orElse(null);
        if (wishlist == null) return false;
        return wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId);
    }
}
