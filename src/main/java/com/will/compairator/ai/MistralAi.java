package com.will.compairator.ai;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.configuration.AiProperties;
import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MistralAi extends ProviderAi {

    private final AiProperties aiProperties;
    private final RestClientFactory restClientFactory;

    public MistralAi(AiProperties aiProperties, RestClientFactory restClientFactory) {
        super(aiProperties, restClientFactory);
        this.aiProperties = aiProperties;
        this.restClientFactory = restClientFactory;
    }

    @Override
    public AiProvider getProvider() {
        return AiProvider.MISTRAL;
    }

    @Override
    public AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest) {
        AiProviderConfig providerConfig = aiProperties.getProviderConfig(this.getProvider());
        RestClient restClient = restClientFactory.buildRestClient(providerConfig);
        System.out.println("Send request de Mistral");

        return restClient.post()
                .uri(providerConfig.getEndpoint())
                .body(aiRequest)
                .retrieve()
                .body(AiApiDTO.PostOutput.class);
    }

}
