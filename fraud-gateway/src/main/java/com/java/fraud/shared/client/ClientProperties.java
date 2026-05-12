package com.java.fraud.shared.client;

public record ClientProperties(String url, Integer connectTimeoutInMs, Integer readTimeoutInMs) {
}
