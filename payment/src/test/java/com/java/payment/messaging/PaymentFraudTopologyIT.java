package com.java.payment.messaging;

import com.java.fraud.events.FraudPaymentEvent;
import com.java.fraud.events.FraudPaymentKey;
import com.java.fraud.events.FraudPaymentResultEnum;
import com.java.payment.IntegrationTest;
import com.java.payment.events.*;
import com.java.payment.shared.KafkaProperties;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentFraudTopologyIT extends IntegrationTest {

    @Autowired
    private KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

    @Autowired
    private KafkaProperties kafkaProperties;

    private final Map<UUID, PaymentProcessResultEvent> mapOfEvents = new ConcurrentHashMap<>();

    private KafkaConsumer<PaymentProcessResultKey, PaymentProcessResultEvent> kafkaConsumer = kafkaConsumer();

    @BeforeEach
    public void setup() {
        super.setup();
        String resultTopic = kafkaProperties.topics().get("payment-process-result");
        kafkaConsumer.subscribe(Pattern.compile(resultTopic));
    }

    @Test
    void whenPaymentAndFraudResultArrive_shouldEmitApprovedProcessResult() {
        // given
        UUID paymentId = UUID.randomUUID();

        PaymentKey paymentKey = PaymentKey.newBuilder()
                .setPaymentId(paymentId)
                .build();

        PaymentEvent paymentEvent = PaymentEvent.newBuilder()
                .setPaymentId(paymentId)
                .setAccountId(UUID.randomUUID())
                .setAmount(Money.newBuilder().setValue(100).setCurrency("USD").build())
                .setCreatedAt(Instant.now())
                .build();

        FraudPaymentKey fraudKey = FraudPaymentKey.newBuilder()
                .setFraudId(UUID.randomUUID())
                .setPaymentId(paymentId)
                .build();

        FraudPaymentEvent fraudEvent = FraudPaymentEvent.newBuilder()
                .setFraudId(UUID.randomUUID())
                .setPaymentId(paymentId)
                .setResult(FraudPaymentResultEnum.PASS)
                .setCreatedAt(Instant.now())
                .build();

        String paymentTopic = kafkaProperties.topics().get("payment-submission");
        String fraudTopic = kafkaProperties.topics().get("fraud-result");


        // when
        kafkaTemplate.executeInTransaction(ko -> ko.send(paymentTopic, paymentKey, paymentEvent));
        kafkaTemplate.executeInTransaction(ko -> ko.send(fraudTopic, fraudKey, fraudEvent));

        // then
        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    kafkaConsumer.poll(Duration.ofMillis(100))
                            .forEach(this::processEvent);
                    assertThat(mapOfEvents.containsKey(paymentId)).isTrue();
                });
    }

    private void processEvent(ConsumerRecord<PaymentProcessResultKey, PaymentProcessResultEvent> record) {
        mapOfEvents.put(record.key().getPaymentId(), record.value());
    }
}