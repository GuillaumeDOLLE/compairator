package com.will.compairator.ai.dto;

import com.will.compairator.ai.enums.AiProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;
import java.util.Set;

public class AiCompareDTO {

    @Builder
    public static record PostInput(
            @NotNull
            @Size(min = 2)
            Set<@NotNull AiProvider> providers,

            @NotBlank
            String prompt
    ) {
    }

    @Builder
    public static record AiResponse(
            @NotNull
            AiProvider provider,

            @NotBlank
            String completion,

            @NotBlank
            String model
    ) {
    }

    @Builder
    public static record PostOutput(
            @NotNull
            List<@NotNull AiResponse> output
    ) {
    }

}

