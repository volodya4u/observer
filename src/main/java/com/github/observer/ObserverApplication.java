package com.github.observer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.github.observer"})
@OpenAPIDefinition
public class ObserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObserverApplication.class, args);
	}
}
