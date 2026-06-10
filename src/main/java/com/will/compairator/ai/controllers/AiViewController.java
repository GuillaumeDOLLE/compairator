package com.will.compairator.ai.controllers;

import com.will.compairator.ai.services.GroqService;
import com.will.compairator.ai.services.MistralService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AiViewController {

    private final GroqService groqService;
    private final MistralService mistralService;

    public AiViewController(GroqService groqService, MistralService mistralService) {
        this.groqService = groqService;
        this.mistralService = mistralService;
    }

    @GetMapping
    public String home() {
        return "compairator-home";
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

        model.addAttribute("prompt", prompt);
        model.addAttribute("groqAnswer", groqAnswer);
        model.addAttribute("mistralAnswer", mistralAnswer);

        return "compairator-home";
    }
}
