package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.BannerFormDTO;
import com.clothshop.admin.dtos.request.banner.BannerRequest;
import com.clothshop.admin.dtos.response.banner.BannerResponse;
import com.clothshop.domain.entities.cms.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BannerMapper {

    // =========================
    // 📌 Request → Entity
    // =========================
    Banner toEntity(BannerRequest request);

    // =========================
    // 📌 Entity → Response
    // =========================
    BannerResponse toResponse(Banner banner);

    // =========================
    // 📌 Entity → FormDTO (để bind form)
    // =========================
    BannerFormDTO toFormDTO(Banner banner);

    // =========================
    // 📌 FormDTO → Entity (khi submit form edit)
    // =========================
    Banner toEntity(BannerFormDTO formDTO);

    // =========================
    // 📌 List mapping
    // =========================
    List<BannerResponse> toResponseList(List<Banner> banners);
}