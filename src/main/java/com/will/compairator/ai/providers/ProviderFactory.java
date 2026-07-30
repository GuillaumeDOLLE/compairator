package com.will.compairator.ai.providers;

import com.will.compairator.ai.enums.AiProvider;

public final class ProviderFactory {

    public static IProviderAi getProvider(AiProvider aiProvider) {

            return switch (aiProvider) {
                case GROQ -> new GroqAi();
                case MISTRAL -> new MistralAi();
                default -> throw new IllegalStateException("Provider " + aiProvider + " is declared in AiProvider but is not handled by the factory");
            };
    }

}
