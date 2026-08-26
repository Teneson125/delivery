package com.zuufa.delivery.service;

import com.zuufa.delivery.dto.DeliveryQuoteRequest;
import com.zuufa.delivery.dto.DeliveryQuoteResponse;
import java.util.UUID;

public interface DeliveryQuoteService {
    DeliveryQuoteResponse getQuotes(UUID tenantId, DeliveryQuoteRequest request);
}
