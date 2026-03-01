package com.clothshop.domain.repositories.auth;

import com.clothshop.domain.entities.auth.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findByIsActiveTrueOrderByFullNameAsc();
}
