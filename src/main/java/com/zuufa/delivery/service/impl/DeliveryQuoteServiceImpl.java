package com.zuufa.delivery.service.impl;

import com.zuufa.delivery.dto.DeliveryQuoteItemRequest;
import com.zuufa.delivery.dto.DeliveryQuoteOptionResponse;
import com.zuufa.delivery.dto.DeliveryQuoteRequest;
import com.zuufa.delivery.dto.DeliveryQuoteResponse;
import com.zuufa.delivery.entity.DeliveryProviderConfig;
import com.zuufa.delivery.entity.TenantDeliverySettings;
import com.zuufa.delivery.entity.Warehouse;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.DeliveryPricingStrategy;
import com.zuufa.delivery.provider.DeliveryProviderFactory;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderRequest;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderResponse;
import com.zuufa.delivery.repository.DeliveryProviderConfigRepository;
import com.zuufa.delivery.repository.TenantDeliverySettingsRepository;
import com.zuufa.delivery.repository.WarehouseRepository;
import com.zuufa.delivery.service.DeliveryQuoteService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryQuoteServiceImpl implements DeliveryQuoteService {

    private final TenantDeliverySettingsRepository settingsRepository;
    private final WarehouseRepository warehouseRepository;
    private final DeliveryProviderConfigRepository providerConfigRepository;
    private final DeliveryProviderFactory providerFactory;

    @Override
    public DeliveryQuoteResponse getQuotes(UUID tenantId, DeliveryQuoteRequest request) {
        TenantDeliverySettings settings = settingsRepository.findByTenantId(tenantId)
                .orElse(null);
        if (settings == null) {
            return fallbackQuote();
        }
        if (!settings.isEnabled()) {
            return fallbackQuote();
        }
        Warehouse warehouse = warehouseRepository.findByTenantIdAndDefaultWarehouseTrue(tenantId)
                .orElse(null);
        if (warehouse == null || !warehouse.isActive()) {
            log.info("Using delivery fallback because default warehouse is missing or inactive tenantId={}", tenantId);
        }

        BigDecimal subtotal = resolveSubtotal(request);
        DeliveryQuoteProviderRequest providerRequest = new DeliveryQuoteProviderRequest(
                tenantId,
                subtotal,
                request.items(),
                request.deliveryAddress()
        );
        List<DeliveryProviderConfig> providerConfigs = providerConfigRepository.findByTenantIdOrderByPriorityAsc(tenantId);
        List<DeliveryQuoteOptionResponse> quotes = providerConfigs
                .stream()
                .filter(DeliveryProviderConfig::isEnabled)
                .filter(config -> config.getProvider() != DeliveryProviderCode.MANUAL || settings.isManualEnabled())
                .map(config -> quoteProvider(settings, warehouse, providerRequest, config))
                .filter(DeliveryQuoteProviderResponse::serviceable)
                .sorted(quoteComparator(settings))
                .map(response -> toOption(response, false))
                .toList();

        if (quotes.isEmpty()
                && settings.isManualEnabled()
                && providerConfigs.stream().noneMatch(config -> config.getProvider() == DeliveryProviderCode.MANUAL)) {
            quotes = List.of(toOption(quoteProvider(settings, warehouse, providerRequest, manualProviderConfig(tenantId)), false));
        }

        if (quotes.isEmpty()) {
            return fallbackQuote();
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

    private DeliveryProviderConfig manualProviderConfig(UUID tenantId) {
        DeliveryProviderConfig config = new DeliveryProviderConfig();
        config.setTenantId(tenantId);
        config.setProvider(DeliveryProviderCode.MANUAL);
        config.setEnabled(true);
        config.setPriority(1);
        return config;
    }

    private DeliveryQuoteProviderResponse quoteProvider(
            TenantDeliverySettings settings,
            Warehouse warehouse,
            DeliveryQuoteProviderRequest request,
            DeliveryProviderConfig config
    ) {
        DeliveryProviderContext context = new DeliveryProviderContext(
                settings.getTenantId(),
                config.getEncryptedCredentials(),
                config.getSettingsJson(),
                warehouse == null ? null : warehouse.getPincode(),
                settings.getManualFixedCharge(),
                settings.getManualFreeDeliveryAbove(),
                settings.getManualEstimatedMinDays(),
                settings.getManualEstimatedMaxDays()
        );
        try {
            return providerFactory.getProvider(config.getProvider()).quote(request, context);
        } catch (RuntimeException error) {
            log.warn("Ignoring delivery provider quote failure tenantId={} provider={}", settings.getTenantId(), config.getProvider(), error);
            return new DeliveryQuoteProviderResponse(
                    config.getProvider(),
                    "Delivery",
                    BigDecimal.ZERO,
                    false,
                    0,
                    0,
                    false,
                    null
            );
        }
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

    private Comparator<DeliveryQuoteProviderResponse> quoteComparator(TenantDeliverySettings settings) {
        Comparator<DeliveryQuoteProviderResponse> comparator = Comparator.comparing(
                DeliveryQuoteProviderResponse::amount,
                Comparator.nullsLast(BigDecimal::compareTo)
        );
        return settings.getPricingStrategy() == DeliveryPricingStrategy.LOWEST ? comparator : comparator.reversed();
    }

    private DeliveryQuoteResponse fallbackQuote() {
        return new DeliveryQuoteResponse("INR", List.of(new DeliveryQuoteOptionResponse(
                DeliveryProviderCode.MANUAL.name().toLowerCase() + "-" + UUID.randomUUID(),
                DeliveryProviderCode.MANUAL,
                "Delivery",
                BigDecimal.ZERO,
                false,
                0,
                0,
                true,
                true,
                null
        )));
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
