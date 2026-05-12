package com.java.fraud.service;

import com.java.fraud.events.FraudPaymentEvent;
import com.java.fraud.events.FraudPaymentKey;
import com.java.fraud.events.FraudPaymentResultEnum;
import com.java.fraud.shared.ApplicationKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class FraudCheckerProducer {

    private static final String TOPIC = "fraud-result";

    private final KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

    private final ApplicationKafkaProperties applicationKafkaProperties;

    public void publish(FraudChecker persisted) {
        String topic = applicationKafkaProperties.topics().get(TOPIC);

        FraudPaymentKey key = FraudPaymentKey.newBuilder()
                .setFraudId(persisted.id())
                .setPaymentId(persisted.paymentId())
                .build();

        FraudPaymentEvent event = FraudPaymentEvent.newBuilder()
                .setFraudId(persisted.id())
                .setPaymentId(persisted.paymentId())
                .setResult(FraudPaymentResultEnum.valueOf(persisted.result().name()))
                .setCreatedAt(Instant.now())
                .build();

        kafkaTemplate.send(topic, key, event);
    }
}
