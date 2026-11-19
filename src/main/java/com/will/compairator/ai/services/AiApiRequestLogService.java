package com.will.compairator.ai.services;

import com.will.compairator.ai.model.AiApiRequestLogEntity;
import com.will.compairator.ai.persistence.AiApiRequestLogRepository;
import org.springframework.stereotype.Service;
import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiApiResponseOrigin;
import com.will.compairator.user.model.UserEntity;
import com.will.compairator.comparison.model.ComparisonEntity;

@Service
public class AiApiRequestLogService {

    private final AiApiRequestLogRepository aiApiRequestLogRepository;
    private final TokenCostService tokenCosts;

    public AiApiRequestLogService(AiApiRequestLogRepository aiApiRequestLogRepository, TokenCostService tokenCosts) {
        this.aiApiRequestLogRepository = aiApiRequestLogRepository;
        this.tokenCosts = tokenCosts;
    }

    // Return type is AiApiRequestLogEntity for future possible uses (atm we don't need it)
    public AiApiRequestLogEntity createAiApiRequestLog(AiApiRequestObject aiApiRequestObject,
            UserEntity user, ComparisonEntity comparison, AiApiDTO.Usage usage) {
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
                .user(user)
                .comparison(comparison)
                .promptTokens(aiApiRequestObject.origin() == AiApiResponseOrigin.CACHE ? Long.valueOf(0) : nonNegative(usage == null ? null : usage.promptTokens()))
                .completionTokens(aiApiRequestObject.origin() == AiApiResponseOrigin.CACHE ? Long.valueOf(0) : nonNegative(usage == null ? null : usage.completionTokens()))
                .estimatedCostUsd(tokenCosts.estimate(aiApiRequestObject.provider(), aiApiRequestObject.model(), usage, aiApiRequestObject.origin()))
                .build();

        return aiApiRequestLogRepository.save(aiApiRequestLogEntity);
    }

    private static Long nonNegative(Long count) { return count == null || count < 0 ? null : count; }
}
