package com.will.compairator.ai.enums;

public enum AiProvider {

    GROQ(
            "groq"
    ),
    MISTRAL(
            "mistral"
    );

    final String providerName;

    AiProvider(String providerName) {
        this.providerName = providerName;
    }

}
