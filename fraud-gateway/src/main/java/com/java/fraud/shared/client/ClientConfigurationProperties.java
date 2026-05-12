package com.java.fraud.shared.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client")
public record ClientConfigurationProperties(ClientProperties fraudChecker) {

}
