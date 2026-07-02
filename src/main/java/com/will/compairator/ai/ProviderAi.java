package com.will.compairator.ai;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.configuration.AiProperties;
import com.will.compairator.configuration.AiProviderConfig;
import lombok.Getter;

@Getter
public abstract class ProviderAi {

    protected final AiProperties aiProperties;
    protected final RestClientFactory restClientFactory;

    protected ProviderAi(AiProperties aiProperties, RestClientFactory restClientFactory) {
        this.aiProperties = aiProperties;
        this.restClientFactory = restClientFactory;
    }

    public abstract AiProvider getProvider();

    public abstract AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest);
}
