package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.dto.CompairatorDTO;
import com.will.compairator.ai.dto.MessageDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.enums.AiRole;
import com.will.compairator.configuration.AiProperties;
import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    private final RestClient.Builder restClientBuilder;
    private final AiProperties aiProperties;

    public AiService(RestClient.Builder restClientBuilder, AiProperties aiProperties) {
        this.restClientBuilder = restClientBuilder;
        this.aiProperties = aiProperties;
    }

    public List<CompairatorDTO.CompareResponseDTO> compare(CompairatorDTO.CompareRequestDTO promptRequest) {

        if (promptRequest.getProviders() == null || promptRequest.getProviders().isEmpty()) {
            throw new IllegalArgumentException("At least one provider is required");
        }

        return promptRequest.getProviders().stream()
                .map(provider -> {
                        CompairatorDTO.ChatRequestDTO chatRequest =
                                new CompairatorDTO.ChatRequestDTO(
                                        promptRequest.getPrompt()
                                );

                        CompairatorDTO.ChatResponseDTO chatResponse =
                                chat(AiProvider.valueOf(provider), chatRequest);

                        return new CompairatorDTO.CompareResponseDTO(
                                provider,
                                chatResponse.getContent()
                        );
                })
                .toList();
    }

    public CompairatorDTO.ChatResponseDTO chat(AiProvider provider, CompairatorDTO.ChatRequestDTO promptRequest) {
        AiProviderConfig config = getProviderConfig(provider);

        AiChatDTO.RequestDTO aiRequest = buildRequest(promptRequest, config);

        AiChatDTO.ResponseDTO aiResponse = sendRequest(aiRequest, config);

        String content = aiResponse.getChoices()
                .getFirst()
                .getMessage()
                .getContent();
        // version avec constructor
        return new CompairatorDTO.ChatResponseDTO(content);
    }

    private AiProviderConfig getProviderConfig(AiProvider provider) {
        AiProviderConfig config = aiProperties.getProviders().get(provider);

        if (config == null) {
            throw new IllegalArgumentException("The provider " + provider + " is not configured");
        }

        return config;
    }

    private AiChatDTO.RequestDTO buildRequest(CompairatorDTO.ChatRequestDTO promptRequest, AiProviderConfig config) {
//        if(promptRequest.getPrompt() == null || promptRequest.getPrompt().isBlank()) {
//            throw new IllegalArgumentException("A prompt is required");
//        }

        MessageDTO message = MessageDTO.builder()
                .role(AiRole.USER.name().toLowerCase())
                .content(promptRequest.getPrompt())
                .build();


        // version sans constructor, avec le builder
        return AiChatDTO.RequestDTO.builder()
                .model(config.getModel())
                .messages(List.of(message))
                .build();
    }

    private AiChatDTO.ResponseDTO sendRequest(AiChatDTO.RequestDTO aiRequest, AiProviderConfig config) {
        RestClient restClient = restClientBuilder
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

        return restClient.post()
                .uri(config.getEndpoint())
                .body(aiRequest)
                .retrieve()
                .body(AiChatDTO.ResponseDTO.class);
    }
}
