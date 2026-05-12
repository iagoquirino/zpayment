package com.java.fraud.messaging;

import com.java.fraud.IntegrationTest;
import com.java.fraud.service.FraudCheckService;
import com.java.fraud.shared.ApplicationKafkaProperties;
import com.java.payment.events.PaymentEvent;
import com.java.payment.events.PaymentKey;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.java.fraud.TestUtils.givenPaymentEvent;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.timeout;

class PaymentSubmittedListenerIT extends IntegrationTest {

    @Autowired
    private KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

    @Autowired
    private ApplicationKafkaProperties applicationKafkaProperties;

    @MockitoBean
    private FraudCheckService fraudCheckService;

    @Test
    void listen() {
        // given
        String topic = applicationKafkaProperties.topics().get("payment-submission");

        PaymentEvent event = givenPaymentEvent();
        PaymentKey key = PaymentKey.newBuilder()
                .setPaymentId(event.getPaymentId())
                .build();

        // when
        kafkaTemplate.send(topic, key, event);

        // then
        then(fraudCheckService).should(timeout(6000)).checkFraud(event);
    }
}