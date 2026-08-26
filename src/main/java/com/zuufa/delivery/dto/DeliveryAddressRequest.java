package com.zuufa.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeliveryAddressRequest(
        @NotBlank String name,
        @NotBlank String phoneNumber,
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String country,
        @Pattern(regexp = "^[0-9A-Za-z -]{3,16}$") String pincode
) {
}
