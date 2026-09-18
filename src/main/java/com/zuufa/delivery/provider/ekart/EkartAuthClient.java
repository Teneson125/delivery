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
        if (!canAuthenticate(context)) {
            throw new com.zuufa.exception.ServiceException(503, "Ekart API calls are disabled on this server.");
        }
        // Ciphertext changes whenever credentials are saved, preventing reuse after rotation.
        String cacheKey = context.tenantId() + ":EKART:" + credentials.clientId() + ":" + fingerprint(context.encryptedCredentials());
        CachedToken cached = tokenCache.get(cacheKey);
        if (cached != null && cached.expiresAt().isAfter(Instant.now().plusSeconds(60))) {
            return cached.authorizationHeader();
        }

        EkartAuthResponse response = restClientBuilder.clone().baseUrl(properties.getBaseUrl()).build()
                .post()
                .uri("/integrations/v2/auth/token/{clientId}", credentials.clientId())
                .body(new EkartAuthRequest(credentials.username(), credentials.password()))
                .retrieve()
                .body(EkartAuthResponse.class);
        if (response == null || !StringUtils.hasText(response.accessToken()) || response.expiresIn() <= 0
                || !"Bearer".equalsIgnoreCase(response.tokenType())) {
            throw new IllegalStateException("Ekart token response is empty");
        }

        String tokenType = StringUtils.hasText(response.tokenType()) ? response.tokenType() : "Bearer";
        String authorizationHeader = tokenType + " " + response.accessToken();
        tokenCache.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(Instant.now()));
        tokenCache.put(cacheKey, new CachedToken(
                authorizationHeader,
                Instant.now().plusSeconds(Math.max(0, response.expiresIn()))
        ));
        return authorizationHeader;
    }

    private record CachedToken(String authorizationHeader, Instant expiresAt) {
    }

    public void invalidate(java.util.UUID tenantId) {
        tokenCache.keySet().removeIf(key -> key.startsWith(tenantId + ":EKART:"));
    }

    private String fingerprint(String value) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
