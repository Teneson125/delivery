package com.zuufa.delivery.service.impl;

import com.zuufa.delivery.dto.CreateShipmentRequest;
import com.zuufa.delivery.dto.DeliveryFulfillmentOptionResponse;
import com.zuufa.delivery.dto.DeliveryFulfillmentOptionsResponse;
import com.zuufa.delivery.dto.DeliveryFulfillmentRequest;
import com.zuufa.delivery.dto.ShipmentEventResponse;
import com.zuufa.delivery.dto.ShipmentResponse;
import com.zuufa.delivery.dto.ShipmentStatusRequest;
import com.zuufa.delivery.dto.TrackingResponse;
import com.zuufa.delivery.entity.DeliveryProviderConfig;
import com.zuufa.delivery.entity.Shipment;
import com.zuufa.delivery.entity.ShipmentEvent;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.ShipmentStatus;
import com.zuufa.delivery.repository.ShipmentEventRepository;
import com.zuufa.delivery.repository.ShipmentRepository;
import com.zuufa.delivery.repository.DeliveryProviderConfigRepository;
import com.zuufa.delivery.service.ShipmentService;
import com.zuufa.exception.BadRequestException;
import com.zuufa.exception.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final DeliveryProviderConfigRepository providerConfigRepository;

    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            ShipmentEventRepository shipmentEventRepository,
            DeliveryProviderConfigRepository providerConfigRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentEventRepository = shipmentEventRepository;
        this.providerConfigRepository = providerConfigRepository;
    }

    @Override
    @Transactional
    public ShipmentResponse createShipment(UUID tenantId, CreateShipmentRequest request) {
        return shipmentRepository.findByTenantIdAndOrderId(tenantId, request.orderId())
                .map(this::toResponse)
                .orElseGet(() -> createNewShipment(tenantId, request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipments(UUID tenantId) {
        return shipmentRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipment(UUID tenantId, UUID shipmentId) {
        return toResponse(findShipment(tenantId, shipmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderId(UUID tenantId, UUID orderId) {
        return toResponse(findShipmentByOrder(tenantId, orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryFulfillmentOptionsResponse getDeliveryOptions(UUID tenantId) {
        List<DeliveryFulfillmentOptionResponse> providers = providerConfigRepository
                .findByTenantIdOrderByPriorityAsc(tenantId)
                .stream()
                .filter(config -> config.getProvider() == DeliveryProviderCode.MANUAL
                        || (config.isEnabled() && config.getEncryptedCredentials() != null))
                .map(config -> new DeliveryFulfillmentOptionResponse(
                        config.getProvider(),
                        displayName(config.getProvider()),
                        true,
                        config.getProvider() == DeliveryProviderCode.MANUAL || config.getEncryptedCredentials() != null
                ))
                .toList();

        boolean hasManual = providers.stream()
                .anyMatch(provider -> provider.provider() == DeliveryProviderCode.MANUAL);
        if (!hasManual) {
            providers = new java.util.ArrayList<>(providers);
            providers.add(0, new DeliveryFulfillmentOptionResponse(
                    DeliveryProviderCode.MANUAL,
                    displayName(DeliveryProviderCode.MANUAL),
                    true,
                    true
            ));
        }

        return new DeliveryFulfillmentOptionsResponse(providers);
    }

    @Override
    @Transactional
    public ShipmentResponse updateFulfillment(UUID tenantId, UUID orderId, DeliveryFulfillmentRequest request) {
        if (!isProviderSelectable(tenantId, request.provider())) {
            throw new BadRequestException("Delivery provider is not configured");
        }
        Shipment shipment = shipmentRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseGet(() -> newShipment(tenantId, orderId, request.provider()));
        shipment.setProvider(request.provider());
        shipment.setStatus(request.status() == null ? ShipmentStatus.READY_TO_SHIP : request.status());
        shipment.setTrackingNumber(trimToNull(request.trackingNumber()));
        shipment.setTrackingUrl(trimToNull(request.trackingUrl()));
        shipment.setNote(trimToNull(request.note()));
        shipment.setEstimatedDeliveryDate(request.estimatedDeliveryDate());

        Shipment saved = shipmentRepository.save(shipment);
        addEvent(saved.getId(), saved.getStatus(), eventMessage(saved));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ShipmentResponse updateStatus(UUID tenantId, UUID shipmentId, ShipmentStatusRequest request) {
        Shipment shipment = findShipment(tenantId, shipmentId);
        shipment.setStatus(request.status());
        Shipment saved = shipmentRepository.save(shipment);
        addEvent(saved.getId(), request.status(), request.message());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingResponse track(UUID tenantId, UUID shipmentId) {
        Shipment shipment = findShipment(tenantId, shipmentId);
        return new TrackingResponse(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getProvider(),
                shipment.getStatus(),
                shipment.getTrackingNumber(),
                getEvents(shipment.getId())
        );
    }

    private ShipmentResponse createNewShipment(UUID tenantId, CreateShipmentRequest request) {
        Shipment shipment = new Shipment();
        shipment.setTenantId(tenantId);
        shipment.setOrderId(request.orderId());
        shipment.setProvider(request.provider() == null ? DeliveryProviderCode.MANUAL : request.provider());
        shipment.setStatus(ShipmentStatus.READY_TO_SHIP);
        shipment.setDeliveryCharge(request.deliveryCharge() == null ? BigDecimal.ZERO : request.deliveryCharge());
        shipment.setCurrency(StringUtils.hasText(request.currency()) ? request.currency() : "INR");
        shipment.setWarehouseId(request.warehouseId());
        shipment.setEstimatedMinDays(request.estimatedMinDays());
        shipment.setEstimatedMaxDays(request.estimatedMaxDays());

        Shipment saved = shipmentRepository.save(shipment);
        addEvent(saved.getId(), saved.getStatus(), "Shipment is ready for delivery.");
        return toResponse(saved);
    }

    private Shipment findShipment(UUID tenantId, UUID shipmentId) {
        return shipmentRepository.findByTenantIdAndId(tenantId, shipmentId)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));
    }

    private Shipment findShipmentByOrder(UUID tenantId, UUID orderId) {
        return shipmentRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));
    }

    private Shipment newShipment(UUID tenantId, UUID orderId, DeliveryProviderCode provider) {
        Shipment shipment = new Shipment();
        shipment.setTenantId(tenantId);
        shipment.setOrderId(orderId);
        shipment.setProvider(provider == null ? DeliveryProviderCode.MANUAL : provider);
        shipment.setStatus(ShipmentStatus.READY_TO_SHIP);
        shipment.setDeliveryCharge(BigDecimal.ZERO);
        shipment.setCurrency("INR");
        return shipment;
    }

    private void addEvent(UUID shipmentId, ShipmentStatus status, String message) {
        ShipmentEvent event = new ShipmentEvent();
        event.setShipmentId(shipmentId);
        event.setStatus(status);
        event.setMessage(message);
        event.setOccurredAt(Instant.now());
        shipmentEventRepository.save(event);
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTenantId(),
                shipment.getOrderId(),
                shipment.getProvider(),
                shipment.getProviderShipmentId(),
                shipment.getAwbNumber(),
                shipment.getTrackingNumber(),
                shipment.getTrackingUrl(),
                shipment.getNote(),
                shipment.getEstimatedDeliveryDate(),
                shipment.getStatus(),
                shipment.getDeliveryCharge(),
                shipment.getCurrency(),
                shipment.getWarehouseId(),
                shipment.getEstimatedMinDays(),
                shipment.getEstimatedMaxDays(),
                getEvents(shipment.getId())
        );
    }

    private List<ShipmentEventResponse> getEvents(UUID shipmentId) {
        return shipmentEventRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId)
                .stream()
                .map(event -> new ShipmentEventResponse(
                        event.getStatus(),
                        event.getMessage(),
                        event.getOccurredAt()
                ))
                .toList();
    }

    private String displayName(DeliveryProviderCode provider) {
        return switch (provider) {
            case MANUAL -> "Manual delivery";
            case EKART -> "Ekart";
            case AMAZON_EASY_SHIP -> "Amazon Easy Ship";
            case DELHIVERY -> "Delhivery";
            case SHIPROCKET -> "Shiprocket";
        };
    }

    private boolean isProviderSelectable(UUID tenantId, DeliveryProviderCode provider) {
        if (provider == DeliveryProviderCode.MANUAL) {
            return true;
        }
        return providerConfigRepository.findByTenantIdAndProvider(tenantId, provider)
                .filter(DeliveryProviderConfig::isEnabled)
                .filter(config -> config.getEncryptedCredentials() != null)
                .isPresent();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String eventMessage(Shipment shipment) {
        if (StringUtils.hasText(shipment.getNote())) {
            return shipment.getNote();
        }
        return displayName(shipment.getProvider()) + " fulfillment updated.";
    }
}
