package com.clothshop.client.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long reviewId;
    private String customerName;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
}
