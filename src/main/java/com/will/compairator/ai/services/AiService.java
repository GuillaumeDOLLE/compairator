package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.*;
import com.will.compairator.ai.enums.*;
import com.will.compairator.ai.exception.AiProviderInvalidResponseException;
import com.will.compairator.ai.providers.*;
import com.will.compairator.comparison.services.ComparisonService;
import com.will.compairator.comparison.model.ComparisonEntity;
import com.will.compairator.comparison.enums.ComparisonStatus;
import com.will.compairator.user.model.UserEntity;
import org.springframework.cache.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class AiService {
    private final AiApiRequestLogService aiApiRequestLogService;
    private final AiApiRequestCachedService aiApiRequestCachedService;
    private final CacheManager cacheManager;
    private final ComparisonService comparisons;

    public AiService(AiApiRequestLogService aiApiRequestLogService,
                     AiApiRequestCachedService aiApiRequestCachedService,
                     CacheManager cacheManager, ComparisonService comparisons) {
        this.aiApiRequestLogService = aiApiRequestLogService;
        this.aiApiRequestCachedService = aiApiRequestCachedService;
        this.cacheManager = cacheManager;
        this.comparisons = comparisons;
    }

    public AiCompareDTO.PostOutput compare(AiCompareDTO.PostInput input, UserEntity user) {
        ComparisonEntity comparison = comparisons.start(user, input.taskType());
        try {
            List<AiCompareDTO.AiResponse> responses = new ArrayList<>();
            // Stable provider order also makes partial failures reproducible.
            for (AiProvider provider : input.providers().stream().sorted().toList()) {
                AiChatDTO.PostOutput response = chat(new AiChatDTO.PostInput(input.prompt(), provider), user, comparison);
                responses.add(new AiCompareDTO.AiResponse(provider, response.content(), response.model()));
            }
            comparisons.finish(comparison, ComparisonStatus.SUCCESS);
            return new AiCompareDTO.PostOutput(List.copyOf(responses), comparison.getId());
        } catch (RuntimeException ex) {
            comparisons.finish(comparison, ComparisonStatus.ERROR);
            throw ex;
        }
    }

    public AiChatDTO.PostOutput chat(AiChatDTO.PostInput input, UserEntity user) {
        return chat(input, user, null);
    }

    private AiChatDTO.PostOutput chat(AiChatDTO.PostInput input, UserEntity user, ComparisonEntity comparison) {
        long start = System.nanoTime();
        AiApiDTO.PostOutput output = null;
        AiApiResponseOrigin origin = AiApiResponseOrigin.PROVIDER;
        String model = input.provider().name();
        String content;
        try {
            IProviderAi provider = ProviderFactory.getProvider(input.provider());
            model = provider.getConfig().model();
            Cache cache = cacheManager.getCache("aiResponse");
            Cache.ValueWrapper cached = cache == null ? null : cache.get(input);
            if (cached != null) {
                origin = AiApiResponseOrigin.CACHE;
                output = (AiApiDTO.PostOutput) cached.get();
            } else {
                output = aiApiRequestCachedService.sendRequest(provider, input);
            }
            if (output != null && output.model() != null && !output.model().isBlank()) model = output.model();
            if (output == null || output.choices() == null || output.choices().isEmpty()
                    || output.choices().getFirst() == null || output.choices().getFirst().message() == null
                    || output.choices().getFirst().message().content() == null
                    || output.choices().getFirst().message().content().isBlank()) {
                if (cache != null) cache.evict(input);
                throw new AiProviderInvalidResponseException("Provider " + input.provider() + " returned no usable content");
            }
            content = output.choices().getFirst().message().content();
        } catch (RuntimeException ex) {
            aiApiRequestLogService.createAiApiRequestLog(new AiApiRequestObject(input.provider(), model, input.prompt(),
                    null, AiApiRequestStatus.ERROR, ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(),
                    Instant.now(), (System.nanoTime() - start) / 1_000_000, origin),
                    user, comparison, output == null ? null : output.usage());
            throw ex;
        }
        aiApiRequestLogService.createAiApiRequestLog(new AiApiRequestObject(input.provider(), model, input.prompt(),
                content, AiApiRequestStatus.SUCCESS, null, Instant.now(), (System.nanoTime() - start) / 1_000_000, origin),
                user, comparison, output.usage());
        return new AiChatDTO.PostOutput(content, model);
    }
}