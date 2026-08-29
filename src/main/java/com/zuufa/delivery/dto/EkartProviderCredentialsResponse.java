package com.zuufa.delivery.dto;

public record EkartProviderCredentialsResponse(
        String clientId,
        String username,
        String password,
        String merchantCode
) {
}
