package com.will.compairator.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {
		"ai", "com.will.compairator"
})
public class CompairatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompairatorApplication.class, args);
	}

}