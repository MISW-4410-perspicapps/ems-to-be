# EMS Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the Employee Management System (EMS) to a Kubernetes cluster.

## Overview

The EMS system consists of:
- **API Gateway**: Spring Cloud Gateway for routing and authentication
- **Backend**: Spring Boot application with modern REST APIs
- **Legacy**: Legacy JSP application
- **MySQL Databases**: Separate databases for backend and legacy systems

## Prerequisites

1. **Kubernetes Cluster**: A running Kubernetes cluster (minikube, kind, or cloud provider)
2. **kubectl**: Kubernetes command-line tool installed and configured
3. **Docker Images**: The application images must be built and available to the cluster
4. **NGINX Ingress Controller** (optional): For external access via ingress

### Building Docker Images

Before deploying, build the required Docker images:

```bash
# From the project root directory
cd Backend && docker build -t ems-backend:latest .
cd ../legacy && docker build -t ems-legacy:latest .
cd ../api-gateway && docker build -t ems-api-gateway:latest .
```

For local clusters (minikube/kind), you may need to load images:
```bash
# For minikube
minikube image load ems-backend:latest
minikube image load ems-legacy:latest
minikube image load ems-api-gateway:latest

# For kind
kind load docker-image ems-backend:latest
kind load docker-image ems-legacy:latest
kind load docker-image ems-api-gateway:latest
```

## Deployment Files

The deployment is organized into the following files:

| File | Description |
|------|-------------|
| `00-namespace.yaml` | Creates the `ems` namespace |
| `01-secrets-configmap.yaml` | Contains secrets (passwords, JWT secret) and configuration |
| `02-persistent-volumes.yaml` | Persistent volumes for database storage and logs |
| `03-backend-mysql.yaml` | Backend MySQL database deployment and service |
| `04-legacy-mysql.yaml` | Legacy MySQL database deployment and service |
| `05-backend-app.yaml` | Backend Spring Boot application deployment and service |
| `06-legacy-app.yaml` | Legacy JSP application deployment and service |
| `07-api-gateway.yaml` | API Gateway deployment and service |
| `08-init-sql-configmaps.yaml` | SQL initialization scripts for databases |
| `09-ingress.yaml` | Ingress configuration for external access |

## Quick Deployment

### Using the Deployment Script

The easiest way to deploy is using the provided script:

```bash
# Make the script executable
chmod +x deploy.sh

# Run the deployment
./deploy.sh
```

### Manual Deployment

Apply the manifests in order:

```bash
kubectl apply -f 00-namespace.yaml
kubectl apply -f 01-secrets-configmap.yaml
kubectl apply -f 08-init-sql-configmaps.yaml
kubectl apply -f 02-persistent-volumes.yaml
kubectl apply -f 03-backend-mysql.yaml
kubectl apply -f 04-legacy-mysql.yaml

# Wait for databases to be ready
kubectl wait --for=condition=available --timeout=300s deployment/backend-mysql -n ems
kubectl wait --for=condition=available --timeout=300s deployment/legacy-mysql -n ems

# Deploy applications
kubectl apply -f 05-backend-app.yaml
kubectl apply -f 06-legacy-app.yaml
kubectl apply -f 07-api-gateway.yaml

# Optional: Setup ingress (requires nginx ingress controller)
kubectl apply -f 09-ingress.yaml
```

## Configuration

### Environment Variables

Key configuration is stored in ConfigMaps and Secrets. You may need to update:

**Secrets** (`01-secrets-configmap.yaml`):
- Database passwords (base64 encoded)
- JWT secret key (base64 encoded)

**ConfigMap** (`01-secrets-configmap.yaml`):
- Database connection details
- Application configuration
- GCP bucket name
- Logging levels

### Persistent Storage

The deployment uses `hostPath` persistent volumes for local development. For production:

1. Change `storageClassName` to your cluster's storage class
2. Update the `hostPath` to appropriate storage solution
3. Consider using dynamic provisioning

## Accessing the Application

