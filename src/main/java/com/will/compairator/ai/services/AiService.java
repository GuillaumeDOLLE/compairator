package com.will.compairator.ai.services;

import com.will.compairator.ai.exception.AiProviderInvalidResponseException;
import com.will.compairator.ai.providers.IProviderAi;
import com.will.compairator.ai.providers.ProviderFactory;
import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.dto.AiCompareDTO;
import com.will.compairator.ai.enums.AiRole;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    public AiCompareDTO.PostOutput compare(AiCompareDTO.PostInput compareInput) {

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

        IProviderAi providerAi = ProviderFactory.getProvider(chatInput.provider());

        String providerModel = providerAi.getModel();

        AiApiDTO.PostInput aiInput = buildRequest(chatInput, providerModel);

        AiApiDTO.PostOutput aiOutput = providerAi.sendRequest(aiInput);

        if (aiOutput == null || CollectionUtils.isEmpty(aiOutput.choices())) {
            throw new AiProviderInvalidResponseException("Provider " + chatInput.provider() + " returned no usable choices");
        }

        // Sometimes the AI respond with 2 choices, here we choose the first one
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
        return new AiChatDTO.PostOutput(content, providerModel);
    }

    private AiApiDTO.PostInput buildRequest(AiChatDTO.PostInput chatRequest, String providerModel) {

        AiApiDTO.Message prompt = AiApiDTO.Message.builder()
                // role is necessary to meet the expected format from the AI API
                .role(AiRole.USER.name().toLowerCase())
                .content(chatRequest.prompt())
                .build();

        // builder version
        return AiApiDTO.PostInput.builder()
                .model(providerModel)
                .messages(List.of(prompt))
                .build();
    }

}
