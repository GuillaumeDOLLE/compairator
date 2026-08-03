package com.will.compairator.ai.providers;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.configuration.AiProviderConfig;
import com.will.compairator.configuration.AiProviderConfigResolver;

public interface IProviderAi {

    AiProvider getProvider();
    default AiProviderConfig getConfig() {
        AiProviderConfigResolver aiProviderConfigResolver = AiProviderConfigResolver.getInstance();
        return aiProviderConfigResolver.resolve(this.getProvider());
    }
    AiApiDTO.PostOutput sendRequest(AiChatDTO.PostInput chatInput);
}
