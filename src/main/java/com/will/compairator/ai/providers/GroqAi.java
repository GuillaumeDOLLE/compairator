package com.will.compairator.ai.providers;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.AiProviderCallException;
import com.will.compairator.configuration.AiProviderConfig;
import com.will.compairator.configuration.AiProviderConfigResolver;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class GroqAi implements IProviderAi {

    private final AiProviderConfig aiProviderConfig;

    public GroqAi() {
        this.aiProviderConfig = AiProviderConfigResolver.getInstance().resolve(AiProvider.GROQ);
    }

    @Override
    public AiProvider getProvider() {
        return AiProvider.GROQ;
    }

    @Override
    public String getModel() {
        return aiProviderConfig.model();
    }

    @Override
    public AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest) {
        RestClient restClient = RestClient.builder()
                .baseUrl(aiProviderConfig.baseUrl())
                .defaultHeader("Authorization", "Bearer " + aiProviderConfig.apiKey())
                .build();
        System.out.println("Send request de Groq");

        try {
            return restClient.post()
                    .uri(aiProviderConfig.endpoint())
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
