package com.will.compairator.configuration;

import com.will.compairator.ai.enums.AiProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "ai")
@Validated
public class AiProperties {

    @NotEmpty
    @Valid
    private Map<AiProvider, AiProviderConfig> providers;

    public AiProviderConfig getProviderConfig(AiProvider provider) {
        return this.getProviders().get(provider);
    }

}
