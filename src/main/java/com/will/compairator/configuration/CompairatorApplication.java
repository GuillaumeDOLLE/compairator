package com.will.compairator.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("com.will.compairator")
@SpringBootApplication(scanBasePackages = {
		"com.will.compairator"
})
@EnableJpaRepositories(basePackages = "com.will.compairator")
@EnableCaching
public class CompairatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompairatorApplication.class, args);
	}

}
