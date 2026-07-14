package com.will.compairator.ai.providers;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.AiProviderCallException;
import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class MistralAi implements IProviderAi {

    private final AiProviderConfig aiProviderConfig;

    public MistralAi(AiProviderConfig aiProviderConfig) {
        this.aiProviderConfig = aiProviderConfig;
    }

    @Override
    public AiProvider getProvider() {
        return AiProvider.MISTRAL;
    }

    @Override
    public AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest) {
        RestClient restClient = RestClient.builder()
                .baseUrl(aiProviderConfig.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + aiProviderConfig.getApiKey())
                .build();
        System.out.println("Send request de Mistral");

        try {
            return restClient.post()
                    .uri(aiProviderConfig.getEndpoint())
                    .body(aiRequest)
                    .retrieve()
                    .body(AiApiDTO.PostOutput.class);
        } catch (RestClientException exception) {
            throw new AiProviderCallException(
                    "Failed to call provider " + getProvider(),
                    exception
            );
        }
    }

}
