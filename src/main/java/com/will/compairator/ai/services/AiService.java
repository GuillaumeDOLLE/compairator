package com.will.compairator.ai.services;

import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.AiProviderInvalidResponseException;
import com.will.compairator.ai.exception.InvalidComparisonException;
import com.will.compairator.ai.providers.IProviderAi;
import com.will.compairator.ai.providers.ProviderFactory;
import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.dto.AiCompareDTO;
import com.will.compairator.ai.enums.AiRole;
import com.will.compairator.configuration.AiProviderConfig;
import com.will.compairator.configuration.AiProviderConfigResolver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AiService {

    private final AiProviderConfigResolver aiProviderConfigResolver;

    public AiService(
            AiProviderConfigResolver aiProviderConfigResolver) {
        this.aiProviderConfigResolver = aiProviderConfigResolver;
    }

    public AiCompareDTO.PostOutput compare(AiCompareDTO.PostInput compareInput) {

        Set<AiProvider> encounteredProviders = new HashSet<>();

        // Check duplicates with the Set
        for (AiProvider provider : compareInput.providers()) {
            if (!encounteredProviders.add(provider)) {
                throw new InvalidComparisonException(
                        "Provider " + provider + " can't be selected more than once !"
                );
            }
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

        AiProviderConfig providerConfig = aiProviderConfigResolver.resolve(chatInput.provider());

        AiApiDTO.PostInput aiInput = buildRequest(chatInput, providerConfig);

        IProviderAi providerAi = ProviderFactory.getProvider(chatInput.provider(), providerConfig);

        AiApiDTO.PostOutput aiOutput = providerAi.sendRequest(aiInput);

        if (aiOutput == null
                || aiOutput.choices() == null
                || aiOutput.choices().isEmpty()) {
            throw new AiProviderInvalidResponseException("Provider " + chatInput.provider() + " returned no usable choices");
        }

        AiApiDTO.Choice firstChoice = aiOutput.choices().getFirst();
        if (firstChoice == null
                || firstChoice.message() == null
                || firstChoice.message().content().isBlank()) {
            throw new AiProviderInvalidResponseException("Provider " + chatInput.provider() + " returned a choice without usable message content");
        }

        String content = aiOutput.choices()
                .getFirst()
                .message()
                .content();
        return new AiChatDTO.PostOutput(content, providerConfig.getModel());

    }

    private AiApiDTO.PostInput buildRequest(AiChatDTO.PostInput chatRequest, AiProviderConfig providerConfig) {
        if(chatRequest.prompt() == null || chatRequest.prompt().isBlank()) {
            throw new InvalidComparisonException("A prompt is required");
        }

        AiApiDTO.Message prompt = AiApiDTO.Message.builder()
                // role is necessary to meet the expected format from the AI API
                .role(AiRole.USER.name().toLowerCase())
                .content(chatRequest.prompt())
                .build();

        // builder version
        return AiApiDTO.PostInput.builder()
                .model(providerConfig.getModel())
                .messages(List.of(prompt))
                .build();
    }

}
