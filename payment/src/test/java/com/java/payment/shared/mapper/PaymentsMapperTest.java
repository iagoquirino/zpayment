package com.java.payment.shared.mapper;

import com.java.payment.api.model.PaymentRequest;
import com.java.payment.api.model.PaymentResponse;
import com.java.payment.api.model.PaymentStateEnum;
import com.java.payment.entity.Payments;
import com.java.payment.entity.enums.Currency;
import org.junit.jupiter.api.Test;

import static com.java.payment.TestUtils.givenPaymentRequest;
import static com.java.payment.TestUtils.givenPayments;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentsMapperTest {

    private final PaymentsMapper mapper = new PaymentsMapperImpl();

    @Test
    void toEntity() {
        // given
        PaymentRequest request = givenPaymentRequest();

        // when
        Payments entity = mapper.toEntity(request);

        // then
        Payments expectedPayments = Payments.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(Currency.USD)
                .build();

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(expectedPayments);

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void toResponse() {
        // given
        Payments payments = givenPayments();

        // when
        PaymentResponse response = mapper.toResponse(payments);

        // then
        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(payments.getId())
                .state(PaymentStateEnum.PENDING)
                .build();

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }
}