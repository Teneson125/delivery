package com.zuufa.delivery.provider.ekart.dto;

public record EkartCredentials(
        String clientId,
        String username,
        String password,
        String merchantCode
) {
}
