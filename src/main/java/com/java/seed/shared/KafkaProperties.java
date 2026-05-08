package com.java.seed.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;


@AllArgsConstructor
@Getter
@ConfigurationProperties(prefix = "configuration.kafka")
public class KafkaProperties {
    Map<String, String> topics;
}
