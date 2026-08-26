package com.zuufa.delivery.provider.ekart.dto;

public record EkartAuthRequest(
        String username,
        String merchantCode
) {
}
