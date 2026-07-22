package com.will.compairator.ai.providers;

import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.configuration.AiProviderConfigResolver;

public class ProviderFactory {

    AiProviderConfigResolver aiProviderConfigResolver;

    public ProviderFactory(AiProviderConfigResolver aiProviderConfigResolver) {
        this.aiProviderConfigResolver = aiProviderConfigResolver;
    }

    public static IProviderAi getProvider(AiProvider aiProvider) {

            return switch (aiProvider) {
                case GROQ -> new GroqAi();
                case MISTRAL -> new MistralAi();
                // no default because the error is being handled before the switch
            };
    }

}
