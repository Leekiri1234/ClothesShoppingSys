package com.clothshop.admin.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerFormDTO {

    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private Integer displayOrder;

    private String status; // ACTIVE, INACTIVE

    private LocalDate startDate;

    private LocalDate endDate;
}
