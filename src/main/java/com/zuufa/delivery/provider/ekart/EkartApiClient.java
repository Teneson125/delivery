package com.zuufa.delivery.provider.ekart;

import com.zuufa.delivery.config.EkartDeliveryProperties;
import com.zuufa.delivery.provider.ekart.dto.EkartEstimateRequest;
import com.zuufa.delivery.provider.ekart.dto.EkartEstimateResponse;
import com.zuufa.delivery.provider.ekart.dto.EkartServiceabilityRequest;
import com.zuufa.delivery.provider.ekart.dto.EkartServiceabilityV3Response;
import com.zuufa.delivery.provider.ekart.dto.EkartShipmentResponse;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EkartApiClient {

    private final EkartDeliveryProperties properties;
    private final RestClient.Builder restClientBuilder;

    public EkartApiClient(EkartDeliveryProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClientBuilder = restClientBuilder;
    }

    public List<EkartServiceabilityV3Response> checkServiceability(
            String authorization,
            EkartServiceabilityRequest request
    ) {
        return client().post()
                .uri("/data/v3/serviceability")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public EkartEstimateResponse estimate(String authorization, EkartEstimateRequest request) {
        return client().post()
                .uri("/data/pricing/estimate")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(EkartEstimateResponse.class);
    }

    public EkartShipmentResponse createShipment(String authorization, Map<String, Object> request) {
        return client().put()
                .uri("/api/v1/package/create")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(EkartShipmentResponse.class);
    }

    public Map<String, Object> downloadLabel(String authorization, List<String> trackingIds) {
        return client().post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/package/label")
                        .queryParam("json_only", true)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(Map.of("ids", trackingIds))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void cancel(String authorization, String trackingId) {
        client().delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/package/cancel")
                        .queryParam("tracking_id", trackingId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .toBodilessEntity();
    }

    public Map<String, Object> track(String trackingId) {
        return client().get()
                .uri("/api/v1/track/{id}", trackingId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private RestClient client() {
        return restClientBuilder.clone().baseUrl(properties.getBaseUrl()).build();
    }

    public List<com.zuufa.delivery.provider.ekart.dto.EkartAddress> addresses(String authorization) {
        return client().get().uri("/api/v2/addresses").header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve().body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> addAddress(String authorization, Map<String, Object> body) {
        return client().post().uri("/api/v2/address").header(HttpHeaders.AUTHORIZATION, authorization)
                .body(body).retrieve().body(new ParameterizedTypeReference<>() {});
    }

    public List<Map<String, Object>> webhooks(String authorization) {
        return client().get().uri("/api/v2/webhook").header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve().body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> saveWebhook(String authorization, String id, Map<String, Object> body) {
        RestClient.RequestBodySpec request = id == null ? client().post().uri("/api/v2/webhook")
                : client().put().uri("/api/v2/webhook/{id}", id);
        return request.header(HttpHeaders.AUTHORIZATION, authorization).body(body).retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
