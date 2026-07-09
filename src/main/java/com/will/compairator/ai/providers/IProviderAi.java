package com.will.compairator.ai.providers;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.AiProvider;

public interface IProviderAi {

    public AiProvider getProvider();

    public AiApiDTO.PostOutput sendRequest(AiApiDTO.PostInput aiRequest);
}
