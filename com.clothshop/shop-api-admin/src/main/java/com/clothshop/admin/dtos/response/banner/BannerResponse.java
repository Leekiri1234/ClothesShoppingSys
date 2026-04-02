package com.clothshop.admin.dtos.response.banner;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {

    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private Integer displayOrder;

    private String status; // ACTIVE, INACTIVE

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
