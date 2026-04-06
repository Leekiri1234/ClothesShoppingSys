package com.clothshop.client.services;

import com.clothshop.client.dtos.request.OrderCreateRequest;
import com.clothshop.client.dtos.response.CheckoutSummaryResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.customer.Cart;
import com.clothshop.domain.entities.customer.CartItem;
import com.clothshop.domain.entities.marketing.Voucher;
import com.clothshop.domain.entities.marketing.VoucherRedemption;
import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.entities.order.OrderItem;
import com.clothshop.domain.entities.order.OrderStatusHistory;
import com.clothshop.domain.entities.order.Payment;
import com.clothshop.domain.enums.DiscountType;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.enums.PaymentStatus;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.customer.CartItemRepository;
import com.clothshop.domain.repositories.customer.CartRepository;
import com.clothshop.domain.repositories.marketing.VoucherRepository;
import com.clothshop.domain.repositories.marketing.VoucherRedemptionRepository;
import com.clothshop.domain.repositories.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutClientService {

    private static final double SHIPPING_FEE_EXPRESS = 30_000.0;
    private static final double SHIPPING_FEE_STANDARD = 0.0;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;
    private final AccountRepository accountRepository;
    private final VoucherRedemptionRepository voucherRedemptionRepository;

    private Customer getCustomerByUsername(String username) {
        Account account = accountRepository.findByUsernameWithCustomer(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
        return account.getCustomer();
    }

    // 1. Pre-check stock availability
    @Transactional(readOnly = true)
    public void validateCartStock(Customer customer) {
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Giỏ hàng trống"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Không có sản phẩm nào trong giỏ hàng");
        }

        for (CartItem item : cart.getItems()) {
            if (item.getVariant().getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                        "Sản phẩm " + item.getVariant().getProduct().getProductName() +
                                " (Size: " + item.getVariant().getSizeValue() + ") chỉ còn " +
                                item.getVariant().getStockQuantity() + " chiếc");
            }
        }
    }

    // 2. Calculate total + apply voucher
    @Transactional(readOnly = true)
    public CheckoutSummaryResponse calculateTotal(String username, String voucherCode) {
        Customer customer = getCustomerByUsername(username);
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Giỏ hàng trống"));

        double totalAmount = 0.0;
        for (CartItem item : cart.getItems()) {
            totalAmount += (item.getPrice().doubleValue() * item.getQuantity());
        }

        double discount = 0.0;
        String voucherMsg = null;

        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            Voucher voucher = voucherRepository.findByCode(voucherCode.trim().toUpperCase())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Mã giảm giá không tồn tại"));

            if (!"ACTIVE".equals(voucher.getStatus()) ||
                    (voucher.getValidTo() != null && voucher.getValidTo().isBefore(LocalDateTime.now())) ||
                    (voucher.getValidFrom() != null && voucher.getValidFrom().isAfter(LocalDateTime.now()))) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã giảm giá đã hết hạn hoặc chưa có hiệu lực");
            }

            if (voucher.getUsageLimit() != null && voucher.getCurrentUsage() >= voucher.getUsageLimit()) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã giảm giá đã hết lượt sử dụng");
            }

            if (voucher.getMinOrderValue() != null && totalAmount < voucher.getMinOrderValue().doubleValue()) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Đơn hàng tối thiểu để áp dụng mã là " + voucher.getMinOrderValue() + "đ");
            }

            // Calculate discount
            if (DiscountType.PERCENTAGE.equals(voucher.getDiscountType())) {
                discount = totalAmount * (voucher.getDiscountValue().doubleValue() / 100);
                if (voucher.getMaxDiscount() != null && discount > voucher.getMaxDiscount().doubleValue()) {
                    discount = voucher.getMaxDiscount().doubleValue();
                }
            } else if (DiscountType.FIXED_AMOUNT.equals(voucher.getDiscountType())) {
                discount = voucher.getDiscountValue().doubleValue();
            }
            voucherMsg = "Đã áp dụng mã giảm giá thành công!";
        }

        double finalAmount = Math.max(0, totalAmount - discount);

        return CheckoutSummaryResponse.builder()
                .totalAmount(totalAmount)
                .discount(discount)
                .finalAmount(finalAmount)
                .voucherCode(voucherCode)
                .voucherMessage(voucherMsg)
                .build();
    }

    // 3. Create Order
    @Transactional
    public String placeOrder(String username, OrderCreateRequest request) {
        Customer customer = getCustomerByUsername(username);

        // Cập nhật thông tin cá nhân và giao hàng cho Customer
        if (request.getFullName() != null && !request.getFullName().isBlank())
            customer.setFullName(request.getFullName());
        if (request.getEmail() != null && !request.getEmail().isBlank())
            customer.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) customer.setPhoneNumber(request.getPhoneNumber());

        // Ghép địa chỉ đầy đủ từ shippingAddress + district + province
        List<String> addressParts = new ArrayList<>();
        if (request.getShippingAddress() != null && !request.getShippingAddress().isBlank())
            addressParts.add(request.getShippingAddress());
        if (request.getDistrict() != null && !request.getDistrict().isBlank())
            addressParts.add(request.getDistrict());
        if (request.getProvince() != null && !request.getProvince().isBlank())
            addressParts.add(request.getProvince());
        if (!addressParts.isEmpty()) customer.setAddress(String.join(", ", addressParts));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Giỏ hàng trống"));

        validateCartStock(customer);
        CheckoutSummaryResponse summary = calculateTotal(username, request.getVoucherCode());

        // Tính phí ship dựa theo shippingMethod
        double shippingFee = "EXPRESS".equalsIgnoreCase(request.getShippingMethod())
                ? SHIPPING_FEE_EXPRESS : SHIPPING_FEE_STANDARD;
        double finalAmountWithShipping = Math.max(0, summary.getFinalAmount() + shippingFee);

        // Create Order Entity
        Order order = new Order();
        order.setOrderInvoice("ORD-" + System.currentTimeMillis());
        order.setCustomer(customer);
        order.setTotalAmount(BigDecimal.valueOf(summary.getTotalAmount()));
        order.setDiscount(BigDecimal.valueOf(summary.getDiscount()));
        order.setTotalPrice(BigDecimal.valueOf(finalAmountWithShipping));
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING); // Khởi tạo với PENDING

        int totalQty = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        // Create OrderItems from CartItems
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPrice());
            orderItems.add(orderItem);
            totalQty += cartItem.getQuantity();
        }
        order.setOrderItems(orderItems);
        order.setTotalQuantity(totalQty);

        // 4. Record OrderStatusHistory
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setNewStatus(OrderStatus.PENDING);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("Order created by customer");

        List<OrderStatusHistory> histories = new ArrayList<>();
        histories.add(history);
        order.setStatusHistory(histories);

        // Create Payment Entity
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(request.getPaymentMethod().name());
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);
        order.setPayment(payment);

        // Update Voucher usage if applicable
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            Voucher voucher = voucherRepository.findByCode(request.getVoucherCode().toUpperCase()).orElse(null);
            if (voucher != null) {
                Integer current = voucher.getCurrentUsage() == null ? 0 : voucher.getCurrentUsage();
                voucher.setCurrentUsage(current + 1);
                voucherRepository.save(voucher);

                VoucherRedemption redemption = new VoucherRedemption();
                redemption.setVoucher(voucher);
                redemption.setCustomer(customer);
                redemption.setOrder(order);
                redemption.setDiscountAmount(BigDecimal.valueOf(summary.getDiscount()));
                voucherRedemptionRepository.save(redemption);
            }
        }

        // Save Order (Cascade will save OrderItems, History, and Payment)
        Order savedOrder = orderRepository.save(order);

        // Clear Cart
        cartItemRepository.deleteAllByCartId(cart.getId());

        // CHÚ Ý: CHƯA TRỪ STOCK Ở ĐÂY. Stock sẽ được trừ khi Admin Verify Payment.

        return savedOrder.getOrderInvoice();
    }
}