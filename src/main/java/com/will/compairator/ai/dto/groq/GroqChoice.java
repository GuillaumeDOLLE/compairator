package com.will.compairator.ai.dto.groq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.will.compairator.ai.dto.Message;
import lombok.Data;

@Data
public class GroqChoice {

    private Integer index;
    private Message message;
    @JsonProperty("finish_reason")
    private String finishReason;

}
