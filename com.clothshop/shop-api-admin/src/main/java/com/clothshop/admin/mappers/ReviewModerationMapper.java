package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.response.review.ReviewModerationResponse;
import com.clothshop.domain.models.product.ProductFeedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewModerationMapper {

    @Mapping(source = "id", target = "reviewId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "customer.fullName", target = "customerName")
    @Mapping(source = "feedbackStatus", target = "status")
    ReviewModerationResponse toResponse(ProductFeedback feedback);
}
