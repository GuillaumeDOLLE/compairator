package com.will.compairator.ai.dto.mistral;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.will.compairator.ai.dto.MessageDTO;
import lombok.Data;

@Data
public class MistralChoiceDTO {

    private Integer index;
    private MessageDTO message;
    @JsonProperty("finish_reason")
    private String finishReason;

}