package com.will.compairator.ai.dto;

import com.will.compairator.ai.enums.AiProvider;
import lombok.Builder;
import lombok.With;

public class AiChatDTO {

    @With
    @Builder
    public static record PostInput(String prompt, AiProvider providerName) {
    }

    @With
    @Builder
    public static record PostOutput(String content, String model) {
    }

}
