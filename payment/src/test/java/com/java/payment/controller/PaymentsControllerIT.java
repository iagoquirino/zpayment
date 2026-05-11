package com.java.payment.controller;

import com.java.payment.IntegrationTest;
import com.java.payment.api.model.PaymentRequest;
import com.java.payment.api.model.PaymentStateEnum;
import com.java.payment.entity.Payments;
import com.java.payment.entity.enums.PaymentState;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.java.payment.TestUtils.*;


class PaymentsControllerIT extends IntegrationTest {

    private static final String PAYMENTS_URL = "/v1/payments";
    private static final String GET_PAYMENTS_URL = "/v1/payments/{id}";
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";


    @Nested
    class ProcessPayment {

        @Nested
        class WhenRequestIsValid {

            @Test
            void whenValidRequestShouldReturnAcceptedWithPaymentIdAndState() throws Exception {
                // given
                UUID idempotencyKey = UUID.randomUUID();
                PaymentRequest request = givenPaymentRequest();
                Payments persisted = givenPersistedPayments();

                // when / then
                restTestClient.post()
                        .uri(PAYMENTS_URL)
                        .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .exchange()
                        .expectStatus().isAccepted()
                        .expectBody()
                        .jsonPath("$.id").isNotEmpty()
                        .jsonPath("$.state").isEqualTo(PaymentState.PENDING.name());
            }

            @Test
            void whenDuplicateIdempotencyKeyShouldReturnExistingPaymentWithoutReprocessing() throws Exception {
                // given
                UUID idempotencyKey = UUID.randomUUID();
                PaymentRequest request = givenPaymentRequest();

                Payments persisted = paymentsRepository.save(givenPayments().toBuilder()
                        .idempotencyKey(idempotencyKey)
                        .build());

                // when / then
                restTestClient.post()
                        .uri(PAYMENTS_URL)
                        .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .exchange()
                        .expectStatus().isAccepted()
                        .expectBody()
                        .jsonPath("$.id").isEqualTo(persisted.getId().toString())
                        .jsonPath("$.state").isEqualTo(PaymentState.PENDING.name());
            }
        }

        @Nested
        class WhenRequestIsInvalid {

            @Test
            void whenIdempotencyKeyHeaderIsMissingShouldReturnBadRequest() throws Exception {
                // given
                PaymentRequest request = givenPaymentRequest();

                // when / then
                restTestClient.post()
                        .uri(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .exchange()
                        .expectStatus().isBadRequest()
                        .expectBody()
                        .jsonPath("$.message").isEqualTo("Required request header 'Idempotency-Key' for method parameter type UUID is not present");
            }

            @Test
            void whenAmountIsZeroShouldReturnBadRequest() throws Exception {
                // given
                UUID idempotencyKey = UUID.randomUUID();
                PaymentRequest request = givenPaymentRequest().amount(0);

                // when / then
                restTestClient.post()
                        .uri(PAYMENTS_URL)
                        .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .exchange()
                        .expectStatus().isBadRequest()
                        .expectBody()
                        .jsonPath("$.message").isEqualTo("amount: must be greater than or equal to 1");
            }

            @Test
            void whenAmountIsNegativeShouldReturnBadRequest() throws Exception {
                // given
                UUID idempotencyKey = UUID.randomUUID();
                PaymentRequest request = givenPaymentRequest().amount(-50);

                // when / then
                restTestClient.post()
                        .uri(PAYMENTS_URL)
                        .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .exchange()
                        .expectStatus().isBadRequest()
                        .expectBody()
                        .jsonPath("$.message").isEqualTo("amount: must be greater than or equal to 1");
            }

            @Test
            void whenAccountIdIsMissingShouldReturnBadRequest() throws Exception {
                // given
                UUID idempotencyKey = UUID.randomUUID();
                PaymentRequest request = givenPaymentRequest().accountId(null);

                // when / then
                restTestClient.post()
                        .uri(PAYMENTS_URL)
                        .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .exchange()
                        .expectStatus().isBadRequest()
                        .expectBody()
                        .jsonPath("$.message").isEqualTo("accountId: must not be null");
            }

            @Test
            void whenCurrencyIsMissingShouldReturnBadRequest() throws Exception {
                // given
                UUID idempotencyKey = UUID.randomUUID();
                PaymentRequest request = givenPaymentRequest().currency(null);

                // when / then
                restTestClient.post()
                        .uri(PAYMENTS_URL)
                        .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .exchange()
                        .expectStatus().isBadRequest()
                        .expectBody()
                        .jsonPath("$.message").isEqualTo("currency: must not be null");
            }
        }
    }

    @Nested
    class GetPaymentById {

        @Nested
        class WhenPaymentExists {


            @ParameterizedTest
            @EnumSource(PaymentStateEnum.class)
            void whenValidIdShouldReturnPaymentWithIdAndState(PaymentStateEnum state) throws Exception {
                // given
                Payments persisted = paymentsRepository.save(
                        givenPayments()
                                .toBuilder()
                                .state(PaymentState.valueOf(state.name()))
                                .build()
                );

                // when / then
                restTestClient.get()
                        .uri(GET_PAYMENTS_URL, persisted.getId())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.id").isEqualTo(persisted.getId().toString())
                        .jsonPath("$.state").isEqualTo(state.name());
            }
        }

        @Nested
        class WhenPaymentDoesNotExist {

            @Test
            void whenPaymentNotFoundShouldReturnNotFoundWithMessage() throws Exception {
                // given
                UUID unknownId = UUID.randomUUID();
                // when / then
                restTestClient.get()
                        .uri(GET_PAYMENTS_URL, unknownId)
                        .exchange()
                        .expectStatus().isNotFound()
                        .expectBody()
                        .jsonPath("$.message").isEqualTo("Payment not found with id: " + unknownId);
            }
        }

        @Nested
        class WhenIdIsInvalid {
            @Test
            void whenIdIsNotUuidShouldReturnBadRequest() throws Exception {
                // when / then
                restTestClient.get()
                        .uri(GET_PAYMENTS_URL, "not-uuid")
                        .exchange()
                        .expectStatus().isBadRequest()
                        .expectBody()
                        .jsonPath("$.message").isEqualTo("Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: not-uuid");
            }
        }
    }
}