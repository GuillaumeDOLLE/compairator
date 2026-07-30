package com.will.compairator.configuration;

import com.will.compairator.ai.exception.MandatoryApplicationPropertyFileNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ProviderPropertyReader {

    static final String AI_PROVIDER_PROPERTY_PREFIX = "ai.providers.";

    public Map<String, String> getApplicationAiProperties() {

        Map<String, String> properties;
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties");

        if (inputStream == null) {
            throw new MandatoryApplicationPropertyFileNotFoundException("The property file has not been found");
        }

        List<String> applicationPropertiesFileLines = getApplicationPropertiesFileLines(inputStream);
        // reaching here, all the properties are inside the list (`applicationPropertiesFileLines`)
        List<String> filteredAiProperties = applicationPropertiesFileLines.stream()
                .filter(cLine -> cLine.startsWith(AI_PROVIDER_PROPERTY_PREFIX))
                .toList();

        properties = filteredAiProperties.stream()
                .map(cAiLine -> cAiLine.split("="))
                .filter(cArrayLine -> cArrayLine.length == 2)
                .collect(Collectors.toMap(
                        adjustedAiProperty -> adjustedAiProperty[0],
                        adjustedAiProperty -> {
                            // handle env variables
                            if (adjustedAiProperty[1].startsWith("${") && adjustedAiProperty[1].endsWith("}")) {
                                String envName = adjustedAiProperty[1].substring(2, adjustedAiProperty[1].length() - 1);

                                adjustedAiProperty[1] = System.getenv(envName);

                                if (adjustedAiProperty[1] == null) {
                                    throw new IllegalStateException("Environment variable not found: " + envName);
                                }
                            }

                            return adjustedAiProperty[1];
                        }

                ));
                //.forEach(adjustedAiProperty -> properties.put(adjustedAiProperty[0], adjustedAiProperty[1]));

        return properties;
    }

    private static List<String> getApplicationPropertiesFileLines(InputStream inputStream) {
        List<String> applicationPropertiesFileLines = new ArrayList<>();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                applicationPropertiesFileLines.add(line.trim());
            }
        } catch (IOException exception) {
            log.error("BufferedReader exception: ", exception);
        }
        return applicationPropertiesFileLines;
    }
}
