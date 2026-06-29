package com.will.compairator.configuration;

import com.will.compairator.ai.GroqAi;
import com.will.compairator.ai.MistralAi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {

    private final RestClient.Builder restClientBuilder;

    public AiClientConfig(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public RestClient buildRestClient(AiProviderConfig config) {
        return restClientBuilder
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build();
    }

    @Bean
    public MistralAi mistralAi(AiClientConfig aiClientConfig) {
        return new MistralAi(aiClientConfig);
    }

    @Bean
    public GroqAi groqAi(AiClientConfig aiClientConfig) {
        return new GroqAi(aiClientConfig);
    }

}
