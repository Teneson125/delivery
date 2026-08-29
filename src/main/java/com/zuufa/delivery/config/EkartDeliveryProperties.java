package com.zuufa.delivery.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "delivery.providers.ekart")
public class EkartDeliveryProperties {

    private String baseUrl = "https://app.elite.ekartlogistics.in";
    private boolean sandbox = true;
    private boolean liveCallsEnabled = false;
}
