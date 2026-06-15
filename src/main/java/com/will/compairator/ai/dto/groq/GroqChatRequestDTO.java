package com.will.compairator.ai.dto.groq;

import com.will.compairator.ai.dto.MessageDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroqChatRequestDTO {

    @NotNull @NotBlank
    private String model;

    @NotNull @NotBlank
    private List<MessageDTO> messages;

}
