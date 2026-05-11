package com.java.payment.messaging;

import com.java.payment.IntegrationTest;
import com.java.payment.entity.Payments;
import com.java.payment.events.PaymentEvent;
import com.java.payment.events.PaymentKey;
import com.java.payment.shared.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.java.payment.TestUtils.givenPersistedPayments;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentsProducerIT extends IntegrationTest {

    @Autowired
    private PaymentsProducer testObject;

    @Autowired
    private KafkaProperties kafkaProperties;

    private KafkaConsumer<PaymentKey, PaymentEvent> consumer;

    private final Map<UUID, PaymentEvent> mapOfEvents = new ConcurrentHashMap<>();


    @BeforeEach
    void setUp() {
        consumer = kafkaConsumer();
        consumer.subscribe(Pattern.compile(kafkaProperties.topics().get("payment-submission")));
    }

    @Test
    void publish() {
        // given
        Payments payments = givenPersistedPayments();

        // when
        testObject.publish(payments);

        // then
        consumer.poll(Duration.ofSeconds(5))
                .forEach(this::processEvent);

        Awaitility.await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(mapOfEvents.isEmpty()).isFalse());
    }

    private void processEvent(ConsumerRecord<PaymentKey, PaymentEvent> record) {
        PaymentEvent event = record.value();
        mapOfEvents.put(event.getPaymentId(), event);
    }
}