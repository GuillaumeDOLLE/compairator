package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class TokenCostServiceTest {
    @Test void pricingRequiresUsageMatchingModelAndBothRates() {
        MockEnvironment env = new MockEnvironment();
        TokenCostService service = new TokenCostService(env);
        AiApiDTO.Usage usage = new AiApiDTO.Usage(100L, 200L);
        assertNull(service.estimate(AiProvider.GROQ, "model", usage, AiApiResponseOrigin.PROVIDER));
        env.setProperty("ai.pricing.groq.model", "model");
        env.setProperty("ai.pricing.groq.input-per-million", "1");
        env.setProperty("ai.pricing.groq.output-per-million", "2");
        assertEquals(0, new BigDecimal("0.0005").compareTo(service.estimate(AiProvider.GROQ, "model", usage, AiApiResponseOrigin.PROVIDER)));
        assertNull(service.estimate(AiProvider.GROQ, "different-model", usage, AiApiResponseOrigin.PROVIDER));
        assertNull(service.estimate(AiProvider.GROQ, "model", null, AiApiResponseOrigin.PROVIDER));
        assertNull(service.estimate(AiProvider.GROQ, "model", new AiApiDTO.Usage(-1L, 0L), AiApiResponseOrigin.PROVIDER));
        assertEquals(BigDecimal.ZERO, service.estimate(AiProvider.GROQ, "model", null, AiApiResponseOrigin.CACHE));
    }
}
