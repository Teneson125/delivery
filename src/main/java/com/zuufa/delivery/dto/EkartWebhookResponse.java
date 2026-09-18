package com.zuufa.delivery.dto;

import java.util.List;

public record EkartWebhookResponse(String id, String url, List<String> topics, boolean active) {}
