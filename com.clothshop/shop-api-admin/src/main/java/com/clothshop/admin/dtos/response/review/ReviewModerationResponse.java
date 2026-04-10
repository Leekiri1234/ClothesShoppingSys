package com.clothshop.admin.dtos.response.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewModerationResponse {
    private Long reviewId;
    private String productName;
    private String customerName;
    private Integer rating;
    private String comment;
    private String status;
    private String hideReason;
}
