package com.java.fraud.client;

import com.java.fraud.IntegrationTest;
import com.java.fraud.client.dto.FraudCheckerResponse;
import com.java.payment.events.Money;
import com.java.payment.events.PaymentEvent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;

import static com.java.fraud.TestUtils.givenPaymentEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FraudCheckerClientIT extends IntegrationTest {

    @Autowired
    private FraudCheckerClient testObject;

    @Nested
    class PassedScenario {
        @Test
        void whenPaymentIsChecked_shouldReturnPass() {
            // given
            PaymentEvent event = givenPaymentEvent();

            // when
            FraudCheckerResponse response = testObject.checkFraud(event);

            // then
            assertThat(response.result()).isEqualTo("PASS");
            assertThat(response.checkId()).isNotNull();
        }
    }

    @Nested
    class RejectedScenario {
        @Test
        void whenPaymentIsChecked_shouldReturnReject() {
            // given
            PaymentEvent event = givenPaymentEvent();
            event.setAmount(Money.newBuilder()
                    .setValue(999)
                    .setCurrency("USD")
                    .build());

            // when
            FraudCheckerResponse response = testObject.checkFraud(event);

            // then
            assertThat(response.result()).isEqualTo("REJECT");
            assertThat(response.checkId()).isNotNull();
        }
    }

    @Nested
    class BadRequestScenario {
        @Test
        void whenPaymentAmountIsInvalid_shouldThrowHttpClientErrorException() {
            // given
            PaymentEvent event = givenPaymentEvent();
            event.setAmount(Money.newBuilder()
                    .setValue(899)
                    .setCurrency("USD")
                    .build());

            // when / then
            assertThatThrownBy(() -> testObject.checkFraud(event))
                    .isInstanceOf(HttpClientErrorException.class)
                    .extracting(e -> ((HttpClientErrorException) e).getStatusCode().value())
                    .isEqualTo(400);
        }
    }
}