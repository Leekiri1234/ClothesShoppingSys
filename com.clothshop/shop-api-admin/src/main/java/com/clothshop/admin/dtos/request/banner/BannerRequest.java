package com.clothshop.admin.dtos.request.banner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BannerRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String linkUrl;

    @NotNull(message = "Thứ tự hiển thị không được để trống")
    private Integer displayOrder;

    @NotNull(message = "Trạng thái không được để trống")
    private String status; // ACTIVE / INACTIVE

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;


    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

}