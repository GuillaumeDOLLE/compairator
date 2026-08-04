package com.will.compairator.ai.persistence;

import com.will.compairator.ai.enums.AiApiRequestStatus;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.model.AiApiRequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiApiRequestLogRepository extends JpaRepository<AiApiRequestLogEntity, Long> {

    public AiApiRequestLogEntity findByProvider(AiProvider aiProvider);

    public AiApiRequestLogEntity findByStatus(AiApiRequestStatus aiCallStatus);

}
