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
public class RoleBasedAuthorizationFilterFactory extends AbstractGatewayFilterFactory<RoleBasedAuthorizationFilterFactory.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public RoleBasedAuthorizationFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            // Check if Authorization header has the correct format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return this.onError(exchange, "Authorization header must start with Bearer", HttpStatus.UNAUTHORIZED);
            }

            // Extract the token
            String token = authHeader.substring(7);
            
            try {
                // Validate the token first
                if (!jwtUtil.validateToken(token)) {
                    return this.onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
                }
                
                // Extract user information
                String username = jwtUtil.getUsernameFromToken(token);
                String userId = jwtUtil.getUserIdFromToken(token);
                Role userRole = jwtUtil.getRoleFromToken(token);
                String firstName = jwtUtil.getFirstNameFromToken(token);
                String activityStatus = jwtUtil.getActivityStatusFromToken(token);
                
                // Check if user is active
                if (!"TRUE".equalsIgnoreCase(activityStatus)) {
                    return this.onError(exchange, "User account is not active", HttpStatus.FORBIDDEN);
                }
                
                // Check role-based authorization
                if (!hasRequiredRole(userRole, config.getAllowedRoles())) {
                    return this.onError(exchange, 
                        String.format("Access denied. Required roles: %s, User role: %s", 
                            config.getAllowedRoles(), userRole.getName()), 
                        HttpStatus.FORBIDDEN);
                }
                
                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    
                    ResponseCookie cookie = ResponseCookie.from("auth_token", token)
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
