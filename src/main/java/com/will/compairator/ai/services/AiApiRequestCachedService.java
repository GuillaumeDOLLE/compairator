package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.providers.IProviderAi;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AiApiRequestCachedService {

    @Cacheable(value = "aiResponse", key = "#chatInput")
    public AiApiDTO.PostOutput sendRequest(IProviderAi providerAi, AiChatDTO.PostInput chatInput) {
        System.out.println("Appel réel au provider");
        return providerAi.sendRequest(chatInput);
    }

}
