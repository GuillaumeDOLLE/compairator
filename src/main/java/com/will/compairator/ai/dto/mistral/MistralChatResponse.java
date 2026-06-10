package com.will.compairator.ai.dto.mistral;

import lombok.Data;

import java.util.List;

@Data
public class MistralChatResponse {

    private List<MistralChoice> choices;

}
