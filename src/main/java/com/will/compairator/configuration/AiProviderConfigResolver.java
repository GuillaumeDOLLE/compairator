package com.will.compairator.configuration;

import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.InvalidProviderConfigurationException;
import io.micrometer.common.util.StringUtils;

import java.util.EnumMap;
import java.util.Map;

public final class AiProviderConfigResolver {

    private static final AiProviderConfigResolver INSTANCE = new AiProviderConfigResolver();
    private final Map<AiProvider, AiProviderConfig> providers;

    public static AiProviderConfigResolver getInstance() {
        return INSTANCE;
    }

    public AiProviderConfigResolver() {
        ProviderPropertyReader providerPropertyReader = new ProviderPropertyReader();
        Map<String, String> providersProperties = providerPropertyReader.getApplicationAiProperties();
        Map<AiProvider, AiProviderConfig> tempMap = new EnumMap<>(AiProvider.class);

        // loop on enum
        for (AiProvider provider : AiProvider.values()) {
            String providerNameToLowerCase = provider.name().toLowerCase();

            // prefix ai.providers.(providerName).
            String keyPrefix = ProviderPropertyReader.AI_PROVIDER_PROPERTY_PREFIX + providerNameToLowerCase + ".";

            String apiKey = providersProperties.get(keyPrefix + "api-key");
            String baseUrl = providersProperties.get(keyPrefix + "base-url");
            String model = providersProperties.get(keyPrefix + "model");
            String endpoint = providersProperties.get(keyPrefix + "endpoint");



            if (StringUtils.isBlank(apiKey)) {
                throw new InvalidProviderConfigurationException("Invalid configuration for provider " + provider + ": missing api key");
            }
            if (StringUtils.isBlank(baseUrl)) {
                throw new InvalidProviderConfigurationException("Invalid configuration for provider " + provider + ": missing base url");
            }
            if (StringUtils.isBlank(model)) {
                throw new InvalidProviderConfigurationException("Invalid configuration for provider " + provider + ": missing model");
            }
            if (StringUtils.isBlank(endpoint)) {
                throw new InvalidProviderConfigurationException("Invalid configuration for provider " + provider + ": missing endpoint");
            }

            AiProviderConfig config = new AiProviderConfig(apiKey, baseUrl, model, endpoint);

            tempMap.put(provider, config);
        }
        // Immutable copy of tempMap
        this.providers = Map.copyOf(tempMap);
    }

    public AiProviderConfig resolve(AiProvider provider) {

        AiProviderConfig config = providers.get(provider);

        if (config == null) {
            throw new InvalidProviderConfigurationException("Invalid configuration for provider " + provider);
        }

        return config;

    }

}
