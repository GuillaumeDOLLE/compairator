package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.MessageDTO;
import com.will.compairator.ai.dto.groq.GroqChatRequestDTO;
import com.will.compairator.ai.dto.groq.GroqChatResponseDTO;
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

    public GroqChatResponseDTO chat(String prompt) {
        GroqChatRequestDTO request = buildRequest(prompt);
        return sendRequest(request);
    }

    private GroqChatRequestDTO buildRequest(String prompt) {
        MessageDTO message = MessageDTO.builder()
                                 .role(AiRole.USER.name().toLowerCase())
                                 .content(prompt)
                                 .build();

        return GroqChatRequestDTO.builder()
                              .model(groqModel)
                              .messages(List.of(message))
                              .build();

    }
    
    private GroqChatResponseDTO sendRequest(GroqChatRequestDTO request) {
        return groqRestClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(GroqChatResponseDTO.class);
    }

}
