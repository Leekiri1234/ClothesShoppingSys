package com.clothshop.domain.repositories.cms;

import com.clothshop.domain.entities.cms.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT COALESCE(MAX(b.displayOrder), 0) FROM Banner b")
    Integer findMaxDisplayOrder();
}
