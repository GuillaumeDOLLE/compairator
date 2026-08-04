package com.will.compairator.ai.services;

import com.will.compairator.ai.model.AiApiRequestLogEntity;
import com.will.compairator.ai.persistence.AiApiRequestLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AiApiRequestLogService {

    private final AiApiRequestLogRepository aiApiRequestLogRepository;

    public AiApiRequestLogService(AiApiRequestLogRepository aiApiRequestLogRepository) {
        this.aiApiRequestLogRepository = aiApiRequestLogRepository;
    }

    // Return type is AiApiRequestLogEntity for future possible uses (atm we don't need it)
    public AiApiRequestLogEntity createAiApiRequestLog(AiApiRequestObject aiApiRequestObject) {
        AiApiRequestLogEntity aiApiRequestLogEntity = AiApiRequestLogEntity.builder()
                .provider(aiApiRequestObject.provider())
                .model(aiApiRequestObject.model())
                .prompt(aiApiRequestObject.prompt())
                .content(aiApiRequestObject.content())
                .status(aiApiRequestObject.status())
                .errorMessage(aiApiRequestObject.errorMessage())
                .createdAt(aiApiRequestObject.createdAt())
                .durationMs(aiApiRequestObject.durationMs())
                .origin(aiApiRequestObject.origin())
                .build();

        return aiApiRequestLogRepository.save(aiApiRequestLogEntity);
    }
}
