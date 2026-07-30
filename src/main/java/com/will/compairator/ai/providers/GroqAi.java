package com.will.compairator.ai.providers;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.AiProviderCallException;
import com.will.compairator.configuration.AiProviderConfig;
import com.will.compairator.configuration.AiProviderConfigResolver;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class GroqAi implements IProviderAi {

    @Override
    public AiProvider getProvider() {
        return AiProvider.GROQ;
    }

    @Override
    public AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest) {
        AiProviderConfig groqConfig = getConfig();

        RestClient restClient = RestClient.builder()
                .baseUrl(groqConfig.baseUrl())
                .defaultHeader("Authorization", "Bearer " + groqConfig.apiKey())
                .build();
        System.out.println("Send request de Groq");

        try {
            return restClient.post()
                    .uri(groqConfig.endpoint())
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
