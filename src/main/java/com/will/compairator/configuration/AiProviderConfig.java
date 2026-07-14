package com.will.compairator.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class AiProviderConfig {

    // pour matcher les éléments dans application.properties
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String apiKey;
    @NotBlank
    private String model;
    @NotBlank
    private String endpoint;

}
