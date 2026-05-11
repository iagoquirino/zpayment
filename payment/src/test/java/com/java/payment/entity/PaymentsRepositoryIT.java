package com.java.payment.entity;

import com.java.payment.IntegrationTest;
import org.junit.jupiter.api.Test;

import static com.java.payment.TestUtils.givenPayments;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentsRepositoryIT extends IntegrationTest {

    @Test
    void save() {
        // given
        Payments payments = givenPayments();

        // when
        Payments persistedEntity = paymentsRepository.save(payments);

        // then
        assertThat(payments)
                .usingRecursiveComparison()
                .isEqualTo(persistedEntity);
    }

    @Test
    void findById() {
        // given
        Payments payments = givenPayments();
        paymentsRepository.save(payments);

        // when
        Payments persistedEntity = paymentsRepository.findById(payments.getId()).get();

        // then
        assertThat(payments)
                .usingRecursiveComparison()
                .isEqualTo(persistedEntity);
    }

    @Test
    void findByIdempotencyKey() {
        // given
        Payments payments = givenPayments();
        paymentsRepository.save(payments);

        // when
        Payments persistedEntity = paymentsRepository.findByIdempotencyKey(payments.getIdempotencyKey()).get();

        // then
        assertThat(payments)
                .usingRecursiveComparison()
                .isEqualTo(persistedEntity);
    }

    @Test
    void shouldIncrementVersionOnUpdate() {
        // given
        Payments payments = givenPayments();
        Payments savedPayment = paymentsRepository.save(payments);
        Long initialVersion = savedPayment.getVersion();

        // when
        savedPayment = savedPayment.toBuilder().amount(200).build();
        Payments updatedPayment = paymentsRepository.save(savedPayment);

        // then
        assertThat(updatedPayment.getVersion()).isGreaterThan(initialVersion);
    }

}