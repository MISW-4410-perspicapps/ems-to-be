# EMS API Gateway

A Spring Cloud Gateway implementation for the Employee Management System (EMS) with JWT authentication.

## Features

- **Role-Based Access Control (RBAC)**: Controls access based on user roles extracted from JWT tokens
- **Direct Service Routing**: Routes requests directly to microservices without service discovery
- **Route Management**: Routes requests to appropriate microservices
- **CORS Support**: Configured for cross-origin requests
- **Health Monitoring**: Actuator endpoints for monitoring
- **Error Handling**: Global error handling with proper HTTP status codes
- **User Context Forwarding**: Passes user information to downstream services

## Architecture

The API Gateway acts as a single entry point for all client requests and routes them to the appropriate microservices:

```
Client → API Gateway → Microservices (Direct URLs)
                ↓
        JWT Validation + RBAC
```

## Role-Based Access Control

The gateway implements a comprehensive RBAC system with the following roles:

- **Admin (ID: 1)**: Full access to all resources
- **Manager (ID: 2)**: Read/write access to most resources  
- **Employee (ID: 3)**: Limited access, mainly to own profile
- **NA (ID: 4)**: No access (inactive/unknown users)

For detailed RBAC documentation, see [RBAC_DOCUMENTATION.md](RBAC_DOCUMENTATION.md).

## Routes Configuration

### Public Routes (No Authentication)
- `GET /api/auth/**` - Authentication service routes
- `GET /api/gateway/health` - Gateway health check
- `GET /api/gateway/info` - Gateway information
- `GET /actuator/**` - Actuator endpoints

### Protected Routes (Role-Based Access)
- `GET|POST|PUT|DELETE /api/users/**` - User service routes (Admin, Manager only)
- `GET|POST|PUT|DELETE /api/employees/profile/**` - Employee profile (All authenticated users)
- `POST /api/employees` - Create employee (Admin only)
- `PUT|DELETE /api/employees/**` - Update/Delete employee (Admin only)
- `GET /api/employees/**` - View employees (Admin, Manager)
- `POST /api/departments` - Create department (Admin only)
- `PUT|DELETE /api/departments/**` - Update/Delete department (Admin only)  
- `GET /api/departments/**` - View departments (Admin, Manager)

## JWT Authentication

### How it works
1. Client sends request with `Authorization: Bearer <token>` header
2. Gateway validates the JWT token using the configured secret
3. If valid, request is forwarded to the target service with user info in headers
4. If invalid, returns 401 Unauthorized

### Token Requirements
- Must be a valid JWT token
- Must not be expired
- Must be signed with the configured secret key

### Headers Added to Downstream Services
When a JWT is validated, the gateway adds the following headers to requests sent to downstream services:
- `X-User-Id`: The username extracted from the JWT token

## Configuration

### Required Environment Variables
```properties
jwt.secret=your-secret-key-here
jwt.expiration=86400
```

### Application Configuration (application.yaml)
```yaml
spring:
  application:
    name: gateway
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              enabled: false

server:
  port: 8080

jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.ems.gateway: DEBUG
```

## Service Endpoints

The gateway routes requests to the following microservices:

- **Auth Service**: http://localhost:8081
- **User Service**: http://localhost:8082  
- **Employee Service**: http://localhost:8083
- **Department Service**: http://localhost:8084

Make sure these services are running on their respective ports.

## Running the Gateway

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Required microservices running on their designated ports

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

The gateway will start on port 8080.

### Docker Run (if available)
```bash
docker build -t ems-gateway .
docker run -p 8080:8080 ems-gateway
```

## Testing the Gateway

### Health Check
```bash
curl http://localhost:8080/api/gateway/health
```

### Testing JWT Authentication
1. **Without token (should fail):**
```bash
curl -X GET http://localhost:8080/api/users/profile
```

2. **With valid token:**
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer your-jwt-token-here"
```

3. **With invalid token (should fail):**
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer invalid-token"
```

## Error Responses

### 401 Unauthorized
```json
{
  "error": "Authorization header is missing",
  "status": 401
}
```

```json
{
  "error": "JWT token is not valid",
  "status": 401
}
```

### 500 Internal Server Error
```json
{
  "error": "InternalServerError",
  "status": 500,
  "message": "Error message details"
}
```

## Monitoring

### Actuator Endpoints
- `GET /actuator/health` - Application health
- `GET /actuator/info` - Application information
- `GET /actuator/gateway/routes` - View configured routes

### Logs
Enable debug logging to see gateway routing details:
```properties
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.com.ems.gateway=DEBUG
```

## Security Considerations

1. **JWT Secret**: Use a strong, unique secret key in production
2. **HTTPS**: Always use HTTPS in production environments
3. **Token Expiration**: Configure appropriate token expiration times
4. **CORS**: Configure CORS policies according to your frontend requirements

## Development Notes

### Adding New Routes
To add new protected routes, update the `GatewayConfig.java`:

```java
.route("new-service", r -> r.path("/api/newservice/**")
    .filters(f -> f.filter(jwtAuthenticationFilterFactory.apply(new JwtAuthenticationFilterFactory.Config())))
    .uri("http://localhost:8085"))
```

### Adding Public Routes
For public routes (no authentication):

```java
.route("public-service", r -> r.path("/api/public/**")
    .uri("http://localhost:8086"))
```

## Troubleshooting

### Common Issues

1. **Service Connection Failed**
   - Ensure target microservices are running on their configured ports
   - Check service URLs in route configuration
   - Verify network connectivity between gateway and services

2. **JWT Validation Fails**
   - Verify the JWT secret matches between auth service and gateway
   - Check token expiration
   - Ensure proper `Bearer` prefix in Authorization header

3. **Service Not Found**
   - Verify target services are running on expected ports
   - Check service URLs in `GatewayConfig.java`
   - Ensure services are accessible from the gateway

4. **CORS Issues**
   - Update CORS configuration in `SecurityConfig.java`
   - Ensure proper preflight handling
