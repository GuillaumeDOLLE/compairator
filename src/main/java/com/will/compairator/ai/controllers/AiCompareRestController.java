package com.will.compairator.ai.controllers;

import com.will.compairator.ai.dto.AiCompareDTO;
import com.will.compairator.ai.services.AiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.will.compairator.user.services.UserService;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/ai/compare")
public class AiCompareRestController {

    private final AiService aiService;
    private final UserService users;

    public AiCompareRestController(AiService aiService, UserService users) {
        this.aiService = aiService;
        this.users = users;
    }

    @PostMapping
    public AiCompareDTO.PostOutput compare(@Valid @RequestBody AiCompareDTO.PostInput prompt, Principal principal) {
        return aiService.compare(prompt, users.find(principal.getName()));
    }

}
