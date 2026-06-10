package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.Message;
import com.will.compairator.ai.dto.mistral.MistralChatRequest;
import com.will.compairator.ai.dto.mistral.MistralChatResponse;
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

    public MistralChatResponse chat(String prompt) {
        MistralChatRequest request = buildRequest(prompt);
        return sendRequest(request);
    }

    private MistralChatRequest buildRequest(String prompt) {
        Message message = Message.builder()
                .role(AiRole.USER.getValue())
                .content(prompt)
                .build();;

        return MistralChatRequest.builder()
                .model(mistralModel)
                .messages(List.of(message))
                .build();

    }

    private MistralChatResponse sendRequest(MistralChatRequest request) {
        return mistralRestClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(MistralChatResponse.class);
    }

}
