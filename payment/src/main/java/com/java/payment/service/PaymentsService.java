package com.java.payment.service;

import com.java.payment.entity.Payments;
import com.java.payment.entity.PaymentsRepository;
import com.java.payment.messaging.PaymentsProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.java.payment.entity.enums.PaymentState.PENDING;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final PaymentsProducer paymentsProducer;

    @Transactional("transactionManager")
    public Payments processPayments(Payments payments, UUID idempotencyKey) {
        return paymentsRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> persist(payments, idempotencyKey));
    }

    private Payments persist(Payments payments, UUID idempotencyKey) {
        Payments persisted = paymentsRepository.save(buildPendingPayment(payments, idempotencyKey));
        paymentsProducer.publish(persisted);
        return persisted;
    }

    private Payments buildPendingPayment(Payments payments, UUID idempotencyKey) {
        return payments.toBuilder()
                .idempotencyKey(idempotencyKey)
                .state(PENDING)
                .build();
    }

    public Payments getPaymentById(UUID id) {
        return paymentsRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
    }
}
