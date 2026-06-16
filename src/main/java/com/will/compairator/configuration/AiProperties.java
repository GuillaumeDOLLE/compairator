package com.will.compairator.configuration;

import com.will.compairator.ai.enums.AiProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private Map<AiProvider, AiProviderConfig> providers;

}
