package com.java.seed;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;

@ConfigurationPropertiesScan
@SpringBootApplication
public class Application {

	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return Flyway::migrate;
    }
}
