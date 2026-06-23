package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.dto.AiCompareDTO;
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

    public AiCompareDTO.PostOutputListDTO compare(AiCompareDTO.PostInputDTO compareInput) {

        if (compareInput.providers() == null ||
                compareInput.providers().size() < 2) {
            throw new IllegalArgumentException("At least 2 providers are required");
        }

        AiCompareDTO.PostOutputListDTO compareOutput = new AiCompareDTO.PostOutputListDTO(
                new ArrayList<>()
        );

        compareInput.providers().forEach(providerName -> {
            AiProviderConfig config = aiProperties.getProviderConfig(providerName);

            AiChatDTO.PostInputDTO chatRequest =
                    AiChatDTO.PostInputDTO.builder()
                            .prompt(compareInput.prompt())
                            .providerName(providerName)
                            .build();

                    AiChatDTO.PostOutputDTO chatResponse =
                            chat(providerName, chatRequest);

            compareOutput.outputList().add(
                            new AiCompareDTO.PostOutputDTO(
                                    providerName,
                                    chatResponse.content(),
                                    config.getModel()
                            )
                    );
        });

        return new AiCompareDTO.PostOutputListDTO(compareOutput.outputList());

    }

    public AiChatDTO.PostOutputDTO chat(AiProvider providerName, AiChatDTO.PostInputDTO chatInput) {
        AiProviderConfig config = aiProperties.getProviderConfig(providerName);

        AiApiDTO.InputDTO aiInput = buildRequest(chatInput, config);

        AiApiDTO.OutputDTO aiOutput = sendRequest(aiInput, config);

        String content = aiOutput.choices()
                .getFirst()
                .message()
                .content();
        // version avec constructor
        return new AiChatDTO.PostOutputDTO(content, config.getModel());
    }



    private AiApiDTO.InputDTO buildRequest(AiChatDTO.PostInputDTO chatRequest, AiProviderConfig config) {
        if(chatRequest.prompt() == null || chatRequest.prompt().isBlank()) {
            throw new IllegalArgumentException("A prompt is required");
        }

        AiApiDTO.MessageDTO prompt = AiApiDTO.MessageDTO.builder()
                // role is necessary to meet the expected format from the AI API
                .role(AiRole.USER.name().toLowerCase())
                .content(chatRequest.prompt())
                .build();


        // version sans constructor, avec le builder
        return AiApiDTO.InputDTO.builder()
                .model(config.getModel())
                .messages(List.of(prompt))
                .build();
    }

    private RestClient buildRestClient(AiProviderConfig config) {
        return restClientBuilder
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build();
    }

    private AiApiDTO.OutputDTO sendRequest(AiApiDTO.InputDTO aiRequest, AiProviderConfig config) {
        RestClient restClient = buildRestClient(config);

        return restClient.post()
                .uri(config.getEndpoint())
                .body(aiRequest)
                .retrieve()
                .body(AiApiDTO.OutputDTO.class);
    }
}
