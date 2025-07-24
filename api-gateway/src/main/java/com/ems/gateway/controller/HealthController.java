package com.ems.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class HealthController {

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "API Gateway");
        response.put("timestamp", System.currentTimeMillis());
        return Mono.just(response);
    }

    @GetMapping("/info")
    public Mono<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "EMS API Gateway");
        response.put("version", "1.0.0");
        response.put("description", "Employee Management System API Gateway with JWT Authentication");
        return Mono.just(response);
    }
}
