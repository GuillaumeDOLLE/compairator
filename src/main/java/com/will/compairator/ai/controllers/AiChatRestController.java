package com.will.compairator.ai.controllers;

import com.will.compairator.ai.dto.AiChatDTO;
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

    @PostMapping
    public AiChatDTO.PostOutput chat(@Valid @RequestBody AiChatDTO.PostInput input) {
        return aiService.chat(input);
    }

}
