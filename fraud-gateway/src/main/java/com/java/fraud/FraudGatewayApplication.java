package com.java.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class FraudGatewayApplication {

	static void main(String[] args) {
		SpringApplication.run(FraudGatewayApplication.class, args);
	}

}
