package com.ems.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter to log all incoming requests to the API Gateway
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().toString();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String fullPath = query != null ? path + "?" + query : path;
        
        // Continue with the filter chain and log the response
        return chain.filter(exchange).doFinally(signalType -> {
            int statusCode = -1;
            if (exchange.getResponse() != null && exchange.getResponse().getStatusCode() != null) {
                statusCode = exchange.getResponse().getStatusCode().value();
            }
            
            // Simple, clean log format: METHOD PATH -> STATUS
            logger.info("{} {} -> {}", method, fullPath, statusCode);
        });
    }
    
    @Override
    public int getOrder() {
        // Set high priority to ensure this filter runs first
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
