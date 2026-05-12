package com.java.payment.messaging;

import com.java.payment.IntegrationTest;
import com.java.payment.events.PaymentProcessResultEvent;
import com.java.payment.events.PaymentProcessResultKey;
import com.java.payment.service.PaymentsService;
import com.java.payment.shared.KafkaProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.java.payment.TestUtils.givenPaymentProcessResultEvent;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.timeout;

class PaymentProcessResultListenerIT extends IntegrationTest {

    @Autowired
    private KafkaTemplate<PaymentProcessResultKey, PaymentProcessResultEvent> kafkaTemplate;

    @Autowired
    private KafkaProperties kafkaProperties;

    @MockitoBean
    private PaymentsService paymentsService;

    @Test
    void listen() {
        // given
        PaymentProcessResultEvent event = givenPaymentProcessResultEvent();

        PaymentProcessResultKey key = PaymentProcessResultKey.newBuilder()
                .setPaymentId(event.getPaymentId())
                .build();

        String topic = kafkaProperties.topics().get("payment-process-result");

        // when
        kafkaTemplate.executeInTransaction(ko-> ko.send(topic, key, event));

        // then
        then(paymentsService).should(timeout(6000)).processResult(event);
    }
}