### Via LoadBalancer (Default)

```bash
# Get the API Gateway external IP/port
kubectl get svc ems-api-gateway -n ems

# Access the application
# Replace <EXTERNAL-IP> with the actual IP or use localhost for NodePort
curl http://<EXTERNAL-IP>:8080/health
```

### Via Port Forwarding

```bash
# Forward API Gateway port
kubectl port-forward svc/ems-api-gateway 8080:8080 -n ems

# Access at http://localhost:8080
```

### Via Ingress (if configured)

Add to `/etc/hosts`:
```
127.0.0.1 ems.local
127.0.0.1 api-gateway.ems.local
127.0.0.1 backend.ems.local
127.0.0.1 legacy.ems.local
```

Access URLs:
- API Gateway: http://api-gateway.ems.local
- Backend API: http://backend.ems.local
- Legacy App: http://legacy.ems.local
- Combined: http://ems.local/api (API Gateway), http://ems.local/backend, http://ems.local/legacy

## Monitoring and Troubleshooting

### Check Pod Status
```bash
kubectl get pods -n ems
```

### View Logs
```bash
# API Gateway logs
kubectl logs -f deployment/ems-api-gateway -n ems

# Backend logs
kubectl logs -f deployment/ems-backend -n ems

# Legacy logs
kubectl logs -f deployment/ems-legacy -n ems

# Database logs
kubectl logs -f deployment/backend-mysql -n ems
kubectl logs -f deployment/legacy-mysql -n ems
```

### Check Services
```bash
kubectl get svc -n ems
```

### Database Connection
```bash
# Connect to backend database
kubectl exec -it deployment/backend-mysql -n ems -- mysql -u ems_user -p ems_backend

# Connect to legacy database
kubectl exec -it deployment/legacy-mysql -n ems -- mysql -u legacy_user -p ems_legacy
```

### Scaling
```bash
# Scale backend application
kubectl scale deployment ems-backend --replicas=3 -n ems

# Scale API Gateway
kubectl scale deployment ems-api-gateway --replicas=2 -n ems
```

## Resource Requirements

| Component | CPU Request | Memory Request | CPU Limit | Memory Limit |
|-----------|-------------|----------------|-----------|--------------|
| API Gateway | 250m | 256Mi | 500m | 512Mi |
| Backend | 200m | 512Mi | 500m | 1Gi |
| Legacy | 200m | 512Mi | 500m | 1Gi |
| Backend MySQL | 100m | 256Mi | 500m | 512Mi |
| Legacy MySQL | 100m | 256Mi | 500m | 512Mi |

## Cleanup

To remove all EMS resources:

```bash
# Using the cleanup script
chmod +x cleanup.sh
./cleanup.sh

# Or manually
kubectl delete namespace ems
```

**Note**: This will delete all data. Backup important data before cleanup.

## Production Considerations

1. **Security**:
   - Use proper secrets management (HashiCorp Vault, AWS Secrets Manager)
   - Enable RBAC and network policies
   - Use non-root containers

2. **Storage**:
   - Use persistent storage with backups
   - Consider database operators for MySQL

3. **Networking**:
   - Configure proper ingress with TLS
   - Use network policies for micro-segmentation

4. **Monitoring**:
   - Add Prometheus metrics
   - Configure log aggregation
   - Set up health checks and alerts

5. **High Availability**:
   - Run multiple replicas
   - Use anti-affinity rules
   - Configure pod disruption budgets

## Troubleshooting Common Issues

### Images Not Found
- Ensure images are built and available to the cluster
- For local clusters, load images using `minikube image load` or `kind load`

### Database Connection Issues
- Check if databases are ready: `kubectl get pods -n ems`
- Verify service names and ports in configuration
- Check database logs for initialization errors

### Application Startup Issues
- Review application logs
- Verify environment variables and secrets
- Check resource limits and requests

### Ingress Not Working
- Ensure nginx ingress controller is installed
- Check ingress configuration and annotations
- Verify DNS resolution for custom domains
