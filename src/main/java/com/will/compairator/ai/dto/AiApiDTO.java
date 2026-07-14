package com.will.compairator.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

public class AiApiDTO {

    @Builder
    // The history of the conversation with the AI, provides context,
    // each time the user add a new prompt, the whole conversation is sent again
    public static record PostInput(
            @NotBlank
            String model,

            @NotNull
            List<@NotNull Message> messages
    ) {
    }

    @Builder
    public static record PostOutput(
            List<Choice> choices
    ) {
    }

    @Builder
    public static record Choice(
            Integer index,
            Message message
    ) {
    }

    @Builder
    public static record Message(
            @NotBlank
            String role,

            @NotBlank
            String content
    ) {
    }

}
