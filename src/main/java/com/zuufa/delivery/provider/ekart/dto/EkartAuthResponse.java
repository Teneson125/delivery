package com.zuufa.delivery.provider.ekart.dto;

public record EkartAuthResponse(
        String token,
        long expiresInSeconds
) {
}
