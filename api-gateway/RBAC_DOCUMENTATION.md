# Role-Based Access Control (RBAC) Documentation

## Overview

The API Gateway implements Role-Based Access Control (RBAC) to secure endpoints based on user roles extracted from JWT tokens. This system ensures that users can only access resources appropriate to their role level.

## Role Hierarchy

```
1. Admin    (ID: 1) - Full access to all resources
2. Manager  (ID: 2) - Read/write access to most resources
3. Employee (ID: 3) - Limited access, mainly to own profile
4. NA       (ID: 4) - No access (inactive/unknown users)
```

## JWT Token Structure

The system expects JWT tokens with the following payload structure:

```json
{
  "firstname": "Andres",
  "role": "1",
  "userId": "1",
  "activityStatus": "TRUE",
  "username": "andresclavijor",
  "sub": "andresclavijor",
  "iat": 1753322999,
  "exp": 1753326599
}
```

### Required Claims:
- `role`: String representing role ID (1-4)
- `userId`: User's unique identifier
- `username`: User's username
- `firstname`: User's first name
- `activityStatus`: "TRUE" for active users, "FALSE" for inactive
- `sub`: Subject (typically username)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

## Access Control Matrix

| Endpoint Pattern | Method | Admin | Manager | Employee | Description |
|-----------------|--------|-------|---------|----------|-------------|
| `/api/auth/**` | ALL | ✅ | ✅ | ✅ | Public - Authentication endpoints |
| `/api/gateway/**` | ALL | ✅ | ✅ | ✅ | Public - Gateway health endpoints |
| `/api/users/**` | ALL | ✅ | ✅ | ❌ | User management (Admin/Manager only) |
| `/api/employees/profile**` | ALL | ✅ | ✅ | ✅ | Employee profile access |
| `/api/employees` | POST | ✅ | ❌ | ❌ | Create employee (Admin only) |
| `/api/employees/**` | PUT/DELETE | ✅ | ❌ | ❌ | Update/Delete employee (Admin only) |
| `/api/employees/**` | GET | ✅ | ✅ | ❌ | View employees (Admin/Manager) |
| `/api/departments` | POST | ✅ | ❌ | ❌ | Create department (Admin only) |
| `/api/departments/**` | PUT/DELETE | ✅ | ❌ | ❌ | Update/Delete department (Admin only) |
| `/api/departments/**` | GET | ✅ | ✅ | ❌ | View departments (Admin/Manager) |

## Headers Added to Downstream Services

When a request is successfully authorized, the gateway adds the following headers for downstream services:

```
X-User-Id: 1
X-Username: andresclavijor
X-User-Role: 1
X-User-Role-Name: Admin
X-User-FirstName: Andres
X-User-ActivityStatus: TRUE
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

### 403 Forbidden
```json
{
  "error": "User account is not active",
  "status": 403
}
```

```json
{
  "error": "Access denied. Required roles: [Admin, Manager], User role: Employee",
  "status": 403
}
```

## Configuration Examples

### Route with Admin-only Access
```java
.route("admin-only", r -> r.path("/api/admin/**")
    .filters(f -> f.filter(roleBasedAuthorizationFilterFactory.apply(
        new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN))))
    .uri("http://localhost:8080"))
```

### Route with Multiple Role Access
```java
.route("multi-role", r -> r.path("/api/shared/**")
    .filters(f -> f.filter(roleBasedAuthorizationFilterFactory.apply(
        new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN, Role.MANAGER))))
    .uri("http://localhost:8080"))
```

### Route with Method-specific Access Control
```java
.route("method-specific", r -> r.path("/api/resource/**").and().method("POST", "PUT", "DELETE")
    .filters(f -> f.filter(roleBasedAuthorizationFilterFactory.apply(
        new RoleBasedAuthorizationFilterFactory.Config().allowedRoles(Role.ADMIN))))
    .uri("http://localhost:8080"))
```

## Testing Role-Based Access

### Admin User Test
```bash
# JWT token with role "1" (Admin)
curl -H "Authorization: Bearer <admin-jwt-token>" \
     http://localhost:8080/api/employees
# Should succeed
```

### Manager User Test
```bash
# JWT token with role "2" (Manager)
curl -H "Authorization: Bearer <manager-jwt-token>" \
     http://localhost:8080/api/employees
# Should succeed (read access)

curl -X POST -H "Authorization: Bearer <manager-jwt-token>" \
     http://localhost:8080/api/employees
# Should fail with 403 Forbidden
```

### Employee User Test
```bash
# JWT token with role "3" (Employee)
curl -H "Authorization: Bearer <employee-jwt-token>" \
     http://localhost:8080/api/employees/profile
# Should succeed (own profile access)

curl -H "Authorization: Bearer <employee-jwt-token>" \
     http://localhost:8080/api/employees
# Should fail with 403 Forbidden
```

### Inactive User Test
```bash
# JWT token with activityStatus "FALSE"
curl -H "Authorization: Bearer <inactive-jwt-token>" \
     http://localhost:8080/api/employees/profile
# Should fail with 403 Forbidden
```

## Security Features

1. **JWT Validation**: All tokens are validated for signature and expiration
2. **Activity Status Check**: Inactive users are denied access regardless of role
3. **Role Hierarchy**: Clear role-based permissions
4. **Method-specific Control**: Different access levels for read vs write operations
5. **User Context Forwarding**: Downstream services receive user information in headers
6. **Comprehensive Error Messages**: Clear feedback on authorization failures

## Best Practices

1. **Principle of Least Privilege**: Users get minimum required access
2. **Active Status Monitoring**: Inactive users are immediately blocked
3. **Audit Trail**: All authorization decisions are logged
4. **Token Expiration**: Short-lived tokens reduce security risks
5. **Role Segregation**: Clear separation between administrative and operational roles

## Extending the System

### Adding New Roles
1. Update the `Role` enum with new role values
2. Update access control matrix documentation
3. Configure routes with appropriate role restrictions

### Adding New Endpoints
1. Determine appropriate role access level
2. Configure route with role-based filter
3. Update documentation and tests

### Custom Authorization Logic
The `RoleBasedAuthorizationFilterFactory` can be extended to support:
- Resource-specific permissions
- Time-based access control
- IP-based restrictions
- Custom business logic
