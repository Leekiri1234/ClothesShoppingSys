package com.clothshop.client.services;

import com.clothshop.client.dtos.request.AddToCartRequest;
import com.clothshop.client.dtos.response.CartItemResponse;
import com.clothshop.client.dtos.response.CartSummaryResponse;
import com.clothshop.client.mappers.CartClientMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.customer.Cart;
import com.clothshop.domain.entities.customer.CartItem;
import com.clothshop.domain.entities.product.ProductVariant;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.customer.CartItemRepository;
import com.clothshop.domain.repositories.customer.CartRepository;
import com.clothshop.domain.repositories.product.ProductVariantRepository;
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
public class CartClientService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final AccountRepository accountRepository;
    private final CartClientMapper cartMapper;

    private Customer getCustomerByUsername(String username) {
        Account account = accountRepository.findByUsernameWithCustomer(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
        return account.getCustomer();
    }

    private Cart getOrCreateCart(Customer customer) {
        return cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .customer(customer)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public void addToCart(String username, AddToCartRequest request) {
        Customer customer = getCustomerByUsername(username);
        Cart cart = getOrCreateCart(customer);

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Sản phẩm không tồn tại"));

        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Sản phẩm chỉ còn " + variant.getStockQuantity() + " chiếc");
        }

        CartItem existingItem = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId()).orElse(null);

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + request.getQuantity();
            if (variant.getStockQuantity() < newQty) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Vượt quá số lượng tồn kho cho phép");
            }
            existingItem.setQuantity(newQty);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .price(variant.getRetailPrice())
                    .build();
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void updateQuantity(String username, Long cartItemId, Integer newQuantity) {
        Customer customer = getCustomerByUsername(username);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy sản phẩm trong giỏ"));

        if (!item.getCart().getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Không có quyền thao tác");
        }

        if (item.getVariant().getStockQuantity() < newQuantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Vượt quá số lượng tồn kho");
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(String username, Long cartItemId) {
        Customer customer = getCustomerByUsername(username);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!item.getCart().getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public CartSummaryResponse getCartSummary(String username) {
        Customer customer = getCustomerByUsername(username);
        Cart cart = getOrCreateCart(customer);

        List<CartItemResponse> items = cart.getItems().stream()
                .map(cartMapper::toItemResponse)
                .collect(Collectors.toList());

        int totalItems = items.stream().mapToInt(CartItemResponse::getQuantity).sum();
        double totalAmount = items.stream().mapToDouble(CartItemResponse::getSubtotal).sum();

        return CartSummaryResponse.builder()
                .items(items)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }

    @Transactional(readOnly = true)
    public int getCartItemCount(String username) {
        Customer customer = getCustomerByUsername(username);
        Cart cart = cartRepository.findByCustomerId(customer.getId()).orElse(null);
        if (cart == null || cart.getItems() == null) return 0;
        return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    }
}