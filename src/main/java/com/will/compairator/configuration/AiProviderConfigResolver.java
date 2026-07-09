package com.will.compairator.configuration;

import com.will.compairator.ai.enums.AiProvider;
import org.springframework.stereotype.Component;

@Component
public class AiProviderConfigResolver {

    private final AiProperties aiProperties;

    public AiProviderConfigResolver(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    public AiProviderConfig resolve(AiProvider provider) {
        return aiProperties.getProviderConfig(provider);
    }

}
