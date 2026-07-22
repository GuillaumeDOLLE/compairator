package com.will.compairator.ai.providers;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;

public interface IProviderAi {

    AiProvider getProvider();
    String getModel();
    AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest);
}
