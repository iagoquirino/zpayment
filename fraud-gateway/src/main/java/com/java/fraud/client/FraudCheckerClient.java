package com.java.fraud.client;

import com.java.fraud.client.dto.FraudCheckerRequest;
import com.java.fraud.client.dto.FraudCheckerResponse;
import com.java.payment.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudCheckerClient {

    private final RestClient restClient;

    public FraudCheckerResponse checkFraud(PaymentEvent payment) {
        return restClient.post()
                .uri("/fraud-check/payments")
                .body(buildRequest(payment))
                .retrieve()
                .body(FraudCheckerResponse.class);
    }

    private FraudCheckerRequest buildRequest(PaymentEvent payment) {
        return FraudCheckerRequest.builder()
                .amount(payment.getAmount().getValue())
                .build();
    }
}
