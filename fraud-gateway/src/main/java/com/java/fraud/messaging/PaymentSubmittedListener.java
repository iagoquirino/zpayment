package com.java.fraud.messaging;

import com.java.fraud.service.FraudCheckService;
import com.java.payment.events.PaymentEvent;
import com.java.payment.events.PaymentKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSubmittedListener {

    private final FraudCheckService fraudCheckService;

    @KafkaListener(topics = "${configuration.kafka.topics.payment-submission}")
    public void listen(ConsumerRecord<PaymentKey, PaymentEvent> record) {
        log.info("Received payment submission for fraud check: {}", record.key());
        fraudCheckService.checkFraud(record.value());
        log.info("Finished payment submission for fraud check: {}", record.key());
    }

}
