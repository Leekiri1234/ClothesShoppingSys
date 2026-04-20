package com.clothshop.admin.services;

import com.clothshop.admin.mappers.ReviewModerationMapper;
import com.clothshop.admin.dtos.response.review.ReviewModerationResponse;
import com.clothshop.common.exceptions  .BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.models.product.ProductFeedback;
import com.clothshop.domain.repositories.product.ProductFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewModerationService {

    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_HIDDEN = "HIDDEN";

    private final ProductFeedbackRepository productFeedbackRepository;
    private final ReviewModerationMapper reviewModerationMapper;

    @Transactional(readOnly = true)
    public Page<ReviewModerationResponse> getReviews(String status, Pageable pageable) {
        return getReviews(status, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReviewModerationResponse> getReviews(String status, Long productId, Integer rating, String keyword, Pageable pageable) {
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<ProductFeedback> reviewPage = productFeedbackRepository.searchAdminReviews(normalizedStatus, productId, rating, normalizedKeyword, pageable);

        return reviewPage.map(reviewModerationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReviewModerationResponse> getReviewsByProductId(Long productId) {
        return productFeedbackRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(reviewModerationMapper::toResponse)
                .toList();
    }

    @Transactional
    public void approveReview(Long reviewId, String moderatorName) {
        ProductFeedback review = findActiveReview(reviewId);
        review.setFeedbackStatus(STATUS_APPROVED);
        review.setHideReason(null);
        review.setModeratedAt(LocalDateTime.now());
        review.setModeratedBy(moderatorName);
        productFeedbackRepository.save(review);

        log.info("Review {} approved by {}", reviewId, moderatorName);
    }

    @Transactional
    public void hideReview(Long reviewId, String reason, String moderatorName) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Vui lòng nhập lý do ẩn đánh giá");
        }

        ProductFeedback review = findActiveReview(reviewId);
        review.setFeedbackStatus(STATUS_HIDDEN);
        review.setHideReason(reason.trim());
        review.setModeratedAt(LocalDateTime.now());
        review.setModeratedBy(moderatorName);
        productFeedbackRepository.save(review);

        log.info("Review {} hidden by {}", reviewId, moderatorName);
    }

    @Transactional
    public void approveReviewForProduct(Long productId, Long reviewId, String moderatorName) {
        ProductFeedback review = findActiveReviewForProduct(productId, reviewId);
        review.setFeedbackStatus(STATUS_APPROVED);
        review.setHideReason(null);
        review.setModeratedAt(LocalDateTime.now());
        review.setModeratedBy(moderatorName);
        productFeedbackRepository.save(review);

        log.info("Review {} for product {} approved by {}", reviewId, productId, moderatorName);
    }

    @Transactional
    public void hideReviewForProduct(Long productId, Long reviewId, String reason, String moderatorName) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Vui lòng nhập lý do ẩn đánh giá");
        }

        ProductFeedback review = findActiveReviewForProduct(productId, reviewId);
        review.setFeedbackStatus(STATUS_HIDDEN);
        review.setHideReason(reason.trim());
        review.setModeratedAt(LocalDateTime.now());
        review.setModeratedBy(moderatorName);
        productFeedbackRepository.save(review);

        log.info("Review {} for product {} hidden by {}", reviewId, productId, moderatorName);
    }

    private ProductFeedback findActiveReview(Long reviewId) {
        return productFeedbackRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đánh giá"));
    }

    private ProductFeedback findActiveReviewForProduct(Long productId, Long reviewId) {
        return productFeedbackRepository.findByIdAndProductIdAndIsActiveTrue(reviewId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đánh giá của sản phẩm"));
    }
}
