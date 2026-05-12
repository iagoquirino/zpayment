package com.java.fraud.service;

import com.java.fraud.shared.FraudCheckerResult;
import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record FraudChecker(UUID id, UUID paymentId, UUID checkId, FraudCheckerResult result) {
}
