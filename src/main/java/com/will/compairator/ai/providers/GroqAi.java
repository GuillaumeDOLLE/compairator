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

public class GroqAi implements IProviderAi {

    @Override
    public AiProvider getProvider() {
        return AiProvider.GROQ;
    }

    @Override
    public AiApiDTO.PostOutput sendRequest(AiChatDTO.PostInput chatInput) {
        AiProviderConfig groqConfig = getConfig();

        AiApiDTO.Message prompt = AiApiDTO.Message.builder()
                .role(AiRole.USER.name().toLowerCase())
                .content(chatInput.prompt())
                .build();

        AiApiDTO.PostInput aiRequest = AiApiDTO.PostInput.builder()
                .model(groqConfig.model())
                .messages(List.of(prompt))
                .build();

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
