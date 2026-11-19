package com.will.compairator.ai.controllers;

import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.services.AiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.will.compairator.user.services.UserService;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/ai/chat")
public class AiChatRestController {

    private final AiService aiService;
    private final UserService users;

    public AiChatRestController(AiService aiService, UserService users) {
        this.aiService = aiService;
        this.users = users;
    }

    @PostMapping
    public AiChatDTO.PostOutput chat(@Valid @RequestBody AiChatDTO.PostInput input, Principal principal) {
        return aiService.chat(input, users.find(principal.getName()));
    }

}
