package com.zuufa.delivery.security;

import com.zuufa.security.service.PermitAllProvider;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DeliveryPermitAllProvider implements PermitAllProvider {

    @Override
    public List<String> getPermitAllEndpoints() {
        return List.of(
                "/swagger-ui/**",
                "/v3/api-docs/**"
        );
    }
}
