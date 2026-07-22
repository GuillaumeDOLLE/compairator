package com.will.compairator.ai.providers;

import com.will.compairator.ai.enums.AiProvider;

public final class ProviderFactory {

    public static IProviderAi getProvider(AiProvider aiProvider) {

            return switch (aiProvider) {
                case GROQ -> new GroqAi();
                case MISTRAL -> new MistralAi();
                // no default because the error is being handled before the switch
            };
    }

}
