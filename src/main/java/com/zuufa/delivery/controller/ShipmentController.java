package com.zuufa.delivery.controller;

import static com.zuufa.common.authorization.ApplicationPermission.CANCEL_SHIPMENT;
import static com.zuufa.common.authorization.ApplicationPermission.CREATE_SHIPMENT;
import static com.zuufa.common.authorization.ApplicationPermission.READ_SHIPMENT;
import static com.zuufa.common.authorization.ApplicationPermission.TRACK_SHIPMENT;

import com.zuufa.delivery.dto.CreateShipmentRequest;
import com.zuufa.delivery.dto.DeliveryFulfillmentOptionsResponse;
import com.zuufa.delivery.dto.DeliveryFulfillmentRequest;
import com.zuufa.delivery.dto.ShipmentResponse;
import com.zuufa.delivery.dto.ShipmentStatusRequest;
import com.zuufa.delivery.dto.TrackingResponse;
import com.zuufa.delivery.service.ShipmentService;
import com.zuufa.security.annotation.RequiredPermission;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    // @RequiredPermission(CREATE_SHIPMENT)
    public ShipmentResponse createShipment(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        return shipmentService.createShipment(tenantId, request);
    }

    @GetMapping
    // @RequiredPermission(READ_SHIPMENT)
    public List<ShipmentResponse> getShipments(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return shipmentService.getShipments(tenantId);
    }

    @GetMapping("/{shipmentId}")
    // @RequiredPermission(READ_SHIPMENT)
    public ShipmentResponse getShipment(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID shipmentId
    ) {
        return shipmentService.getShipment(tenantId, shipmentId);
    }

    @GetMapping("/orders/{orderId}")
    // @RequiredPermission(READ_SHIPMENT)
    public ShipmentResponse getShipmentByOrder(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID orderId
    ) {
        return shipmentService.getShipmentByOrderId(tenantId, orderId);
    }

    @GetMapping("/orders/{orderId}/delivery-options")
    // @RequiredPermission(READ_SHIPMENT)
    public DeliveryFulfillmentOptionsResponse getDeliveryOptions(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID orderId
    ) {
        return shipmentService.getDeliveryOptions(tenantId);
    }

    @PostMapping("/orders/{orderId}/delivery-fulfillment")
    // @RequiredPermission(CREATE_SHIPMENT)
    public ShipmentResponse updateFulfillment(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID orderId,
            @Valid @RequestBody DeliveryFulfillmentRequest request
    ) {
        return shipmentService.updateFulfillment(tenantId, orderId, request);
    }

    @PatchMapping("/{shipmentId}/status")
    // @RequiredPermission(CANCEL_SHIPMENT)
    public ShipmentResponse updateStatus(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID shipmentId,
            @Valid @RequestBody ShipmentStatusRequest request
    ) {
        return shipmentService.updateStatus(tenantId, shipmentId, request);
    }

    @GetMapping("/{shipmentId}/tracking")
    // @RequiredPermission(TRACK_SHIPMENT)
    public TrackingResponse track(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID shipmentId
    ) {
        return shipmentService.track(tenantId, shipmentId);
    }
}
