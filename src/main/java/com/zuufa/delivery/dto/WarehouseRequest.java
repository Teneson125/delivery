package com.zuufa.delivery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WarehouseRequest(
        @NotBlank String name,
        @NotBlank String contactName,
        @NotBlank String phoneNumber,
        @Email @NotBlank String email,
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String country,
        @Pattern(regexp = "^[0-9A-Za-z -]{3,16}$") String pincode
) {
}
