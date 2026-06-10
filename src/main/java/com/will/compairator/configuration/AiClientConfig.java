package com.will.compairator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {

    @Bean("groq")
    public RestClient groqRestClient(RestClient.Builder builder,
                                     @Value("${ai.groq.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean("mistral")
    public RestClient mistralRestClient(RestClient.Builder builder,
                                        @Value("${ai.mistral.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
