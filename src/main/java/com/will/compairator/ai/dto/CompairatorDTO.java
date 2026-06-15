package com.will.compairator.ai.dto;

import lombok.Builder;
import lombok.Data;

public class CompairatorDTO {

    @Data
    @Builder
    public static class CompairatorChatRequestDTO {

        private String prompt;

    }

    @Data
    @Builder
    public static class ComparaitorChatResponseDTO {

        private String content;

    }

}
