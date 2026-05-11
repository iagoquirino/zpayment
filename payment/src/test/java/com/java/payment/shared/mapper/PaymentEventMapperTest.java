package com.java.payment.shared.mapper;

import com.java.payment.entity.Payments;
import com.java.payment.events.PaymentEvent;
import com.java.payment.events.PaymentKey;
import org.junit.jupiter.api.Test;

import static com.java.payment.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentEventMapperTest {

    private PaymentEventMapper testObject = new PaymentEventMapperImpl();

    @Test
    void toEventKey() {
        // given
        Payments payments = givenPersistedPayments();

        // when
        PaymentKey eventKey = testObject.toEventKey(payments);

        // then
        assertThat(eventKey.getPaymentId())
                .isEqualTo(payments.getId());
    }

    @Test
    void toEvent() {
        // given
        Payments payments = givenPersistedPayments();

        // when
        PaymentEvent event = testObject.toEvent(payments);

        // then
        PaymentEvent expectedEvent = givenPaymentEvent(payments);

        assertThat(event)
                .usingRecursiveComparison()
                .isEqualTo(expectedEvent);
    }
}