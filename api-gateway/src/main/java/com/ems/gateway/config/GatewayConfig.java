package com.ems.gateway.config;

import com.ems.gateway.enums.Role;
import com.ems.gateway.filter.RoleBasedAuthorizationFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    
    @Autowired
    private RoleBasedAuthorizationFilterFactory rbacFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Public routes - no authentication required
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("http://localhost:8081"))
                
                // Gateway health endpoints - no authentication required
                .route("gateway-health", r -> r.path("/api/gateway/**")
                        .uri("http://localhost:8080"))
                
                // User Management - Admin and Manager access only
                .route("user-management", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(rbacFilter.apply(
                                new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN, Role.MANAGER))))
                        .uri("http://localhost:8082"))
                
                // Employee Profile Access - All authenticated users can access their own profile
                .route("ems-legacy", r -> r.path("/ems/legacy/login")
                        .filters(f -> f
                                .rewritePath("/ems/legacy/login", "/ems/login")
                                .filter(rbacFilter.apply(
                                new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN, Role.MANAGER, Role.EMPLOYEE))))
                        .uri("http://localhost:8081"))
                
                // Employee Management - Admin access only for create/update/delete operations
                .route("employee-admin", r -> r.path("/api/employees").and().method("POST")
                        .or().path("/api/employees/**").and().method("PUT", "DELETE")
                        .filters(f -> f.filter(rbacFilter.apply(
                                new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN))))
                        .uri("http://localhost:8083"))
                
                // Employee Read Access - Admin and Manager can view all employees
                .route("employee-read", r -> r.path("/api/employees", "/api/employees/**").and().method("GET")
                        .filters(f -> f.filter(rbacFilter.apply(
                                new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN, Role.MANAGER))))
                        .uri("http://localhost:8083"))
                
                // Department Management - Admin access only for create/update/delete
                .route("department-admin", r -> r.path("/api/departments").and().method("POST")
                        .or().path("/api/departments/**").and().method("PUT", "DELETE")
                        .filters(f -> f.filter(rbacFilter.apply(
                                new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN))))
                        .uri("http://localhost:8084"))
                        
                // Department Read Access - Admin and Manager can view departments
                .route("department-read", r -> r.path("/api/departments", "/api/departments/**").and().method("GET")
                        .filters(f -> f.filter(rbacFilter.apply(
                                new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN, Role.MANAGER))))
                        .uri("http://localhost:8084"))
                        
                .build();
    }
}
