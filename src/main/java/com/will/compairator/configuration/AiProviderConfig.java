package com.will.compairator.configuration;

import jakarta.validation.constraints.NotBlank;

public record AiProviderConfig(
        @NotBlank String apiKey,
        @NotBlank String baseUrl,
        @NotBlank String model,
        @NotBlank String endpoint
) {

    public AiProviderConfig(String apiKey, String baseUrl, String model, String endpoint) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.endpoint = endpoint;
    }
}
