package com.java.fraud.service;

import com.java.fraud.IntegrationTest;
import com.java.fraud.events.FraudPaymentEvent;
import com.java.fraud.events.FraudPaymentKey;
import com.java.fraud.events.FraudPaymentResultEnum;
import com.java.fraud.shared.ApplicationKafkaProperties;
import com.java.fraud.shared.FraudCheckerResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class FraudCheckerProducerIT extends IntegrationTest {

    @Autowired
    private FraudCheckerProducer testObject;

    @Autowired
    private ApplicationKafkaProperties applicationKafkaProperties;

    private KafkaConsumer<FraudPaymentKey, FraudPaymentEvent> consumer;

    private final Map<UUID, FraudPaymentEvent> mapOfEvents = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        consumer = kafkaConsumer();
        consumer.subscribe(Pattern.compile(applicationKafkaProperties.topics().get("fraud-result")));
    }


    @ParameterizedTest
    @EnumSource(FraudCheckerResult.class)
    void publish(FraudCheckerResult parameters) {
        // given
        FraudChecker fraudChecker = givenFraudChecker().toBuilder()
                .result(parameters)
                .build();

        // when
        testObject.publish(fraudChecker);

        // then
        consumer.poll(Duration.ofSeconds(5))
                .forEach(this::processEvent);

        Awaitility.await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    FraudPaymentEvent fraudPaymentEvent = mapOfEvents.get(fraudChecker.id());
                    assertThat(fraudPaymentEvent.getFraudId()).isEqualTo(fraudChecker.id());
                    assertThat(fraudPaymentEvent.getPaymentId()).isEqualTo(fraudChecker.paymentId());
                    assertThat(fraudPaymentEvent.getResult()).isEqualTo(FraudPaymentResultEnum.valueOf(parameters.name()));
                    assertThat(fraudPaymentEvent.getCreatedAt()).isNotNull();
                });
    }

    private FraudChecker givenFraudChecker() {
        return FraudChecker.builder()
                .id(UUID.randomUUID())
                .paymentId(UUID.randomUUID())
                .checkId(UUID.randomUUID())
                .result(FraudCheckerResult.PASS)
                .build();
    }

    private void processEvent(ConsumerRecord<FraudPaymentKey, FraudPaymentEvent> record) {
        FraudPaymentEvent event = record.value();
        mapOfEvents.put(event.getFraudId(), event);
    }
}