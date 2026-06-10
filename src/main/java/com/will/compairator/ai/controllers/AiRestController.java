package com.will.compairator.ai.controllers;

import com.will.compairator.ai.dto.CompairatorChatRequest;
import com.will.compairator.ai.dto.ComparaitorChatResponse;
import com.will.compairator.ai.dto.groq.GroqChatResponse;
import com.will.compairator.ai.dto.mistral.MistralChatResponse;
import com.will.compairator.ai.services.GroqService;
import com.will.compairator.ai.services.MistralService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiRestController {

    private final GroqService groqService;
    private final MistralService mistralService;

    public AiRestController(GroqService groqService,
                            MistralService mistralService) {
        this.groqService = groqService;
        this.mistralService = mistralService;
    }

    @GetMapping
    public String get() {
        System.out.println("Hello World");
        return "OK GET";
    }

    @PostMapping("/chat/groq")
    public ComparaitorChatResponse chatWithGroq(@RequestBody CompairatorChatRequest request) {
        GroqChatResponse groqChatResponse = groqService.chat(request.getPrompt());

        String content = groqChatResponse.getChoices()
                                         .getFirst()
                                         .getMessage()
                                         .getContent();

        return ComparaitorChatResponse.builder()
                                      .content(content)
                                      .build();
    }

    @PostMapping("/chat/mistral")
    public ComparaitorChatResponse chatWithMistral(@RequestBody  CompairatorChatRequest request) {
        MistralChatResponse mistralChatResponse = mistralService.chat(request.getPrompt());

        String content = mistralChatResponse.getChoices()
                .getFirst()
                .getMessage()
                .getContent();

        return ComparaitorChatResponse.builder()
                                      .content(content)
                                      .build();
    }

}
