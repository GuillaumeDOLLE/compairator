package com.will.compairator.ai;

import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientFactory {

    private final RestClient.Builder restClientBuilder;

    public RestClientFactory(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public RestClient buildRestClient(AiProviderConfig providerConfig) {
        return restClientBuilder
                .baseUrl(providerConfig.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + providerConfig.getApiKey())
                .build();
    }

}
