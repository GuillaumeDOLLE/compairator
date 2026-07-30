package com.will.compairator.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.will.compairator"
})
@EnableJpaRepositories
public class CompairatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompairatorApplication.class, args);
	}

}