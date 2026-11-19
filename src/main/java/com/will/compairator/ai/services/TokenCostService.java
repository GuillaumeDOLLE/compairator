package com.will.compairator.ai.services;

import com.will.compairator.ai.dto.AiApiDTO;
import com.will.compairator.ai.enums.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.math.*;
import java.util.Locale;

@Service
public class TokenCostService {
    private final Environment environment;
    public TokenCostService(Environment environment) { this.environment = environment; }

    public BigDecimal estimate(AiProvider provider, String model, AiApiDTO.Usage usage, AiApiResponseOrigin origin) {
        if (origin == AiApiResponseOrigin.CACHE) return BigDecimal.ZERO;
        if (usage == null || usage.promptTokens() == null || usage.completionTokens() == null
                || usage.promptTokens() < 0 || usage.completionTokens() < 0) return null;
        String prefix = "ai.pricing." + provider.name().toLowerCase(Locale.ROOT) + ".";
        if (!model.equals(environment.getProperty(prefix + "model"))) return null;
        BigDecimal input = environment.getProperty(prefix + "input-per-million", BigDecimal.class);
        BigDecimal output = environment.getProperty(prefix + "output-per-million", BigDecimal.class);
        if (input == null || output == null || input.signum() < 0 || output.signum() < 0) return null;
        return input.multiply(BigDecimal.valueOf(usage.promptTokens()))
                .add(output.multiply(BigDecimal.valueOf(usage.completionTokens())))
                .divide(BigDecimal.valueOf(1_000_000), 12, RoundingMode.HALF_UP);
    }
}
