package com.will.compairator.comparison.persistence;

import com.will.compairator.comparison.model.ComparisonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.*;

public interface ComparisonRepository extends JpaRepository<ComparisonEntity, UUID> {
    Optional<ComparisonEntity> findByIdAndUserId(UUID id, Long userId);
    /** Comparaisons lancées entre from inclus et to exclu. */
    @Query("select comparison from ComparisonEntity comparison where comparison.createdAt >= :from and comparison.createdAt < :to")
    List<ComparisonEntity> findCreatedBetween(Instant from, Instant to);
}
