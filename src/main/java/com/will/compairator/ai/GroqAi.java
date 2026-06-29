package com.will.compairator.ai;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.configuration.AiClientConfig;
import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.web.client.RestClient;

public class GroqAi {

    private final AiClientConfig aiClientConfig;

    public GroqAi(AiClientConfig aiClientConfig) {
        this.aiClientConfig = aiClientConfig;
    }

    public AiApiDTO.Output sendRequest(AiApiDTO.Input aiRequest, AiProviderConfig config) {
        RestClient restClient = aiClientConfig.buildRestClient(config);

        return restClient.post()
                .uri(config.getEndpoint())
                .body(aiRequest)
                .retrieve()
                .body(AiApiDTO.Output.class);
    }

}
