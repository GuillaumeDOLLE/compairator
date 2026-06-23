package com.will.compairator.ai.dto;

import com.will.compairator.ai.enums.AiProvider;
import lombok.Builder;
import lombok.With;

public record AiChatDTO() {

    @With
    @Builder
    public record PostInputDTO(String prompt, AiProvider providerName) {
    }

    @With
    @Builder
    public record PostOutputDTO(String content, String model) {
    }

}
