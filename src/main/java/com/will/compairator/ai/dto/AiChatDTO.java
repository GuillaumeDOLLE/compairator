package com.will.compairator.ai.dto;

import com.will.compairator.ai.enums.AiProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class AiChatDTO {

    @Builder
    public static record PostInput(
            @NotBlank
            String prompt,

            @NotNull
            AiProvider provider
    ) {
    }

    @Builder
    public static record PostOutput(
            @NotBlank
            String content,

            @NotBlank
            String model
    ) {
    }

}
