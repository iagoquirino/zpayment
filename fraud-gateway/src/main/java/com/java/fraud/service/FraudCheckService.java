package com.java.fraud.service;

import com.java.fraud.client.FraudCheckerClient;
import com.java.fraud.client.dto.FraudCheckerResponse;
import com.java.fraud.shared.FraudCheckerResult;
import com.java.payment.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudCheckService {

    private final FraudCheckerClient fraudCheckerClient;
    private final FraudCheckerProducer fraudCheckerProducer;

    public void checkFraud(PaymentEvent event) {
        log.info("Processing fraud check for payment ID: {}", event.getPaymentId());
        FraudCheckerResponse response = fraudCheckerClient.checkFraud(event);
        FraudChecker persisted = FraudChecker.builder()
                .id(UUID.randomUUID())
                .paymentId(event.getPaymentId())
                .checkId(response.checkId())
                .result(FraudCheckerResult.valueOf(response.result()))
                .build();
        fraudCheckerProducer.publish(persisted);
    }

}
