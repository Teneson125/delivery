package com.zuufa.delivery.provider;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.exception.NotFoundException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DeliveryProviderFactory {

    private final Map<DeliveryProviderCode, DeliveryProvider> providers = new EnumMap<>(DeliveryProviderCode.class);

    public DeliveryProviderFactory(List<DeliveryProvider> providerList) {
        providerList.forEach(provider -> providers.put(provider.code(), provider));
    }

    public DeliveryProvider getProvider(DeliveryProviderCode code) {
        DeliveryProvider provider = providers.get(code);
        if (provider == null) {
            throw new NotFoundException("Delivery provider not found");
        }
        return provider;
    }
}
