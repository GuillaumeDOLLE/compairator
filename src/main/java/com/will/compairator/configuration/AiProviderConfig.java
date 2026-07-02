package com.will.compairator.configuration;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class AiProviderConfig {

    // pour matcher les éléments dans application.properties
    private String baseUrl;
    private String apiKey;
    private String model;
    private String endpoint;

}
