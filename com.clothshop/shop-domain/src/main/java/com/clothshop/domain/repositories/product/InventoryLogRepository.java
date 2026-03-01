package com.clothshop.domain.repositories.product;

import com.clothshop.domain.entities.product.InventoryLog;
import com.clothshop.domain.entities.product.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    List<InventoryLog> findByProductVariantOrderByCreatedAtDesc(ProductVariant productVariant);
}
