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
        return restClientBuilder.baseUrl(properties.getBaseUrl()).build();
    }
}
