package com.zuufa.delivery.dto;

import jakarta.validation.constraints.*;

public record EkartAddressRequest(
        @NotBlank @Size(max = 100) String alias,
        @NotBlank @Pattern(regexp = "[1-9][0-9]{9}") String phone,
        @NotBlank @Size(max = 500) String addressLine1,
        @Size(max = 500) String addressLine2,
        @NotBlank @Pattern(regexp = "[1-9][0-9]{5}") String pincode,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Pattern(regexp = "India|IN") String country
) {}
