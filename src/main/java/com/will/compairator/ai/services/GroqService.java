package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.Message;
import com.will.compairator.ai.dto.groq.GroqChatRequest;
import com.will.compairator.ai.dto.groq.GroqChatResponse;
import com.will.compairator.ai.enums.AiRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GroqService {

    private String apiKey;
    private String groqModel;

    private final RestClient groqRestClient;

    public GroqService(
            @Value("${ai.groq.api-key}") String apiKey,
            @Value("${ai.groq.chat.model}") String groqModel,
            @Qualifier("groq") RestClient groqRestClient) {
        this.apiKey = apiKey;
        this.groqModel = groqModel;
        this.groqRestClient = groqRestClient;
    }

    public GroqChatResponse chat(String prompt) {
        GroqChatRequest request = buildRequest(prompt);
        return sendRequest(request);
    }

    private GroqChatRequest buildRequest(String prompt) {
        Message message = Message.builder()
                                 .role(AiRole.USER.getValue())
                                 .content(prompt)
                                 .build();;

        return GroqChatRequest.builder()
                              .model(groqModel)
                              .messages(List.of(message))
                              .build();

    }
    
    private GroqChatResponse sendRequest(GroqChatRequest request) {
        return groqRestClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(GroqChatResponse.class);
    }

}
