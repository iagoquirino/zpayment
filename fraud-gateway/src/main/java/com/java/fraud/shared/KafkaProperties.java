package com.java.fraud.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;


@ConfigurationProperties(prefix = "configuration.kafka")
public record KafkaProperties(Map<String, String> topics) { }