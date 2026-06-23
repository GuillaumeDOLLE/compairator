package com.will.compairator.ai.dto;

import lombok.Builder;
import lombok.With;

import java.util.List;

public record AiApiDTO() {

    @With
    @Builder
    // The history of the conversation with the AI, provides context,
    // each time the user add a new prompt, the whole conversation is sent again
    public record InputDTO(String model, List<MessageDTO> messages) {
    }

    @With
    @Builder
    public record OutputDTO(List<ChoiceDTO> choices) {
    }

    @With
    @Builder
    public record ChoiceDTO(Integer index, MessageDTO message) {
    }

    @With
    @Builder
    public record MessageDTO(String role, String content) {
    }

}
