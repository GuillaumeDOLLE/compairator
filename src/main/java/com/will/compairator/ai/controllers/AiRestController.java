package com.will.compairator.ai.controllers;

import com.will.compairator.ai.dto.CompairatorDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.services.AiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiRestController {

    private final AiService aiService;

    public AiRestController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping
    public String get() {
        System.out.println("Hello World");
        return "OK GET";
    }

    @PostMapping("/chat/{provider}")
    public CompairatorDTO.ChatResponseDTO chatWithAi(
            @PathVariable String provider,
            @Valid @RequestBody CompairatorDTO.ChatRequestDTO promptRequest) {
        AiProvider aiProvider = AiProvider.valueOf(provider.toUpperCase());
        return aiService.chat(aiProvider, promptRequest);
    }

    @PostMapping("/chat/compare")
    public List<CompairatorDTO.CompareResponseDTO> compare(@Valid @RequestBody CompairatorDTO.CompareRequestDTO promptRequest) {
        return aiService.compare(promptRequest);
    }

}
