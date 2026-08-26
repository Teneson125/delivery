package com.zuufa.delivery.service.impl;

import com.zuufa.delivery.dto.DeliveryQuoteItemRequest;
import com.zuufa.delivery.dto.DeliveryQuoteOptionResponse;
import com.zuufa.delivery.dto.DeliveryQuoteRequest;
import com.zuufa.delivery.dto.DeliveryQuoteResponse;
import com.zuufa.delivery.entity.DeliveryProviderConfig;
import com.zuufa.delivery.entity.TenantDeliverySettings;
import com.zuufa.delivery.entity.Warehouse;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.provider.DeliveryProviderFactory;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderRequest;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderResponse;
import com.zuufa.delivery.repository.DeliveryProviderConfigRepository;
import com.zuufa.delivery.repository.TenantDeliverySettingsRepository;
import com.zuufa.delivery.repository.WarehouseRepository;
import com.zuufa.delivery.service.DeliveryQuoteService;
import com.zuufa.exception.BadRequestException;
import com.zuufa.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryQuoteServiceImpl implements DeliveryQuoteService {

    private final TenantDeliverySettingsRepository settingsRepository;
    private final WarehouseRepository warehouseRepository;
    private final DeliveryProviderConfigRepository providerConfigRepository;
    private final DeliveryProviderFactory providerFactory;

    @Override
    public DeliveryQuoteResponse getQuotes(UUID tenantId, DeliveryQuoteRequest request) {
        TenantDeliverySettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Delivery settings not found"));
        if (!settings.isEnabled()) {
            return new DeliveryQuoteResponse("INR", List.of());
        }
        Warehouse warehouse = warehouseRepository.findByTenantIdAndDefaultWarehouseTrue(tenantId)
                .orElseThrow(() -> new NotFoundException("Default warehouse not found"));
        if (!warehouse.isActive()) {
            throw new BadRequestException("Default warehouse is inactive");
        }

        BigDecimal subtotal = resolveSubtotal(request);
        DeliveryQuoteProviderRequest providerRequest = new DeliveryQuoteProviderRequest(tenantId, subtotal);
        List<DeliveryQuoteOptionResponse> quotes = providerConfigRepository.findByTenantIdOrderByPriorityAsc(tenantId)
                .stream()
                .filter(DeliveryProviderConfig::isEnabled)
                .map(config -> quoteProvider(settings, providerRequest, config))
                .filter(DeliveryQuoteProviderResponse::serviceable)
                .sorted(Comparator.comparing(DeliveryQuoteProviderResponse::amount))
                .map(response -> toOption(response, false))
                .toList();

        if (quotes.isEmpty()) {
            return new DeliveryQuoteResponse("INR", quotes);
        }
        DeliveryQuoteOptionResponse first = quotes.getFirst();
        List<DeliveryQuoteOptionResponse> marked = quotes.stream()
                .map(option -> option.quoteId().equals(first.quoteId()) ? new DeliveryQuoteOptionResponse(
                        option.quoteId(),
                        option.provider(),
                        option.label(),
                        option.amount(),
                        option.freeDeliveryApplied(),
                        option.estimatedMinDays(),
                        option.estimatedMaxDays(),
                        option.serviceable(),
                        true,
                        option.message()
                ) : option)
                .toList();
        return new DeliveryQuoteResponse("INR", marked);
    }

    private DeliveryQuoteProviderResponse quoteProvider(
            TenantDeliverySettings settings,
            DeliveryQuoteProviderRequest request,
            DeliveryProviderConfig config
    ) {
        DeliveryProviderContext context = new DeliveryProviderContext(
                settings.getTenantId(),
                config.getEncryptedCredentials(),
                config.getSettingsJson(),
                settings.getManualFixedCharge(),
                settings.getManualFreeDeliveryAbove(),
                settings.getManualEstimatedMinDays(),
                settings.getManualEstimatedMaxDays()
        );
        return providerFactory.getProvider(config.getProvider()).quote(request, context);
    }

    private DeliveryQuoteOptionResponse toOption(DeliveryQuoteProviderResponse response, boolean recommended) {
        return new DeliveryQuoteOptionResponse(
                response.provider().name().toLowerCase() + "-" + UUID.randomUUID(),
                response.provider(),
                response.label(),
                response.amount(),
                response.freeDeliveryApplied(),
                response.estimatedMinDays(),
                response.estimatedMaxDays(),
                response.serviceable(),
                recommended,
                response.message()
        );
    }

    private BigDecimal resolveSubtotal(DeliveryQuoteRequest request) {
        if (request.subtotal() != null) {
            return request.subtotal();
        }
        return request.items().stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal lineTotal(DeliveryQuoteItemRequest item) {
        if (item.unitPrice() == null) {
            return BigDecimal.ZERO;
        }
        return item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
    }
}
