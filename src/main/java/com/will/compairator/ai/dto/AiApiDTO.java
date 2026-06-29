package com.will.compairator.ai.dto;

import lombok.Builder;
import lombok.With;

import java.util.List;

public class AiApiDTO{

    @With
    @Builder
    // The history of the conversation with the AI, provides context,
    // each time the user add a new prompt, the whole conversation is sent again
    public static record Input(String model, List<Message> messages) {
    }

    @With
    @Builder
    public static record Output(List<Choice> choices) {
    }

    @With
    @Builder
    public static record Choice(Integer index, Message message) {
    }

    @With
    @Builder
    public static record Message(String role, String content) {
    }

}
