package com.will.compairator.configuration;

import jakarta.validation.constraints.NotBlank;

public record AiProviderConfig(
        @NotBlank String apiKey,
        @NotBlank String baseUrl,
        @NotBlank String model,
        @NotBlank String endpoint
) {
}
