package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.banner.BannerRequest;
import com.clothshop.admin.dtos.response.banner.BannerResponse;
import com.clothshop.admin.mappers.BannerMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.common.utils.FileUploadUtil;
import com.clothshop.domain.entities.cms.Banner;
import com.clothshop.domain.repositories.cms.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;
    private final FileUploadUtil fileUploadUtil;

    // =========================
    // 📌 GET ALL
    // =========================
    @Transactional(readOnly = true)
    public List<BannerResponse> getAll() {
        return bannerMapper.toResponseList(
                bannerRepository.findAll(Sort.by("displayOrder"))
        );
    }

    // =========================
    // 📌 GET BY ID
    // =========================
    @Transactional(readOnly = true)
    public BannerResponse getById(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy banner"));

        return bannerMapper.toResponse(banner);
    }

    // =========================
    // 📌 CREATE
    // =========================
    @Transactional
    public void create(BannerRequest request, MultipartFile file) {

        Banner banner = bannerMapper.toEntity(request);

        // mặc định active
        banner.setIsActive(true);

        // default status
        if (banner.getStatus() == null) {
            banner.setStatus("ACTIVE");
        }

        // upload image
        if (file != null && !file.isEmpty()) {
            String fileName = fileUploadUtil.upload(file, "banners");
            banner.setImageUrl("/uploads/banners/" + fileName);
        }

        bannerRepository.save(banner);
        log.info("Banner created: {}", banner.getTitle());
    }

    // =========================
    // 📌 UPDATE
    // =========================
    @Transactional
    public void update(Long id, BannerRequest request, MultipartFile file) {

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy banner"));

        banner.setTitle(request.getTitle());
        banner.setLinkUrl(request.getLinkUrl());
        banner.setDisplayOrder(request.getDisplayOrder());
        banner.setStatus(request.getStatus());

        // 🔥 FIX: thêm 2 field này
        banner.setStartDate(request.getStartDate());
        banner.setEndDate(request.getEndDate());

        // update image nếu có
        if (file != null && !file.isEmpty()) {
            String fileName = fileUploadUtil.upload(file, "banners");
            banner.setImageUrl("/uploads/banners/" + fileName);
        }

        bannerRepository.save(banner);
        log.info("Banner updated: {}", banner.getId());
    }

    // =========================
    // 📌 DELETE (SOFT DELETE chuẩn Hibernate)
    // =========================
    @Transactional
    public void delete(Long id) {

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // dùng @SQLDelete
        bannerRepository.delete(banner);

        log.info("Banner deleted (soft): {}", id);
    }

    // =========================
    // 📌 UPDATE DISPLAY ORDER (REORDER CHUẨN)
    // =========================
    @Transactional
    public void updateOrder(Long id, int newOrder) {

        Banner current = bannerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        List<Banner> banners = bannerRepository.findAll(Sort.by("displayOrder"));

        for (Banner b : banners) {
            if (b.getId().equals(id)) continue;

            if (b.getDisplayOrder() != null && b.getDisplayOrder() >= newOrder) {
                b.setDisplayOrder(b.getDisplayOrder() + 1);
            }
        }

        current.setDisplayOrder(newOrder);

        bannerRepository.saveAll(banners);

        log.info("Banner {} updated order to {}", id, newOrder);
    }
}