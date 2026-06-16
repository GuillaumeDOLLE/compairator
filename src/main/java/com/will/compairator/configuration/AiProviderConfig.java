package com.will.compairator.configuration;

import lombok.Data;

@Data
public class AiProviderConfig {

    private String baseUrl;
    private String apiKey;
    private String model;
    private String endpoint;

}
