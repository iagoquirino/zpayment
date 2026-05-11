package com.java.payment.messaging;

import com.java.payment.entity.Payments;
import com.java.payment.events.PaymentEvent;
import com.java.payment.events.PaymentKey;
import com.java.payment.shared.KafkaProperties;
import com.java.payment.shared.mapper.PaymentEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentsProducer {

    private static final String PAYMENT_SUBMISSION = "payment-submission";

    private final PaymentEventMapper mapper;

    private final KafkaTemplate<PaymentKey, PaymentEvent> kafkaTemplate;

    private final KafkaProperties kafkaProperties;

    @Transactional("kafkaTransactionManager")
    public void publish(Payments payments) {
        String topic = kafkaProperties.topics().get(PAYMENT_SUBMISSION);
        PaymentKey key = mapper.toEventKey(payments);
        PaymentEvent event = mapper.toEvent(payments);
        kafkaTemplate.send(topic, key, event);
    }
}
