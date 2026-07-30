package com.will.compairator.ai.persistence;

import com.will.compairator.ai.enums.AiCallStatus;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.model.AiCallTraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiCallTraceRepository extends JpaRepository<AiCallTraceEntity, Long> {

    public AiCallTraceEntity findByProvider(AiProvider aiProvider);

    public AiCallTraceEntity findByStatus(AiCallStatus aiCallStatus);

}
