package com.will.compairator.ai.dto;

import com.will.compairator.ai.enums.AiProvider;
import lombok.Builder;
import lombok.With;

import java.util.List;

public record AiCompareDTO() {

    @With
    @Builder
    public record PostInputDTO(List<AiProvider> providers, String prompt) {
    }

    @With
    @Builder
    public record PostOutputDTO(AiProvider provider, String completion, String model) {
    }

    @With
    @Builder
    public record PostOutputListDTO(List<PostOutputDTO> outputList) {
    }


}

