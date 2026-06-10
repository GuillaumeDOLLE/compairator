package com.will.compairator.ai.dto.mistral;
import com.will.compairator.ai.dto.Message;
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
public class MistralChatRequest {

    @NotNull @NotBlank
    private String model;

    @NotNull @NotBlank
    private List<Message> messages;
}
