package com.zuufa.delivery.service;

import com.zuufa.delivery.dto.CreateShipmentRequest;
import com.zuufa.delivery.dto.ShipmentResponse;
import com.zuufa.delivery.dto.ShipmentStatusRequest;
import com.zuufa.delivery.dto.TrackingResponse;
import java.util.List;
import java.util.UUID;

public interface ShipmentService {
    ShipmentResponse createShipment(UUID tenantId, CreateShipmentRequest request);
    List<ShipmentResponse> getShipments(UUID tenantId);
    ShipmentResponse getShipment(UUID tenantId, UUID shipmentId);
    ShipmentResponse updateStatus(UUID tenantId, UUID shipmentId, ShipmentStatusRequest request);
    TrackingResponse track(UUID tenantId, UUID shipmentId);
}
