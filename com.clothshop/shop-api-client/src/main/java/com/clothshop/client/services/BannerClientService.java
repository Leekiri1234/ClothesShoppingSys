package com.clothshop.client.services;

import com.clothshop.domain.entities.cms.Banner;
import com.clothshop.domain.repositories.cms.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerClientService {

    private final BannerRepository bannerRepository;

    public List<Banner> getActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .filter(b -> "ACTIVE".equals(b.getStatus()))
                .collect(Collectors.toList());
    }
}
