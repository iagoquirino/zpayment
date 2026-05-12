package com.java.fraud.client.dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record FraudCheckerResponse(UUID checkId, String result) { }
