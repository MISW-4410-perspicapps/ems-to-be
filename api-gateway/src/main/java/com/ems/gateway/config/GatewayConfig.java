package com.ems.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway configuration for enhanced logging and monitoring
 */
@Configuration
public class GatewayConfig {

    /**
     * Additional route configuration if needed for specific logging requirements
     * This is optional as routes are primarily defined in application.yaml
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // This can be used to add programmatic routes with specific filters
                // For now, we'll rely on the YAML configuration and our global filter
                .build();
    }
}
