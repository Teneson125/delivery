package com.zuufa.delivery.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record EkartWebhookRequest(
        @NotBlank @Size(max = 2000) String url,
        @NotBlank @Size(min = 6, max = 30) String secret,
        @NotEmpty @Size(max = 3) List<@Pattern(regexp = "track_updated|shipment_created|shipment_recreated") String> topics,
        boolean active
) {}
