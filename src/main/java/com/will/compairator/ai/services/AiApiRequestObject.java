package com.will.compairator.ai.services;

import com.will.compairator.ai.enums.AiApiRequestStatus;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.model.exception.AiApiRequestLogCreationException;
import io.micrometer.common.util.StringUtils;

import java.time.Instant;

public record AiApiRequestObject(
        AiProvider provider,
        String model,
        String prompt,
        String content,
        AiApiRequestStatus status,
        String errorMessage,
        Instant createdAt,
        long durationMs
) {
    // compact constructor syntax for records
    public AiApiRequestObject {

        if (provider == null) {
            throw new AiApiRequestLogCreationException("Le provider ne peut pas être null");
        }

        if (StringUtils.isBlank(model)) {
            throw new AiApiRequestLogCreationException("Le model du provider doit être spécifié");
        }

        if (StringUtils.isBlank(prompt)) {
            throw new AiApiRequestLogCreationException("Le prompt de la requête ne doit pas être null ou vide");
        }

        if (status == null) {
            throw new AiApiRequestLogCreationException("Le status de la requête ne peut pas être null");
        }

        if (status == AiApiRequestStatus.SUCCESS && StringUtils.isBlank(content)) {
            throw new AiApiRequestLogCreationException("Le content est obligatoire si le call a fonctionné");
        }

        if (status == AiApiRequestStatus.ERROR && StringUtils.isBlank(errorMessage)) {
            throw new AiApiRequestLogCreationException("Le message d'erreur est obligatoire si le call a échoué");
        }

    }

}
