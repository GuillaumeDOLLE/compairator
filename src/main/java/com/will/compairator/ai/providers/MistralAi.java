package com.will.compairator.ai.providers;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.AiProviderCallException;
import com.will.compairator.configuration.AiProviderConfig;
import com.will.compairator.configuration.AiProviderConfigResolver;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class MistralAi implements IProviderAi {

    @Override
    public AiProvider getProvider() {
        return AiProvider.MISTRAL;
    }


    @Override
    public AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest) {
        AiProviderConfig mistralConfig = getConfig();

        RestClient restClient = RestClient.builder()
                .baseUrl(mistralConfig.baseUrl())
                .defaultHeader("Authorization", "Bearer " + mistralConfig.apiKey())
                .build();
        System.out.println("Send request de Mistral");

        try {
            return restClient.post()
                    .uri(mistralConfig.endpoint())
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
