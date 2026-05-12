package com.java.payment.messaging;

import com.java.payment.events.PaymentProcessResultEvent;
import com.java.payment.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentProcessResultListener {

    private final PaymentsService paymentsService;

    @Bean
    public Consumer<PaymentProcessResultEvent> paymentProcessResultListenerV1() {
        return event -> {
            log.info("Received payment fraud event for paymentId={}, result={}", event.getPaymentId(), event.getProcessResult());
            paymentsService.processResult(event);
        };
    }
}
