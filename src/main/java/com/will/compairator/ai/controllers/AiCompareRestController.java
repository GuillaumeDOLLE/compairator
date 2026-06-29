package com.will.compairator.ai.controllers;

import com.will.compairator.ai.dto.AiCompareDTO;
import com.will.compairator.ai.services.AiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ai/compare")
public class AiCompareRestController {

    private final AiService aiService;

    public AiCompareRestController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public AiCompareDTO.PostOutput compareWithAi(@Valid @RequestBody AiCompareDTO.PostInput prompt) {
        return aiService.compare(prompt);
    }

}
