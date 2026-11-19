package com.will.compairator.ai.persistence;

import com.will.compairator.ai.enums.AiApiRequestStatus;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.model.AiApiRequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface AiApiRequestLogRepository extends JpaRepository<AiApiRequestLogEntity, Long> {
    /** Appels enregistrés entre from inclus et to exclu. */
    @Query("select call from AiApiRequestLogEntity call where call.createdAt >= :from and call.createdAt < :to")
    List<AiApiRequestLogEntity> findCreatedBetween(Instant from, Instant to);
    /** Appels des comparaisons lancées dans la période, reçus avant la fin d'observation. */
    @Query("""
            select call from AiApiRequestLogEntity call
            where call.comparison.createdAt >= :from and call.comparison.createdAt < :to
              and call.createdAt < :observationEnd
            """)
    List<AiApiRequestLogEntity> findComparisonCalls(
            Instant from, Instant to, Instant observationEnd);

    public AiApiRequestLogEntity findByProvider(AiProvider aiProvider);

    public AiApiRequestLogEntity findByStatus(AiApiRequestStatus aiCallStatus);

}
