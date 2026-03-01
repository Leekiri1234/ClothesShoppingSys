package com.clothshop.domain.repositories.marketing;

import com.clothshop.domain.entities.marketing.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findByCollectionSlug(String collectionSlug);

    List<Collection> findByIsActiveTrueOrderByCreatedAtDesc();
}
