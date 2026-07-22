package com.will.compairator.configuration;

import com.will.compairator.ai.exception.InvalidPropertyFormatException;
import com.will.compairator.ai.exception.PropertyFileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ProviderPropertyReader {

    static final String AI_PROVIDER_PROPERTY_PREFIX = "ai.providers.";

    public Map<String, String> readProperties() {
        Map<String, String> properties = new HashMap<>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties");
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                String trimmedLine;


                while ((line = bufferedReader.readLine()) != null) {
                    trimmedLine = line.trim();

                    if (trimmedLine.startsWith("#")
                            || trimmedLine.startsWith("!")
                            || trimmedLine.isEmpty()) {
                        continue;
                    } else if (!trimmedLine.startsWith(AI_PROVIDER_PROPERTY_PREFIX)) {
                        continue;
                    } else {
                        // split key and value with char =, not using split here to avoid splitting more than once
                        // if there is another = char
                        int splitIndex = trimmedLine.indexOf("=");
                        if (splitIndex == -1) {
                            throw new InvalidPropertyFormatException("Invalid property format: missing '=' in line: " + trimmedLine);
                        }

                        String key = trimmedLine.substring(0, splitIndex).trim();
                        String value = trimmedLine.substring(splitIndex + 1).trim();

                        if(value.startsWith("${") && value.endsWith("}")) {
                            String envName = value.substring(2, value.length() - 1);
                            value = System.getenv(envName);
                        }

                        properties.put(key, value);
                    }
                }
            } catch (IOException exception) {
                log.error("BufferedReader exception: ", exception);
            }
            return properties;
        }
        throw new PropertyFileNotFoundException("The property file has not been found");
    }

}
