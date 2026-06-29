package com.will.compairator.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {
		"com.will.compairator"
})
@ConfigurationPropertiesScan
public class CompairatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompairatorApplication.class, args);
	}

}