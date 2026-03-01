package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    List<FlashSale> findByStatusAndIsActiveTrue(String status);
}
