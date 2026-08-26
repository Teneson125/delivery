package com.zuufa.delivery.service.impl;

import com.zuufa.delivery.dto.CreateShipmentRequest;
import com.zuufa.delivery.dto.ShipmentEventResponse;
import com.zuufa.delivery.dto.ShipmentResponse;
import com.zuufa.delivery.dto.ShipmentStatusRequest;
import com.zuufa.delivery.dto.TrackingResponse;
import com.zuufa.delivery.entity.Shipment;
import com.zuufa.delivery.entity.ShipmentEvent;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.ShipmentStatus;
import com.zuufa.delivery.repository.ShipmentEventRepository;
import com.zuufa.delivery.repository.ShipmentRepository;
import com.zuufa.delivery.service.ShipmentService;
import jakarta.persistence.EntityNotFoundException;
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

    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            ShipmentEventRepository shipmentEventRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentEventRepository = shipmentEventRepository;
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
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));
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
}
