package com.will.compairator.ai.services;

import com.will.compairator.ai.GroqAi;
import com.will.compairator.ai.MistralAi;
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
    private final MistralAi mistralAi;
    private final GroqAi groqAi;

    public AiService(
            AiProperties aiProperties,
            MistralAi mistralAi,
            GroqAi groqAi) {
        this.aiProperties = aiProperties;
        this.mistralAi = mistralAi;
        this.groqAi = groqAi;
    }

    public AiCompareDTO.PostOutput compare(AiCompareDTO.PostInput compareInput) {

        if (compareInput.providers() == null ||
                compareInput.providers().size() < 2) {
            throw new IllegalArgumentException("At least 2 providers are required");
        }

        List<AiCompareDTO.AiResponse> responses = new ArrayList<>();

        compareInput.providers().forEach(providerName -> {
            AiProviderConfig config = aiProperties.getProviderConfig(providerName);

            AiChatDTO.PostInput chatRequest =
                    AiChatDTO.PostInput.builder()
                            .prompt(compareInput.prompt())
                            .providerName(providerName)
                            .build();

                    AiChatDTO.PostOutput chatResponse =
                            chat(chatRequest);

            responses.add(
                    new AiCompareDTO.AiResponse(
                            providerName,
                            chatResponse.content(),
                            config.getModel()
                    )
            );
        });

        return new AiCompareDTO.PostOutput(List.copyOf(responses));

    }

    public AiChatDTO.PostOutput chat(AiChatDTO.PostInput chatInput) {

        AiProviderConfig config = aiProperties.getProviderConfig(chatInput.providerName());

        AiApiDTO.Input aiInput = buildRequest(chatInput, config);

        if (chatInput.providerName().equals(AiProvider.MISTRAL)) {
            AiApiDTO.Output mistralOutput = mistralAi.sendRequest(aiInput, config);
            String content = mistralOutput.choices()
                    .getFirst()
                    .message()
                    .content();

            return new AiChatDTO.PostOutput(content, config.getModel());

        } else if (chatInput.providerName().equals(AiProvider.GROQ)) {
            AiApiDTO.Output groqOutput = groqAi.sendRequest(aiInput, config);
            String content = groqOutput.choices()
                    .getFirst()
                    .message()
                    .content();

            return new AiChatDTO.PostOutput(content, config.getModel());
        } else {
            throw new IllegalArgumentException("This provider name is incorrect");
        }

    }

    private AiApiDTO.Input buildRequest(AiChatDTO.PostInput chatRequest, AiProviderConfig config) {
        if(chatRequest.prompt() == null || chatRequest.prompt().isBlank()) {
            throw new IllegalArgumentException("A prompt is required");
        }

        AiApiDTO.Message prompt = AiApiDTO.Message.builder()
                // role is necessary to meet the expected format from the AI API
                .role(AiRole.USER.name().toLowerCase())
                .content(chatRequest.prompt())
                .build();


        // version sans constructor, avec le builder
        return AiApiDTO.Input.builder()
                .model(config.getModel())
                .messages(List.of(prompt))
                .build();
    }

}
