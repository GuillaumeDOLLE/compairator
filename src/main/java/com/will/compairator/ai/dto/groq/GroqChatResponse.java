package com.will.compairator.ai.dto.groq;

import lombok.Data;

import java.util.List;

@Data
public class GroqChatResponse {

    private List<GroqChoice> choices;

}
