package com.java.payment.entity;


import com.java.payment.entity.enums.Currency;
import com.java.payment.entity.enums.PaymentState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static com.java.payment.entity.enums.PaymentState.PENDING;

@Getter
@Entity
@Table
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PaymentState state = PENDING;

    @Version
    private Long version;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
