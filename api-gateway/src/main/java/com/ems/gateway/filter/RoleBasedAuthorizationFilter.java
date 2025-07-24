package com.ems.gateway.filter;

import com.ems.gateway.enums.Role;
import com.ems.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleBasedAuthorizationFilter extends AbstractGatewayFilterFactory<RoleBasedAuthorizationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public RoleBasedAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String token = null;
            
            // First, try to get token from Authorization header
            boolean hasAuthHeader = request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
            if (hasAuthHeader) {
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                
                // Check if Authorization header has the correct format
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
            
            // If no token from header, try to get it from auth_token cookie
            boolean hasAuthCookie = request.getCookies().containsKey("auth_token");
            if ((token == null || token.trim().isEmpty()) && hasAuthCookie) {
                var cookieValues = request.getCookies().get("auth_token");
                if (cookieValues != null && !cookieValues.isEmpty()) {
                    token = cookieValues.get(0).getValue();
                }
            }
            
            // If still no token found, return unauthorized
            if (token == null || token.trim().isEmpty()) {
                return this.onError(exchange, "Authorization header or auth_token cookie is required", HttpStatus.UNAUTHORIZED);
            }
            
            // Make token final for use in lambda
            final String finalToken = token;
            
            try {
                // Parse and validate the token in a single operation
                JwtUtil.TokenInfo tokenInfo = jwtUtil.parseAndValidateToken(finalToken);
                if (tokenInfo == null) {
                    return this.onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
                }
                
                // Check if user is active
                if (!"TRUE".equalsIgnoreCase(tokenInfo.getActivityStatus())) {
                    return this.onError(exchange, "User account is not active", HttpStatus.FORBIDDEN);
                }
                
                // Check role-based authorization
                if (!hasRequiredRole(tokenInfo.getRole(), config.getAllowedRoles())) {
                    return this.onError(exchange, 
                        String.format("Access denied. Required roles: %s, User role: %s", 
                            config.getAllowedRoles(), tokenInfo.getRole()), 
                        HttpStatus.FORBIDDEN);
                }
                
                // Add user information to request headers for downstream services
                ServerHttpRequest.Builder requestBuilder = request.mutate()
                        .header("X-User-Id", tokenInfo.getUserId());
                
                // Add Authorization header if it doesn't exist (e.g., when token came from cookie)
                if (!hasAuthHeader) {
                    requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + finalToken);
                }
                
                ServerHttpRequest modifiedRequest = requestBuilder.build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    if(hasAuthCookie)
                        return;
                    
                    ServerHttpResponse response = exchange.getResponse();
                    ResponseCookie cookie = ResponseCookie.from("auth_token", finalToken)
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .sameSite("Strict")
                                .maxAge(3600) // 1 hour
                                .build();

                    response.addCookie(cookie);
                }));
                
            } catch (Exception e) {
                return this.onError(exchange, "JWT token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean hasRequiredRole(Role userRole, List<Role> allowedRoles) {
        if (allowedRoles == null || allowedRoles.isEmpty()) {
            return true; // No role restriction
        }
        return allowedRoles.contains(userRole);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\": \"%s\", \"status\": %d}", err, httpStatus.value());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        private List<Role> allowedRoles;

        public Config() {
        }

        public Config(Role... roles) {
            this.allowedRoles = Arrays.asList(roles);
        }

        public List<Role> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<Role> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

        public Config allowedRoles(Role... roles) {
            this.allowedRoles = Arrays.asList(roles);
            return this;
        }
    }
}
