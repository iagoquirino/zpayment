package com.java.payment.controller;

import com.java.payment.api.PaymentsApi;
import com.java.payment.api.model.PaymentRequest;
import com.java.payment.api.model.PaymentResponse;
import com.java.payment.entity.Payments;
import com.java.payment.service.PaymentsService;
import com.java.payment.shared.mapper.PaymentsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentsController implements PaymentsApi {

    private final PaymentsService paymentsService;

    private final PaymentsMapper mapper;

    @Override
    public ResponseEntity<PaymentResponse> getPaymentById(UUID id) throws Exception {
        return ResponseEntity.ok(mapper.toResponse(paymentsService.getPaymentById(id)));
    }

    @Override
    public ResponseEntity<PaymentResponse> processPayment(UUID idempotencyKey, PaymentRequest paymentRequest) throws Exception {
        log.info("Processing payment with idempotencyKey: {}", idempotencyKey);
        Payments payments = mapper.toEntity(paymentRequest);
        Payments entity = paymentsService.processPayments(payments, idempotencyKey);
        log.info("Processed payment with idempotencyKey: {}, paymentId: {}", idempotencyKey, entity.getId());
        return ResponseEntity.accepted().body(mapper.toResponse(entity));
    }

}
