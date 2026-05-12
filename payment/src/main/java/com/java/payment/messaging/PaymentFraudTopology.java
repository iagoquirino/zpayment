package com.java.payment.messaging;

import com.java.fraud.events.FraudPaymentEvent;
import com.java.fraud.events.FraudPaymentKey;
import com.java.fraud.events.FraudPaymentResultEnum;
import com.java.payment.events.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.UUID;
import java.util.function.BiFunction;

import static com.java.payment.events.PaymentProcessResultEnum.UNDER_REVIEW;
import static java.util.Objects.isNull;

@Configuration
public class PaymentFraudTopology {

    @Bean
    public BiFunction<KStream<PaymentKey, PaymentEvent>, KStream<FraudPaymentKey, FraudPaymentEvent>, KStream<PaymentProcessResultKey, PaymentProcessResultEvent>> paymentFraudTopologyV1() {
        return (paymentKStream, fraudPaymentKStream) -> {

            KTable<PaymentProcessResultKey, PaymentEvent> kTablePayments = paymentKStream
                    .selectKey((key, value) -> buildKey(key.getPaymentId()))
                    .toTable();

            KTable<PaymentProcessResultKey, FraudPaymentEvent> kTableFraud = fraudPaymentKStream
                    .selectKey((key, value) -> buildKey(key.getPaymentId()))
                    .toTable();

            return kTablePayments
                    .leftJoin(kTableFraud, PaymentFraudTopology::join)
                    .toStream();
        };
    }

    private static PaymentProcessResultEvent join(PaymentEvent paymentEvent, FraudPaymentEvent fraudPaymentEvent) {
        if (isNull(fraudPaymentEvent)) {
            return build(paymentEvent.getPaymentId(), UNDER_REVIEW);
        }
        return build(paymentEvent.getPaymentId(), processStatus(fraudPaymentEvent.getResult()));
    }

    private static PaymentProcessResultEnum processStatus(FraudPaymentResultEnum result) {
        return switch (result) {
            case PASS -> PaymentProcessResultEnum.APPROVED;
            case REJECT -> PaymentProcessResultEnum.REJECTED;
            default -> PaymentProcessResultEnum.UNDER_REVIEW;
        };
    }

    private static PaymentProcessResultEvent build(UUID paymentId, PaymentProcessResultEnum result) {
        return PaymentProcessResultEvent.newBuilder()
                .setPaymentId(paymentId)
                .setProcessResult(result)
                .setCreatedAt(Instant.now())
                .build();
    }

    private static PaymentProcessResultKey buildKey(UUID paymentId) {
        return PaymentProcessResultKey.newBuilder()
                .setPaymentId(paymentId)
                .build();
    }
}
