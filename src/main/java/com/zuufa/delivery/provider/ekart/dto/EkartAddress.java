package com.zuufa.delivery.provider.ekart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EkartAddress(
        String alias, String phone,
        @JsonProperty("address_line1") String addressLine1,
        @JsonProperty("address_line2") String addressLine2,
        String pincode, String city, String state, String country
) {}
