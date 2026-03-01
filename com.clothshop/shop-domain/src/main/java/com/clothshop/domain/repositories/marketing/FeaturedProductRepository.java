package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.FeaturedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturedProductRepository extends JpaRepository<FeaturedProduct, Long> {

    List<FeaturedProduct> findByIsActiveTrueOrderByDisplayOrderAsc();
}
