//package com.clothshop.client.services;
//
//import com.clothshop.client.dtos.request.ReviewCreateRequest;
//import com.clothshop.client.dtos.response.ReviewResponse;
//import com.clothshop.common.dtos.response.PageResponse;
//import com.clothshop.common.exceptions.BusinessException;
//import com.clothshop.common.exceptions.ErrorCode;
//import com.clothshop.domain.models.auth.Account;
//import com.clothshop.domain.models.auth.Customer;
//import com.clothshop.domain.models.product.Product;
//import com.clothshop.domain.models.product.ProductFeedback;
//import com.clothshop.domain.repositories.auth.AccountRepository;
//import com.clothshop.domain.repositories.order.OrderItemRepository;
//import com.clothshop.domain.repositories.product.ProductFeedbackRepository;
//import com.clothshop.domain.repositories.product.ProductRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ReviewClientService {
//
//    private final ProductFeedbackRepository feedbackRepository;
//    private final OrderItemRepository orderItemRepository;
//    private final ProductRepository productRepository;
//    private final AccountRepository accountRepository;
//
//    private Customer getCustomer(String username) {
//        Account account = accountRepository.findByUsernameWithCustomer(username)
//                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
//        return account.getCustomer();
//    }
//
//    @Transactional
//    public void submitReview(String username, ReviewCreateRequest request) {
//        Customer customer = getCustomer(username);
//        Product product = productRepository.findById(request.getProductId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy sản phẩm"));
//
//        // 1. Validate customer purchased this product
//        boolean purchased = orderItemRepository.existsByOrderCustomerAndVariantProductId(customer, request.getProductId());
//        if (!purchased) {
//            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Bạn chỉ có thể đánh giá sản phẩm đã mua");
//        }
//
//        // 2. Check not already reviewed
//        boolean alreadyReviewed = feedbackRepository.existsByCustomerAndProduct(customer, product);
//        if (alreadyReviewed) {
//            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Bạn đã đánh giá sản phẩm này rồi");
//        }
//
//        // 3. Create Review
//        ProductFeedback feedback = ProductFeedback.builder()
//                .product(product)
//                .customer(customer)
//                .rating(request.getRating())
//                .comment(request.getComment())
//                .feedbackStatus("PENDING") // Tự động duyệt hoặc để PENDING tùy logic
//                .build();
//
//        // 4. Save
//        feedbackRepository.save(feedback);
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<ReviewResponse> getReviewsForProduct(Long productId, Pageable pageable) {
//        Page<ProductFeedback> feedbackPage = feedbackRepository.findByProductIdAndFeedbackStatusOrderByCreatedAtDesc(
//                productId, "APPROVED", pageable);
//
//        List<ReviewResponse> content = feedbackPage.getContent().stream()
//                .map(f -> ReviewResponse.builder()
//                        .reviewId(f.getId())
//                        .customerName(f.getCustomer().getFullName())
//                        .rating(f.getRating())
//                        .comment(f.getComment())
//                        .reviewDate(f.getCreatedAt())
//                        .build())
//                .collect(Collectors.toList());
//
//        return PageResponse.<ReviewResponse>builder()
//                .content(content)
//                .pageNumber(feedbackPage.getNumber())
//                .pageSize(feedbackPage.getSize())
//                .totalElements(feedbackPage.getTotalElements())
//                .totalPages(feedbackPage.getTotalPages())
//                .build();
//    }
//
//    @Transactional(readOnly = true)
//    public boolean canReview(String username, Long productId) {
//        if (username == null || productId == null) return false;
//
//        return accountRepository.findByUsernameWithCustomer(username)
//                .map(account -> {
//                    Customer customer = account.getCustomer();
//                    Product product = productRepository.findById(productId).orElse(null);
//                    if (product == null) return false;
//
//                    boolean purchased = orderItemRepository.existsByOrderCustomerAndVariantProductId(customer, productId);
//                    boolean alreadyReviewed = feedbackRepository.existsByCustomerAndProduct(customer, product);
//
//                    return purchased && !alreadyReviewed;
//                })
//                .orElse(false);
//    }
//}
package com.clothshop.client.services;

import com.clothshop.client.dtos.request.ReviewCreateRequest;
import com.clothshop.client.dtos.response.ReviewResponse;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.models.auth.Account;
import com.clothshop.domain.models.auth.Customer;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.models.product.ProductFeedback;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.order.OrderItemRepository;
import com.clothshop.domain.repositories.product.ProductFeedbackRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewClientService {

    private final ProductFeedbackRepository feedbackRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    // Chỉ cho phép review khi đơn hàng đã giao thành công
    private static final List<OrderStatus> REVIEWABLE_STATUSES =
            List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED);

    private Customer getCustomer(String username) {
        Account account = accountRepository.findByUsernameWithCustomer(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
        return account.getCustomer();
    }

    @Transactional
    public void submitReview(String username, ReviewCreateRequest request) {
        Customer customer = getCustomer(username);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy sản phẩm"));

        // 1. Validate đã mua và đơn hàng đã hoàn thành/giao
        boolean purchased = orderItemRepository
                .existsByOrderCustomerAndVariantProductIdAndOrderStatusIn(
                        customer, request.getProductId(), REVIEWABLE_STATUSES);
        if (!purchased) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Bạn chỉ có thể đánh giá sản phẩm đã nhận được");
        }

        // 2. Chưa đánh giá sản phẩm này
        boolean alreadyReviewed = feedbackRepository.existsByCustomerAndProduct(customer, product);
        if (alreadyReviewed) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Bạn đã đánh giá sản phẩm này rồi");
        }

        // 3. Validate rating
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Vui lòng chọn số sao (1-5)");
        }

        // 4. Lưu
        ProductFeedback feedback = ProductFeedback.builder()
                .product(product)
                .customer(customer)
                .rating(request.getRating())
                .comment(request.getComment())
                .feedbackStatus("PENDING")
                .build();

        feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviewsForProduct(Long productId, Pageable pageable) {
        Page<ProductFeedback> feedbackPage = feedbackRepository
                .findByProductIdAndFeedbackStatusOrderByCreatedAtDesc(productId, "APPROVED", pageable);

        List<ReviewResponse> content = feedbackPage.getContent().stream()
                .map(f -> ReviewResponse.builder()
                        .reviewId(f.getId())
                        .customerName(f.getCustomer().getFullName())
                        .rating(f.getRating())
                        .comment(f.getComment())
                        .reviewDate(f.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .pageNumber(feedbackPage.getNumber())
                .pageSize(feedbackPage.getSize())
                .totalElements(feedbackPage.getTotalElements())
                .totalPages(feedbackPage.getTotalPages())
                .build();
    }

    /**
     * canReview = đã mua (DELIVERED/COMPLETED) VÀ chưa từng đánh giá.
     */
    @Transactional(readOnly = true)
    public boolean canReview(String username, Long productId) {
        if (username == null || productId == null) return false;

        return accountRepository.findByUsernameWithCustomer(username)
                .map(account -> {
                    Customer customer = account.getCustomer();
                    Product product = productRepository.findById(productId).orElse(null);
                    if (customer == null || product == null) return false;

                    boolean purchased = orderItemRepository
                            .existsByOrderCustomerAndVariantProductIdAndOrderStatusIn(
                                    customer, productId, REVIEWABLE_STATUSES);
                    boolean alreadyReviewed = feedbackRepository.existsByCustomerAndProduct(customer, product);

                    return purchased && !alreadyReviewed;
                })
                .orElse(false);
    }
}