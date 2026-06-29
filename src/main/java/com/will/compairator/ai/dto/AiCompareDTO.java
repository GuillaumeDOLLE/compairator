package com.will.compairator.ai.dto;

import com.will.compairator.ai.enums.AiProvider;
import lombok.Builder;
import lombok.With;

import java.util.List;

public class AiCompareDTO {

    @With
    @Builder
    public static record PostInput(List<AiProvider> providers, String prompt) {
    }

    @With
    @Builder
    public static record AiResponse(AiProvider provider, String completion, String model) {
    }

    @With
    @Builder
    public static record PostOutput(List<AiResponse> output) {
    }


}

