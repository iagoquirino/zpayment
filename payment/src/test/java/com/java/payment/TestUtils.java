package com.java.payment;

import com.java.payment.api.model.CurrencyEnum;
import com.java.payment.api.model.PaymentRequest;
import com.java.payment.entity.Payments;
import com.java.payment.entity.enums.Currency;
import com.java.payment.entity.enums.PaymentState;
import com.java.payment.events.Money;
import com.java.payment.events.PaymentEvent;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TestUtils {

    public static Payments givenPayments() {
        return Payments.builder()
                .accountId(UUID.randomUUID())
                .state(PaymentState.PENDING)
                .amount(100)
                .idempotencyKey(UUID.randomUUID())
                .currency(Currency.USD)
                .build();
    }


    public static Payments givenPersistedPayments() {
        return givenPayments().toBuilder()
                .id(UUID.randomUUID())
                .idempotencyKey(UUID.randomUUID())
                .version(1L)
                .build();
    }


    public static PaymentEvent givenPaymentEvent(Payments payments) {
        return PaymentEvent.newBuilder()
                .setPaymentId(payments.getId())
                .setAccountId(payments.getAccountId())
                .setAmount(Money.newBuilder()
                        .setValue(payments.getAmount())
                        .setCurrency(payments.getCurrency().name())
                        .build())
                .setCreatedAt(payments.getCreatedAt())
                .build();
    }

    public static PaymentRequest givenPaymentRequest() {
        return PaymentRequest.builder()
                .currency(CurrencyEnum.USD)
                .amount(100)
                .accountId(UUID.randomUUID())
                .build();
    }
}
