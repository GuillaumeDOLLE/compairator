package com.will.compairator.ai.providers;

import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.configuration.AiProviderConfig;

public class ProviderFactory {

    public static IProviderAi getProvider(AiProvider aiProvider, AiProviderConfig providerConfig) {

            return switch (aiProvider) {
                case GROQ -> new GroqAi(providerConfig);
                case MISTRAL -> new MistralAi(providerConfig);
                default -> throw new IllegalStateException("Unknown provider");
            };
    }

}
