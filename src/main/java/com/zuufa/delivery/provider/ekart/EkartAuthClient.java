package com.zuufa.delivery.provider.ekart;

import com.zuufa.delivery.config.EkartDeliveryProperties;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import com.zuufa.delivery.provider.ekart.dto.EkartAuthRequest;
import com.zuufa.delivery.provider.ekart.dto.EkartAuthResponse;
import com.zuufa.delivery.provider.ekart.dto.EkartCredentials;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class EkartAuthClient {

    private final EkartDeliveryProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final Map<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

    public EkartAuthClient(EkartDeliveryProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClientBuilder = restClientBuilder;
    }

    public boolean canAuthenticate(DeliveryProviderContext context) {
        return properties.isLiveCallsEnabled()
                && StringUtils.hasText(properties.getBaseUrl())
                && StringUtils.hasText(context.encryptedCredentials());
    }

    public String getAuthorizationHeader(DeliveryProviderContext context, EkartCredentials credentials) {
        String cacheKey = context.tenantId() + ":EKART:" + credentials.clientId();
        CachedToken cached = tokenCache.get(cacheKey);
        if (cached != null && cached.expiresAt().isAfter(Instant.now().plusSeconds(60))) {
            return cached.authorizationHeader();
        }

        EkartAuthResponse response = restClientBuilder.baseUrl(properties.getBaseUrl()).build()
                .post()
                .uri("/integrations/v2/auth/token/{clientId}", credentials.clientId())
                .body(new EkartAuthRequest(credentials.username(), credentials.password()))
                .retrieve()
                .body(EkartAuthResponse.class);
        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new IllegalStateException("Ekart token response is empty");
        }

        String tokenType = StringUtils.hasText(response.tokenType()) ? response.tokenType() : "Bearer";
        String authorizationHeader = tokenType + " " + response.accessToken();
        tokenCache.put(cacheKey, new CachedToken(
                authorizationHeader,
                Instant.now().plusSeconds(Math.max(0, response.expiresIn()))
        ));
        return authorizationHeader;
    }

    private record CachedToken(String authorizationHeader, Instant expiresAt) {
    }
}
