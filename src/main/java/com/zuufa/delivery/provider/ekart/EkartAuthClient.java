package com.zuufa.delivery.provider.ekart;

import com.zuufa.delivery.config.EkartDeliveryProperties;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EkartAuthClient {

    private final EkartDeliveryProperties properties;

    public EkartAuthClient(EkartDeliveryProperties properties) {
        this.properties = properties;
    }

    public boolean canAuthenticate(DeliveryProviderContext context) {
        return properties.isLiveCallsEnabled()
                && StringUtils.hasText(properties.getBaseUrl())
                && StringUtils.hasText(context.encryptedCredentials());
    }
}
