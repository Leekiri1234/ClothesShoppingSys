package com.clothshop.client.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {
    private Long productId;
    private Integer rating; // 1-5
    private String comment;
}
