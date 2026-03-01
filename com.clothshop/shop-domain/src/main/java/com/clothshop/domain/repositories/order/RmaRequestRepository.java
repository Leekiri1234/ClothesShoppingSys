package com.clothshop.domain.repositories.order;

import com.clothshop.domain.entities.order.RmaRequest;;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RmaRequestRepository extends JpaRepository<RmaRequest, Long> {}
