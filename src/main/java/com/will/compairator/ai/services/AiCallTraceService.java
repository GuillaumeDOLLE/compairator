package com.will.compairator.ai.services;

import com.will.compairator.ai.enums.AiCallStatus;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.model.AiCallTraceEntity;
import com.will.compairator.ai.model.exception.AiCallTraceCreationException;
import com.will.compairator.ai.persistence.AiCallTraceRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AiCallTraceService {

    private final AiCallTraceRepository aiCallTraceRepository;

    public AiCallTraceService(AiCallTraceRepository aiCallTraceRepository) {
        this.aiCallTraceRepository = aiCallTraceRepository;
    }

    public AiCallTraceEntity createAiCallTrace(
            AiProvider provider,
            String model,
            String prompt,
            String content,
            AiCallStatus status,
            String errorMessage,
            Instant createdAt,
            long durationMs
    ) {

        if (provider == null) {
            throw new AiCallTraceCreationException("Le provider ne peut pas être null");
        }

        if (StringUtils.isBlank(model)) {
            throw new AiCallTraceCreationException("Le model du provider doit être spécifié");
        }

        if (StringUtils.isBlank(prompt)) {
            throw new AiCallTraceCreationException("Le prompt de la requête ne doit pas être null ou vide");
        }

        if (status == null) {
            throw new AiCallTraceCreationException("Le status de la requête ne peut pas être null");
        }

        if (status == AiCallStatus.SUCCESS && StringUtils.isBlank(content)) {
            throw new AiCallTraceCreationException("Le content est obligatoire si le call a fonctionné");
        }

        if (status == AiCallStatus.ERROR && StringUtils.isBlank(errorMessage)) {
            throw new AiCallTraceCreationException("Le message d'erreur est obligatoire si le call a échoué");
        }

        AiCallTraceEntity newAiCallTrace = AiCallTraceEntity.builder()
                .provider(provider)
                .model(model)
                .prompt(prompt)
                .content(content)
                .status(status)
                .errorMessage(errorMessage)
                .createdAt(createdAt)
                .durationMs(durationMs)
                .build();

        return aiCallTraceRepository.save(newAiCallTrace);
    }

}
