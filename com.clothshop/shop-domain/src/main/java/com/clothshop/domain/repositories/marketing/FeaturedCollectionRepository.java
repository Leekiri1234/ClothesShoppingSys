package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.FeaturedCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturedCollectionRepository extends JpaRepository<FeaturedCollection, Long> {

    List<FeaturedCollection> findByIsActiveTrueOrderByDisplayOrderAsc();
}
