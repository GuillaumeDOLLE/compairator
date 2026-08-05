package com.will.compairator.ai.services;

import com.will.compairator.ai.enums.AiApiRequestStatus;
import com.will.compairator.ai.enums.AiApiResponseOrigin;
import com.will.compairator.ai.exception.AiProviderInvalidResponseException;
import com.will.compairator.ai.providers.IProviderAi;
import com.will.compairator.ai.providers.ProviderFactory;
import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.dto.AiChatDTO;
import com.will.compairator.ai.dto.AiCompareDTO;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    private final AiApiRequestLogService aiApiRequestLogService;
    private final AiApiRequestCachedService aiApiRequestCachedService;
    private final CacheManager cacheManager;

    public AiService(AiApiRequestLogService aiApiRequestLogService, AiApiRequestCachedService aiApiRequestCachedService, CacheManager cacheManager) {
        this.aiApiRequestLogService = aiApiRequestLogService;
        this.aiApiRequestCachedService = aiApiRequestCachedService;
        this.cacheManager = cacheManager;
    }


    public AiCompareDTO.PostOutput compare(AiCompareDTO.PostInput compareInput) {

        List<AiCompareDTO.AiResponse> responses = new ArrayList<>();

        compareInput.providers().forEach(provider -> {

            AiChatDTO.PostInput chatRequest =
                    AiChatDTO.PostInput.builder()
                            .prompt(compareInput.prompt())
                            .provider(provider)
                            .build();

                    AiChatDTO.PostOutput chatResponse =
                            chat(chatRequest);

            responses.add(
                    new AiCompareDTO.AiResponse(
                            provider,
                            chatResponse.content(),
                            chatResponse.model()
                    )
            );
        });

        return new AiCompareDTO.PostOutput(List.copyOf(responses));
    }

    public AiChatDTO.PostOutput chat(AiChatDTO.PostInput chatInput) {

        IProviderAi providerAi = ProviderFactory.getProvider(chatInput.provider());

        long startTime = System.nanoTime();

        try {
            Cache potentialCache = cacheManager.getCache("aiResponse");
            Cache.ValueWrapper cachedValue = potentialCache == null ? null : potentialCache.get(chatInput);
            AiApiDTO.PostOutput aiOutput = null;
            long durationMs = 0;
            AiApiResponseOrigin aiApiResponseOrigin = null;

            if (cachedValue != null) {

                aiOutput = (AiApiDTO.PostOutput) cachedValue.get();
                // duration of the response after the request was sent
                durationMs = (System.nanoTime() - startTime) / 1_000_000;
                aiApiResponseOrigin = AiApiResponseOrigin.CACHE;

            } else {

                aiOutput = aiApiRequestCachedService.sendRequest(providerAi, chatInput);
                durationMs = (System.nanoTime() - startTime) / 1_000_000;
                aiApiResponseOrigin = AiApiResponseOrigin.PROVIDER;

            }

            String errorMessage = null;

            if (aiOutput == null || CollectionUtils.isEmpty(aiOutput.choices())) {
                errorMessage = "Provider " + chatInput.provider() + " returned no usable choices";
                throw new AiProviderInvalidResponseException(errorMessage);
            }

            // Sometimes the AI respond with 2 choices, here we choose the first one
            AiApiDTO.Choice firstChoice = aiOutput.choices().getFirst();
            if (firstChoice == null
                    || firstChoice.message() == null
                    || firstChoice.message().content().isBlank()) {
                errorMessage = "Provider " + chatInput.provider() + " returned a choice without usable message content";
                throw new AiProviderInvalidResponseException(errorMessage);
            }

            AiApiRequestObject aiApiRequestObject = new AiApiRequestObject(
                    chatInput.provider(),
                    providerAi.getConfig().model(),
                    chatInput.prompt(),
                    firstChoice.message().content(),
                    AiApiRequestStatus.SUCCESS,
                    errorMessage,
                    Instant.now(),
                    durationMs,
                    aiApiResponseOrigin
            );

            aiApiRequestLogService.createAiApiRequestLog(aiApiRequestObject);

            return new AiChatDTO.PostOutput(aiOutput.choices()
                    .getFirst()
                    .message()
                    .content(), providerAi.getConfig().model());
        } catch (RuntimeException exception) {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            AiApiRequestObject aiApiRequestObject = new AiApiRequestObject(
                    chatInput.provider(),
                    providerAi.getConfig().model(),
                    chatInput.prompt(),
                    null,
                    AiApiRequestStatus.ERROR,
                    exception.getMessage(),
                    Instant.now(),
                    durationMs,
                    AiApiResponseOrigin.PROVIDER
            );

            aiApiRequestLogService.createAiApiRequestLog(aiApiRequestObject);

            throw exception;
        }

    }

}
