package com.java.fraud;

import com.java.fraud.client.dto.FraudCheckerResponse;
import com.java.payment.events.Money;
import com.java.payment.events.PaymentEvent;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class TestUtils {

  public static FraudCheckerResponse givenFraudCheckerResponse() {
    return FraudCheckerResponse.builder()
            .checkId(UUID.randomUUID())
            .result("PASS")
            .build();
  }

  public static PaymentEvent givenPaymentEvent() {
    return PaymentEvent.newBuilder()
            .setAccountId(UUID.randomUUID())
            .setPaymentId(UUID.randomUUID())
            .setAmount(Money.newBuilder()
                    .setValue(1000)
                    .setCurrency("USD")
                    .build())
            .setCreatedAt(Instant.now())
            .build();
  }
}
