package com.will.compairator.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("com.will.compairator.ai.model")
@SpringBootApplication(scanBasePackages = {
		"com.will.compairator"
})
@EnableJpaRepositories(basePackages = "com.will.compairator.ai.persistence")
public class CompairatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompairatorApplication.class, args);
	}

}