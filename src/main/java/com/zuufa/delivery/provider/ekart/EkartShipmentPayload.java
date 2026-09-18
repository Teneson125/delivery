package com.zuufa.delivery.provider.ekart;

import com.zuufa.delivery.provider.dto.CreateShipmentProviderRequest;
import com.zuufa.delivery.provider.ekart.dto.EkartSettings;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EkartShipmentPayload {
    private EkartShipmentPayload() {}

    public static Map<String, Object> build(CreateShipmentProviderRequest request, EkartSettings settings) {
        var invoice = request.ekartDetails();
        var address = request.deliveryAddress();
        if (invoice == null || address == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Ekart needs invoice, customer and package details");
        }
        if (invoice.totalAmount() == null || invoice.taxValue() == null || invoice.taxableAmount() == null
                || invoice.codAmount() == null || invoice.consigneeGstAmount() == null || invoice.invoiceDate() == null
                || invoice.sellerGstTin() == null || invoice.totalAmount().compareTo(BigDecimal.ONE) < 0
                || invoice.taxableAmount().compareTo(BigDecimal.ONE) < 0 || invoice.taxValue().signum() < 0
                || invoice.consigneeGstAmount().signum() < 0
                || invoice.totalAmount().compareTo(invoice.taxableAmount().add(invoice.taxValue())) != 0) {
            throw new IllegalArgumentException("Ekart needs valid invoice amounts");
        }
        for (String value : new String[]{invoice.sellerName(), invoice.sellerAddress(), invoice.invoiceNumber(),
                invoice.categoryOfGoods(), invoice.productsDescription(), address.name(), address.addressLine1(),
                address.city(), address.state(), settings.pickupAddressAlias(), settings.returnAddressAlias()}) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException("Ekart shipment details are incomplete");
        }
        if (!("Prepaid".equals(invoice.paymentMode()) || "COD".equals(invoice.paymentMode()))
                || invoice.codAmount().signum() < 0 || invoice.codAmount().compareTo(new BigDecimal("49999")) > 0
                || ("Prepaid".equals(invoice.paymentMode()) && invoice.codAmount().signum() != 0)
                || ("COD".equals(invoice.paymentMode()) && invoice.codAmount().signum() <= 0)) {
            throw new IllegalArgumentException("Invalid shipment payment amounts");
        }
        String phone = address.phoneNumber() == null ? "" : address.phoneNumber().replaceAll("[\\s()-]", "");
        if (phone.startsWith("+91")) phone = phone.substring(3);
        if (!phone.matches("[1-9][0-9]{9}") || address.pincode() == null || !address.pincode().matches("[1-9][0-9]{5}")
                || !("IN".equals(address.country()) || "India".equals(address.country()))) {
            throw new IllegalArgumentException("Ekart needs an Indian customer address and ten digit phone number");
        }
        if (invoice.weightGrams() < 1 || invoice.lengthCm() < 1 || invoice.widthCm() < 1 || invoice.heightCm() < 1) {
            throw new IllegalArgumentException("Actual packed dimensions and weight are required for Ekart booking");
        }
        int quantity = 0;
        for (var item : request.items()) {
            if (item.quantity() < 1) throw new IllegalArgumentException("Invalid item quantity");
            quantity = Math.addExact(quantity, item.quantity());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("seller_name", invoice.sellerName()); body.put("seller_address", invoice.sellerAddress());
        body.put("seller_gst_tin", invoice.sellerGstTin()); body.put("consignee_gst_amount", invoice.consigneeGstAmount());
        body.put("order_number", request.orderId().toString()); body.put("invoice_number", invoice.invoiceNumber());
        body.put("invoice_date", invoice.invoiceDate().toString()); body.put("consignee_name", address.name());
        body.put("consignee_alternate_phone", phone); body.put("payment_mode", invoice.paymentMode());
        body.put("category_of_goods", invoice.categoryOfGoods()); body.put("products_desc", invoice.productsDescription());
        body.put("total_amount", invoice.totalAmount()); body.put("tax_value", invoice.taxValue());
        body.put("taxable_amount", invoice.taxableAmount()); body.put("commodity_value", invoice.taxableAmount().toPlainString());
        body.put("cod_amount", invoice.codAmount()); body.put("quantity", quantity); body.put("weight", invoice.weightGrams());
        body.put("length", invoice.lengthCm()); body.put("width", invoice.widthCm()); body.put("height", invoice.heightCm());
        body.put("pickup_location", Map.of("name", settings.pickupAddressAlias()));
        body.put("return_location", Map.of("name", settings.returnAddressAlias()));
        body.put("drop_location", Map.of("name", address.name(), "phone", Long.valueOf(phone),
                "address", address.addressLine1() + (address.addressLine2() == null || address.addressLine2().isBlank() ? "" : ", " + address.addressLine2()),
                "pin", Integer.valueOf(address.pincode()), "city", address.city(), "state", address.state(), "country", "India"));
        return body;
    }
}
