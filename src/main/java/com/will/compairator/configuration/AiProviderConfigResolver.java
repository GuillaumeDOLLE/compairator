package com.will.compairator.configuration;

import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.InvalidProviderConfigurationException;
import org.springframework.stereotype.Component;

@Component
public class AiProviderConfigResolver {

    private final AiProperties aiProperties;

    public AiProviderConfigResolver(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    public AiProviderConfig resolve(AiProvider provider) {

        AiProviderConfig config = aiProperties.getProviderConfig(provider);

        if (config == null) {
            throw new InvalidProviderConfigurationException("Invalid configuration for provider " + provider);
        }

        return config;

    }

}
