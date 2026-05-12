package com.java.payment;

import com.java.fraud.events.FraudPaymentEvent;
import com.java.fraud.events.FraudPaymentResultEnum;
import com.java.payment.api.model.CurrencyEnum;
import com.java.payment.api.model.PaymentRequest;
import com.java.payment.entity.Payments;
import com.java.payment.entity.enums.Currency;
import com.java.payment.entity.enums.PaymentState;
import com.java.payment.events.Money;
import com.java.payment.events.PaymentEvent;
import com.java.payment.events.PaymentProcessResultEnum;
import com.java.payment.events.PaymentProcessResultEvent;
import lombok.experimental.UtilityClass;

import java.time.Instant;
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

    public static PaymentProcessResultEvent givenPaymentProcessResultEvent() {
        return PaymentProcessResultEvent.newBuilder()
                .setPaymentId(UUID.randomUUID())
                .setProcessResult(PaymentProcessResultEnum.APPROVED)
                .setCreatedAt(Instant.now())
                .build();
    }

    public static PaymentEvent givenPaymentEvent() {
        return PaymentEvent.newBuilder()
                .setPaymentId(UUID.randomUUID())
                .setAccountId(UUID.randomUUID())
                .setAmount(Money.newBuilder()
                        .setValue(9000)
                        .setCurrency(Currency.USD.name())
                        .build())
                .setCreatedAt(Instant.now())
                .build();
    }

    public static FraudPaymentEvent givenFraudPaymentEvent() {
        return FraudPaymentEvent.newBuilder()
                .setFraudId(UUID.randomUUID())
                .setPaymentId(UUID.randomUUID())
                .setResult(FraudPaymentResultEnum.PASS)
                .setCreatedAt(Instant.now())
                .build();
    }

}
