package com.will.compairator.ai.services;

import com.will.compairator.ai.GroqAi;
import com.will.compairator.ai.MistralAi;
import com.will.compairator.ai.ProviderAi;
import com.will.compairator.ai.ProviderFactory;
import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.dto.AiCompareDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.enums.AiRole;
import com.will.compairator.configuration.AiProperties;
import com.will.compairator.configuration.AiProviderConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    private final AiProperties aiProperties;
    private final ProviderFactory providerFactory;

    public AiService(
            AiProperties aiProperties,
            ProviderFactory providerFactory) {
        this.aiProperties = aiProperties;
        this.providerFactory = providerFactory;
    }

    public AiCompareDTO.PostOutput compare(AiCompareDTO.PostInput compareInput) {

        if (compareInput.providers() == null ||
                compareInput.providers().size() < 2) {
            throw new IllegalArgumentException("At least 2 providers are required");
        }

        List<AiCompareDTO.AiResponse> responses = new ArrayList<>();

        compareInput.providers().forEach(provider -> {

            AiChatDTO.PostInput chatRequest =
                    AiChatDTO.PostInput.builder()
                            .prompt(compareInput.prompt())
                            .provider(provider)
                            .build();

                    AiChatDTO.PostOutput chatResponse =
                            chat(chatRequest);

            responses.add(
                    new AiCompareDTO.AiResponse(
                            provider,
                            chatResponse.content(),
                            chatResponse.model()
                    )
            );
        });

        return new AiCompareDTO.PostOutput(List.copyOf(responses));

    }

    public AiChatDTO.PostOutput chat(AiChatDTO.PostInput chatInput) {

        AiProviderConfig providerConfig = aiProperties.getProviderConfig(chatInput.provider());

        AiApiDTO.PostInput aiInput = buildRequest(chatInput, providerConfig);

            ProviderAi providerAi = providerFactory.getProvider(chatInput.provider());
            AiApiDTO.PostOutput aiOutput = providerAi.sendRequest(aiInput);
            String content = aiOutput.choices()
                    .getFirst()
                    .message()
                    .content();
            return new AiChatDTO.PostOutput(content, providerConfig.getModel());

    }

    private AiApiDTO.PostInput buildRequest(AiChatDTO.PostInput chatRequest, AiProviderConfig providerConfig) {
        if(chatRequest.prompt() == null || chatRequest.prompt().isBlank()) {
            throw new IllegalArgumentException("A prompt is required");
        }

        AiApiDTO.Message prompt = AiApiDTO.Message.builder()
                // role is necessary to meet the expected format from the AI API
                .role(AiRole.USER.name().toLowerCase())
                .content(chatRequest.prompt())
                .build();


        // version sans constructor, avec le builder
        return AiApiDTO.PostInput.builder()
                .model(providerConfig.getModel())
                .messages(List.of(prompt))
                .build();
    }

}
