package com.ems.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Gateway filter to extract JWT token from authentication responses and set it as an HTTP-only cookie
 */
@Component
public class AuthTokenCookieFilter extends AbstractGatewayFilterFactory<AuthTokenCookieFilter.Config> {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenCookieFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Pre-compiled token field names for better performance
    private static final String[] TOKEN_FIELDS = {"token", "accessToken", "access_token", "jwt", "authToken", "auth_token"};
    
    // Content type patterns for faster checks
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    public AuthTokenCookieFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter((exchange, chain) -> {
            // Only log at debug level to reduce overhead
            if (logger.isDebugEnabled()) {
                logger.debug("AuthTokenCookieFilter: Processing request to {}", exchange.getRequest().getPath());
            }
            
            // Early check - only process responses from auth endpoints
            String path = exchange.getRequest().getPath().value();
            if (!isAuthEndpoint(path)) {
                return chain.filter(exchange);
            }
            
            return chain.filter(exchange.mutate().response(createResponseDecorator(exchange, config)).build());
        }, -2); // Higher priority than NettyWriteResponseFilter (-1)
    }
    
    private boolean isAuthEndpoint(String path) {
        // Only process authentication endpoints that might return tokens
        return path.contains("/auth/") || path.contains("/login") || path.contains("/authenticate");
    }
    
    private ServerHttpResponseDecorator createResponseDecorator(ServerWebExchange exchange, Config config) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        
        return new ServerHttpResponseDecorator(originalResponse) {
            @Override
            @NonNull
            public Mono<Void> writeWith(@NonNull org.reactivestreams.Publisher<? extends DataBuffer> body) {
                var statusCode = getStatusCode();
                
                // Early exit for non-success responses
                if (statusCode == null || !statusCode.is2xxSuccessful()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("AuthTokenCookieFilter: Skipping non-2xx response: {}", statusCode);
                    }
                    return super.writeWith(body);
                }
                
                // Check content type before processing
                String contentType = getHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.toLowerCase().contains(JSON_CONTENT_TYPE)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("AuthTokenCookieFilter: Skipping non-JSON response: {}", contentType);
                    }
                    return super.writeWith(body);
                }
                
                if (logger.isDebugEnabled()) {
                    logger.debug("AuthTokenCookieFilter: Processing JSON response for token extraction");
                }
                
                Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                
                return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                    // Use buffer() instead of collectList() for better memory efficiency
                    DataBuffer joinedBuffer = bufferFactory.join(dataBuffers);
                    byte[] content = new byte[joinedBuffer.readableByteCount()];
                    joinedBuffer.read(content);
                    DataBufferUtils.release(joinedBuffer);
                    
                    // Process token extraction asynchronously to avoid blocking
                    AuthTokenCookieFilter.this.processTokenExtraction(originalResponse, content, config);
                    
                    return bufferFactory.wrap(content);
                }));
            }
        };
    }
    
    private void processTokenExtraction(ServerHttpResponse response, byte[] content, Config config) {
        try {
            // Performance optimization: check response size limit
            if (content.length > config.getMaxResponseSizeBytes()) {
                if (config.isDebugLogging() && logger.isDebugEnabled()) {
                    logger.debug("AuthTokenCookieFilter: Response too large ({} bytes), skipping token extraction", content.length);
                }
                return;
            }
            
            String responseBody = new String(content, StandardCharsets.UTF_8);
            
            // Limit response body logging to debug level and truncate if too long
            if (config.isDebugLogging() && logger.isDebugEnabled()) {
                String logBody = responseBody.length() > 500 ? 
                    responseBody.substring(0, 500) + "..." : responseBody;
                logger.debug("AuthTokenCookieFilter: Response body: {}", logBody);
            }
            
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String token = extractToken(jsonNode);
            
            if (token != null && !token.isEmpty()) {
                setTokenCookie(response, token, config);
                if (config.isDebugLogging() && logger.isDebugEnabled()) {
                    logger.debug("JWT token cookie set successfully for auth response");
                }
            } else {
                if (config.isDebugLogging() && logger.isDebugEnabled()) {
                    logger.debug("No token found in response body");
                }
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to parse auth response or extract token: {}", e.getMessage());
            }
        }
    }
    
    private String extractToken(JsonNode jsonNode) {
        // Use pre-compiled array for better performance
        for (String field : TOKEN_FIELDS) {
            JsonNode tokenNode = jsonNode.get(field);
            if (tokenNode != null && !tokenNode.isNull()) {
                return tokenNode.asText();
            }
        }
        return null;
    }
    
    private void setTokenCookie(ServerHttpResponse response, String token, Config config) {
        // Build cookie efficiently with pre-configured settings
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(config.getCookieName(), token)
                .httpOnly(config.isHttpOnly())
                .secure(config.isSecure())
                .sameSite(config.getSameSite())
                .path(config.getPath())
                .maxAge(config.getMaxAge());
        
        // Only add domain if specified to avoid unnecessary header size
        if (config.getDomain() != null && !config.getDomain().isEmpty()) {
            cookieBuilder.domain(config.getDomain());
        }
        
        response.addCookie(cookieBuilder.build());
    }
    
    public static class Config {
        private String cookieName = "authToken";
        private boolean httpOnly = true;
        private boolean secure = false; // Set to true in production with HTTPS
        private String sameSite = "Lax";
        private Duration maxAge = Duration.ofHours(24);
        private String domain;
        private String path = "/";
        
        // Performance optimization: limit response body size to process
        private int maxResponseSizeBytes = 10240; // 10KB default
        
        // Performance optimization: enable/disable detailed logging
        private boolean debugLogging = false;
        
        // Getters and setters
        public String getCookieName() { return cookieName; }
        public void setCookieName(String cookieName) { this.cookieName = cookieName; }
        
        public boolean isHttpOnly() { return httpOnly; }
        public void setHttpOnly(boolean httpOnly) { this.httpOnly = httpOnly; }
        
        public boolean isSecure() { return secure; }
        public void setSecure(boolean secure) { this.secure = secure; }
        
        public String getSameSite() { return sameSite; }
        public void setSameSite(String sameSite) { this.sameSite = sameSite; }
        
        public Duration getMaxAge() { return maxAge; }
        public void setMaxAge(Duration maxAge) { this.maxAge = maxAge; }
        
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public int getMaxResponseSizeBytes() { return maxResponseSizeBytes; }
        public void setMaxResponseSizeBytes(int maxResponseSizeBytes) { this.maxResponseSizeBytes = maxResponseSizeBytes; }
        
        public boolean isDebugLogging() { return debugLogging; }
        public void setDebugLogging(boolean debugLogging) { this.debugLogging = debugLogging; }
    }
}
