package com.java.payment.entity;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentsRepository extends CrudRepository<Payments, UUID> {

    Optional<Payments> findByIdempotencyKey(UUID idempotencyKey);

}
