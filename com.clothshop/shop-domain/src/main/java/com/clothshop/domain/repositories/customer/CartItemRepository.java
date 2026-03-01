package com.clothshop.domain.repositories.customer;

import com.clothshop.domain.entities.customer.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Xóa sạch giỏ hàng sau khi checkout thành công
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(Long cartId);

    // Tìm xem variant này đã có trong giỏ hàng chưa để cộng dồn quantity
    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);
}
