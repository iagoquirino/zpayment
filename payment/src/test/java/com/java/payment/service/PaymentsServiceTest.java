package com.java.payment.service;

import com.java.payment.entity.Payments;
import com.java.payment.entity.PaymentsRepository;
import com.java.payment.entity.enums.PaymentState;
import com.java.payment.events.PaymentProcessResultEvent;
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

import static com.java.payment.TestUtils.*;
import static com.java.payment.entity.enums.PaymentState.APPROVED;
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

    @Test
    void processPaymentResult_whenPaymentDoesNotExist() {
        // given
        PaymentProcessResultEvent event = givenPaymentProcessResultEvent();

        // when
        testObject.processResult(event);

        // then
        then(paymentsRepository).should(never()).save(any());
    }

    @Test
    void processPaymentResult_whenPaymentExists() {
        // given
        PaymentProcessResultEvent event = givenPaymentProcessResultEvent();

        Payments value = givenPersistedPayments();
        given(paymentsRepository.findById(event.getPaymentId())).willReturn(Optional.of(value));

        // when
        testObject.processResult(event);

        // then
        then(paymentsRepository).should().save(paymentsCaptor.capture());

        Payments payment = paymentsCaptor.getValue();
        assertThat(payment.getState()).isEqualTo(APPROVED);
    }
}