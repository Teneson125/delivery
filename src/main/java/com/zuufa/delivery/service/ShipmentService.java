package com.zuufa.delivery.service;

import com.zuufa.delivery.dto.CreateShipmentRequest;
import com.zuufa.delivery.dto.DeliveryFulfillmentOptionsResponse;
import com.zuufa.delivery.dto.DeliveryFulfillmentRequest;
import com.zuufa.delivery.dto.ShipmentLabelResponse;
import com.zuufa.delivery.dto.ShipmentResponse;
import com.zuufa.delivery.dto.ShipmentStatusRequest;
import com.zuufa.delivery.dto.TrackingResponse;
import java.util.List;
import java.util.UUID;

public interface ShipmentService {
    ShipmentResponse createShipment(UUID tenantId, CreateShipmentRequest request);
    List<ShipmentResponse> getShipments(UUID tenantId);
    ShipmentResponse getShipment(UUID tenantId, UUID shipmentId);
    ShipmentResponse getShipmentByOrderId(UUID tenantId, UUID orderId);
    DeliveryFulfillmentOptionsResponse getDeliveryOptions(UUID tenantId);
    ShipmentResponse updateFulfillment(UUID tenantId, UUID orderId, DeliveryFulfillmentRequest request);
    ShipmentResponse updateStatus(UUID tenantId, UUID shipmentId, ShipmentStatusRequest request);
    ShipmentLabelResponse getLabel(UUID tenantId, UUID shipmentId);
    TrackingResponse track(UUID tenantId, UUID shipmentId);
}
