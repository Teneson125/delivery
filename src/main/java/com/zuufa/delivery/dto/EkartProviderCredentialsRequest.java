package com.zuufa.delivery.dto;

import jakarta.validation.constraints.NotBlank;

public record EkartProviderCredentialsRequest(
        @NotBlank String clientId,
        @NotBlank String username,
        @NotBlank String password,
        String merchantCode
) {
}
