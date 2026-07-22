package com.will.compairator.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.will.compairator"
})
public class CompairatorApplication {

	public static void main(String[] args) {
		// fail fast if the resolver fails to read the property file and to validate all providers config
		AiProviderConfigResolver.getInstance();
		SpringApplication.run(CompairatorApplication.class, args);
	}

}