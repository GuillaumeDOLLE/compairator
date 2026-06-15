package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.MessageDTO;
import com.will.compairator.ai.dto.mistral.MistralChatRequestDTO;
import com.will.compairator.ai.dto.mistral.MistralChatResponseDTO;
import com.will.compairator.ai.enums.AiRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class MistralService {

    private String apiKey;
    private String mistralModel;

    private final RestClient mistralRestClient;

    public MistralService(
            @Value("${ai.mistral.api-key}") String apiKey,
            @Value("${ai.mistral.chat.model}") String mistralModel,
            @Qualifier("mistral") RestClient mistralRestClient) {
        this.apiKey = apiKey;
        this.mistralModel = mistralModel;
        this.mistralRestClient = mistralRestClient;
    }

    public MistralChatResponseDTO chat(String prompt) {
        MistralChatRequestDTO request = buildRequest(prompt);
        return sendRequest(request);
    }

    private MistralChatRequestDTO buildRequest(String prompt) {
        MessageDTO message = MessageDTO.builder()
                .role(AiRole.USER.name().toLowerCase())
                .content(prompt)
                .build();;

        return MistralChatRequestDTO.builder()
                .model(mistralModel)
                .messages(List.of(message))
                .build();

    }

    private MistralChatResponseDTO sendRequest(MistralChatRequestDTO request) {
        return mistralRestClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(MistralChatResponseDTO.class);
    }

}
