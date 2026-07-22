package com.will.compairator.configuration;

import com.will.compairator.ai.enums.AiProvider;
import com.will.compairator.ai.exception.InvalidProviderConfigurationException;

import java.util.EnumMap;
import java.util.Map;

public final class AiProviderConfigResolver {

    private static final AiProviderConfigResolver INSTANCE = new AiProviderConfigResolver();
    private final Map<AiProvider, AiProviderConfig> providers;

    public static AiProviderConfigResolver getInstance() {
        return INSTANCE;
    }

    private AiProviderConfigResolver() {
        ProviderPropertyReader providerPropertyReader = new ProviderPropertyReader();
        Map<String, String> providersProperties = providerPropertyReader.readProperties();
        Map<AiProvider, AiProviderConfig> tempMap = new EnumMap<>(AiProvider.class);

        // loop on enum
        for (AiProvider providerName : AiProvider.values()) {
            String providerNameToLowerCase = providerName.name().toLowerCase();

            // prefix ai.providers.(providerName).
            String keyPrefix = ProviderPropertyReader.AI_PROVIDER_PROPERTY_PREFIX + providerNameToLowerCase + ".";

            String apiKey = providersProperties.get(keyPrefix + "api-key");
            String baseUrl = providersProperties.get(keyPrefix + "base-url");
            String model = providersProperties.get(keyPrefix + "model");
            String endpoint = providersProperties.get(keyPrefix + "endpoint");

            AiProviderConfig config = new AiProviderConfig(apiKey, baseUrl, model, endpoint);

            if (config.apiKey() == null || config.apiKey().isBlank()) {
                throw new InvalidProviderConfigurationException("Invalid configuration for provider " + providerName + ": missing api key");
            }
            if (config.baseUrl() == null || config.baseUrl().isBlank()) {
                throw new InvalidProviderConfigurationException("Invalid configuration for provider " + providerName + ": missing base url");
            }
            if (config.model() == null || config.model().isBlank()) {
                throw new InvalidProviderConfigurationException("Invalid configuration for provider " + providerName + ": missing model");
            }
            if (config.endpoint() == null || config.endpoint().isBlank()) {
                throw new InvalidProviderConfigurationException("Invalid configuration for provider " + providerName + ": missing endpoint");
            }
            tempMap.put(providerName, config);
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
