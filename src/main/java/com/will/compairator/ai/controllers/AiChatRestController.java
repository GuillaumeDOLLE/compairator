package com.will.compairator.ai.controllers;

import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.services.AiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ai/chat")
public class AiChatRestController {

    private final AiService aiService;

    public AiChatRestController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping
    public String get() {
        System.out.println("Hello World");
        return "OK GET";
    }

    @PostMapping
    public AiChatDTO.PostOutputDTO chatWithAi(@Valid @RequestBody AiChatDTO.PostInputDTO input) {
        return aiService.chat(input.providerName(), input);
    }

}
