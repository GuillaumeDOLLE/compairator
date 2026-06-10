package com.will.compairator.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompairatorChatRequest {

    private String prompt;

}
