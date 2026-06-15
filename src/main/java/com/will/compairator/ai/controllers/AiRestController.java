package com.will.compairator.ai.controllers;

import com.will.compairator.ai.dto.CompairatorDTO;
import com.will.compairator.ai.dto.groq.GroqChatResponseDTO;
import com.will.compairator.ai.dto.mistral.MistralChatResponseDTO;
import com.will.compairator.ai.services.GroqService;
import com.will.compairator.ai.services.MistralService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
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
    public CompairatorDTO.ComparaitorChatResponseDTO chatWithGroq(@RequestBody CompairatorDTO.CompairatorChatRequestDTO request) {
        GroqChatResponseDTO groqChatResponseDto = groqService.chat(request.getPrompt());

        String content = groqChatResponseDto.getChoices()
                                         .getFirst()
                                         .getMessage()
                                         .getContent();

        return CompairatorDTO.ComparaitorChatResponseDTO.builder()
                                      .content(content)
                                      .build();
    }

    @PostMapping("/chat/mistral")
    public CompairatorDTO.ComparaitorChatResponseDTO chatWithMistral(@RequestBody CompairatorDTO.CompairatorChatRequestDTO request) {
        MistralChatResponseDTO mistralChatResponse = mistralService.chat(request.getPrompt());

        String content = mistralChatResponse.getChoices()
                .getFirst()
                .getMessage()
                .getContent();

        return CompairatorDTO.ComparaitorChatResponseDTO.builder()
                                      .content(content)
                                      .build();
    }

    @PostMapping("/compare")
    public String compareView(@RequestParam String prompt, Model model) {

        String groqAnswer = groqService.chat(prompt)
                .getChoices()
                .getFirst()
                .getMessage()
                .getContent();

        String mistralAnswer = mistralService.chat(prompt)
                .getChoices()
                .getFirst()
                .getMessage()
                .getContent();

        // TODO: J'dois surement retourner autre chose qu'un string pour avoir les réponses de toutes les IA
        return mistralAnswer;
    }

}
