package com.will.compairator.ai.dto.mistral;

import lombok.Data;

import java.util.List;

@Data
public class MistralChatResponseDTO {

    private List<MistralChoiceDTO> choices;

}
