package com.zuufa.delivery.provider.ekart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EkartAuthResponse(
        @JsonProperty("access_token") String accessToken,
        String scope,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("token_type") String tokenType
) {
}
