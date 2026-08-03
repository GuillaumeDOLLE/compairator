package com.will.compairator.ai.providers;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.enums.AiRole;
import com.will.compairator.ai.exception.AiProviderCallException;
import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

public class MistralAi implements IProviderAi {

    @Override
    public AiProvider getProvider() {
        return AiProvider.MISTRAL;
    }


    @Override
    public AiApiDTO.PostOutput sendRequest(AiChatDTO.PostInput chatInput) {
        AiProviderConfig mistralConfig = getConfig();

        AiApiDTO.Message prompt = AiApiDTO.Message.builder()
                // role is necessary to meet the expected format from the AI API
                .role(AiRole.USER.name().toLowerCase())
                .content(chatInput.prompt())
                .build();

        AiApiDTO.PostInput aiRequest = AiApiDTO.PostInput.builder()
                .model(mistralConfig.model())
                .messages(List.of(prompt))
                .build();

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
