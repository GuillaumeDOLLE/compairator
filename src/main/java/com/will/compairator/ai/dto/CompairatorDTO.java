package com.will.compairator.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class CompairatorDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatRequestDTO {

        @NotNull
        @NotBlank
        private String prompt;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatResponseDTO {

        private String content;

    }

    @Data
    public static class CompareRequestDTO {

        @NotEmpty
        private List<String> providers;
        @NotNull
        @NotBlank
        private String prompt;

    }

    @Data
    @AllArgsConstructor
    public static class CompareResponseDTO {

        private String provider;
        private String content;

    }

}
