package com.zuufa.delivery.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuufa.delivery.dto.EkartProviderConfigRequest;
import com.zuufa.delivery.dto.EkartProviderConfigResponse;
import com.zuufa.delivery.dto.EkartProviderCredentialsResponse;
import com.zuufa.delivery.dto.EkartProviderSettingsRequest;
import com.zuufa.delivery.dto.ProviderConnectionTestResponse;
import com.zuufa.delivery.entity.DeliveryProviderConfig;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import com.zuufa.delivery.provider.ekart.EkartAuthClient;
import com.zuufa.delivery.provider.ekart.dto.EkartCredentials;
import com.zuufa.delivery.repository.DeliveryProviderConfigRepository;
import com.zuufa.delivery.service.DeliveryProviderSettingsService;
import com.zuufa.exception.BadRequestException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DeliveryProviderSettingsServiceImpl implements DeliveryProviderSettingsService {

    private final DeliveryProviderConfigRepository providerConfigRepository;
    private final ObjectMapper objectMapper;
    private final EkartAuthClient ekartAuthClient;
    private final com.zuufa.delivery.provider.ekart.EkartCredentialCipher credentialCipher;
    private final com.zuufa.delivery.service.EkartAccountService accountService;
    private final com.zuufa.delivery.config.EkartDeliveryProperties properties;

    @Override
    @Transactional(readOnly = true)
    public EkartProviderConfigResponse getEkartConfig(UUID tenantId) {
        return toResponse(getOrNewConfig(tenantId));
    }

    @Override
    @Transactional
    public EkartProviderConfigResponse saveEkartConfig(UUID tenantId, EkartProviderConfigRequest request) {
        DeliveryProviderConfig config = getOrNewConfig(tenantId);
        EkartProviderSettingsRequest previousSettings = readSettings(config.getSettingsJson());
        config.setEnabled(request.enabled());
        config.setPriority(50);
        if (request.credentials() != null) {
            config.setEncryptedCredentials(credentialCipher.encrypt(toJson(new EkartCredentials(
                    request.credentials().clientId().trim(),
                    request.credentials().username().trim(),
                    request.credentials().password(),
                    trimToNull(request.credentials().merchantCode())
            ))));
            ekartAuthClient.invalidate(tenantId);
        } else if (StringUtils.hasText(config.getEncryptedCredentials()) && config.getEncryptedCredentials().stripLeading().startsWith("{")) {
            config.setEncryptedCredentials(credentialCipher.encrypt(config.getEncryptedCredentials()));
        }
        EkartProviderSettingsRequest settings = request.settings() == null
                ? readSettings(config.getSettingsJson()) : request.settings();
        String pickup = trimToNull(settings.pickupAddressAlias());
        String returns = trimToNull(settings.returnAddressAlias());
        String mode = StringUtils.hasText(settings.paymentMode()) ? settings.paymentMode() : "Prepaid";
        String service = StringUtils.hasText(settings.serviceType()) ? settings.serviceType() : "SURFACE";
        if (!java.util.Set.of("Prepaid", "COD").contains(mode) || !java.util.Set.of("SURFACE", "EXPRESS").contains(service)) {
            throw new BadRequestException("Invalid Ekart payment mode or service type");
        }
        boolean changedAddresses = !java.util.Objects.equals(pickup, previousSettings.pickupAddressAlias())
                || !java.util.Objects.equals(returns, previousSettings.returnAddressAlias()) || request.credentials() != null;
        String pincode = changedAddresses ? null : previousSettings.pickupPincode();
        if (request.enabled() || (changedAddresses && (pickup != null || returns != null))) {
            var addresses = accountService.addresses(config);
            var selected = accountService.requireAddress(addresses, pickup);
            accountService.requireAddress(addresses, returns);
            pincode = selected.pincode();
            if (pincode == null || !pincode.matches("[1-9][0-9]{5}")) throw new BadRequestException("Ekart pickup address has an invalid pincode");
        }
        config.setSettingsJson(toJson(new EkartProviderSettingsRequest(pincode, pickup, returns, mode, service)));
        return toResponse(providerConfigRepository.save(config));
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderConnectionTestResponse testEkartConnection(UUID tenantId) {
        DeliveryProviderConfig config = getOrNewConfig(tenantId);
        if (!StringUtils.hasText(config.getEncryptedCredentials())) {
            return new ProviderConnectionTestResponse(false, "Ekart credentials are not configured.");
        }
        if (!properties.isLiveCallsEnabled()) {
            return new ProviderConnectionTestResponse(false, "Ekart API calls are disabled on this server.");
        }
        ekartAuthClient.invalidate(tenantId);
        try {
            EkartCredentials credentials = objectMapper.readValue(credentialCipher.decrypt(config.getEncryptedCredentials()), EkartCredentials.class);
            ekartAuthClient.getAuthorizationHeader(
                    new DeliveryProviderContext(
                            tenantId,
                            config.getEncryptedCredentials(),
                            config.getSettingsJson(),
                            null,
                            BigDecimal.ZERO,
                            null,
                            0,
                            0
                    ),
                    credentials
            );
            return new ProviderConnectionTestResponse(true, "Ekart connection verified.");
        } catch (Exception error) {
            return new ProviderConnectionTestResponse(false, "Unable to verify Ekart connection.");
        }
    }

    private DeliveryProviderConfig getOrNewConfig(UUID tenantId) {
        return providerConfigRepository.findByTenantIdAndProvider(tenantId, DeliveryProviderCode.EKART)
                .orElseGet(() -> {
                    DeliveryProviderConfig config = new DeliveryProviderConfig();
                    config.setTenantId(tenantId);
                    config.setProvider(DeliveryProviderCode.EKART);
                    config.setEnabled(false);
                    config.setPriority(50);
                    return config;
                });
    }

    private EkartProviderConfigResponse toResponse(DeliveryProviderConfig config) {
        EkartCredentials credentials = readCredentials(config.getEncryptedCredentials());
        return new EkartProviderConfigResponse(
                DeliveryProviderCode.EKART,
                config.isEnabled(),
                StringUtils.hasText(config.getEncryptedCredentials()),
                credentials == null ? null : new EkartProviderCredentialsResponse(
                        mask(credentials.clientId()),
                        mask(credentials.username()),
                        "saved",
                        mask(credentials.merchantCode())
                ),
                readSettings(config.getSettingsJson()),
                properties.isLiveCallsEnabled(), credentialCipher.configured(),
                "AWAITING_EKART_SIGNATURE_CONTRACT"
        );
    }

    private EkartCredentials readCredentials(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(credentialCipher.decrypt(value), EkartCredentials.class);
        } catch (JsonProcessingException | RuntimeException error) {
            return null;
        }
    }

    private EkartProviderSettingsRequest readSettings(String value) {
        if (!StringUtils.hasText(value)) {
            return new EkartProviderSettingsRequest(null, null, null, "Prepaid", "SURFACE");
        }
        try {
            return objectMapper.readValue(value, EkartProviderSettingsRequest.class);
        } catch (JsonProcessingException error) {
            return new EkartProviderSettingsRequest(null, null, null, "Prepaid", "SURFACE");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException error) {
            throw new BadRequestException("Invalid Ekart provider configuration");
        }
    }

    private String mask(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 6) {
            return "******";
        }
        return trimmed.substring(0, 3) + "******" + trimmed.substring(trimmed.length() - 3);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
