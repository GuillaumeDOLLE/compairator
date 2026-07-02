package com.will.compairator.ai;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.configuration.AiProperties;
import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GroqAi extends ProviderAi {

    private final AiProperties aiProperties;
    private final RestClientFactory restClientFactory;

    public GroqAi(AiProperties aiProperties, RestClientFactory restClientFactory) {
        super(aiProperties, restClientFactory);
        this.aiProperties = aiProperties;
        this.restClientFactory = restClientFactory;
    }

    @Override
    public AiProvider getProvider() {
        return AiProvider.GROQ;
    }

    @Override
    public AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest) {
        AiProviderConfig providerConfig = aiProperties.getProviderConfig(this.getProvider());
        RestClient restClient = restClientFactory.buildRestClient(providerConfig);
        System.out.println("Send request de Groq");

        return restClient.post()
                .uri(providerConfig.getEndpoint())
                .body(aiRequest)
                .retrieve()
                .body(AiApiDTO.PostOutput.class);
    }

}
