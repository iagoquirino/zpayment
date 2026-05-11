package com.java.payment.service;

import com.java.payment.entity.Payments;
import com.java.payment.entity.PaymentsRepository;
import com.java.payment.entity.enums.PaymentState;
import com.java.payment.messaging.PaymentsProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.java.payment.TestUtils.givenPayments;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceTest {

    @Mock
    private PaymentsRepository paymentsRepository;

    @Mock
    private PaymentsProducer paymentsProducer;

    @Captor
    private ArgumentCaptor<Payments> paymentsCaptor;

    private PaymentsService testObject;

    @BeforeEach
    void setUp() {
        testObject = new PaymentsService(paymentsRepository, paymentsProducer);
    }

    @Test
    void processPaymentsShouldPersistAndPublishEvent() {
        // given
        Payments payment = givenPayments()
                .toBuilder()
                .id(null)
                .idempotencyKey(null)
                .state(null)
                .build();

        UUID idempotencyKey = UUID.randomUUID();

        given(paymentsRepository.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.empty());

        Payments persistedPayments = payment.toBuilder()
                .id(UUID.randomUUID())
                .version(1L)
                .state(PaymentState.PENDING)
                .build();

        given(paymentsRepository.save(any())).willReturn(persistedPayments);

        // when
        Payments payments = testObject.processPayments(payment, idempotencyKey);

        // then
        assertThat(payments).isEqualTo(persistedPayments);

        then(paymentsRepository).should().save(paymentsCaptor.capture());

        Payments capturedPayments = paymentsCaptor.getValue();
        assertThat(capturedPayments.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(capturedPayments.getState()).isEqualTo(PaymentState.PENDING);
    }

    @Test
    void processPaymentsShouldNotPersistWhenIdempotencyKeyFound() {
        // given
        Payments payment = givenPayments()
                .toBuilder()
                .id(null)
                .idempotencyKey(null)
                .state(null)
                .build();

        UUID idempotencyKey = UUID.randomUUID();

        Payments persistedPayments = payment.toBuilder()
                .id(UUID.randomUUID())
                .version(1L)
                .state(PaymentState.PENDING)
                .build();


        given(paymentsRepository.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.of(persistedPayments));

        // when
        Payments payments = testObject.processPayments(payment, idempotencyKey);

        // then
        assertThat(payments).isEqualTo(persistedPayments);

        then(paymentsRepository).should(never()).save(any());
        then(paymentsProducer).should(never()).publish(any());
    }
}