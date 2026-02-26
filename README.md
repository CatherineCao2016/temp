# Payment Application

A Spring Boot-based payment processing application with automated CI/CD deployment to OpenShift.

## 🚀 Quick Start

### Prerequisites
- Java 11 or later
- Maven 3.9+
- Docker (for local builds)
- OpenShift CLI (oc) for deployment

### Local Development

```bash
# Clone the repository
git clone <repository-url>
cd payment-app

# Build the application
mvn clean package

# Run locally
mvn spring-boot:run

# Access the application
open http://localhost:8080
```

### Running Tests

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Generate code coverage report
mvn jacoco:report
```

## 🏗️ Architecture

### Technology Stack
- **Framework:** Spring Boot 2.7.x
- **Language:** Java 11
- **Build Tool:** Maven
- **Container:** Docker
- **Orchestration:** OpenShift/Kubernetes
- **CI/CD:** GitHub Actions

### Application Structure
```
payment-app/
├── src/
│   ├── main/
│   │   ├── java/com/demo/payment/
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── model/           # Domain models
│   │   │   ├── repository/      # Data access
│   │   │   ├── dto/             # Data transfer objects
│   │   │   └── config/          # Configuration classes
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/       # Web templates
│   └── test/                    # Test classes
├── .github/workflows/           # CI/CD pipelines
├── openshift/                   # Kubernetes manifests
├── Dockerfile                   # Container image definition
└── pom.xml                      # Maven configuration
```

## 🔄 CI/CD Pipeline

### Automated Deployment

The application uses GitHub Actions for automated CI/CD:

**Pipeline Stages:**
1. **Build & Test** - Compile code and run tests
2. **Security Scan** - OWASP dependency vulnerability check
3. **Build Image** - Create container image in OpenShift
4. **Deploy Staging** - Automatic deployment to staging
5. **Deploy Production** - Manual approval required

**Workflow File:** `.github/workflows/cicd.yaml`

### Deployment Environments

#### Staging
- **URL:** https://payment-app-staging.apps.itz-9i05h3.hub01-lb.techzone.ibm.com
- **Deployment:** Automatic on push to `main` or `develop`
- **Purpose:** Testing and validation

#### Production
- **URL:** https://payment-app-production.apps.itz-anmwwn.hub01-lb.techzone.ibm.com
- **Deployment:** Manual approval required
- **Purpose:** Live production environment

### Triggering Deployments

```bash
# Automatic deployment to staging
git push origin main

# Production deployment requires manual approval in GitHub Actions UI
```

## 🐳 Container Image

### Building Locally

```bash
# Build Docker image
docker build -t payment-app:latest .

# Run container
docker run -p 8080:8080 payment-app:latest
```

### OpenShift Build

The CI/CD pipeline uses OpenShift's built-in build system:
- **BuildConfig:** Binary source build
- **Strategy:** Docker
- **Registry:** OpenShift internal registry

## ☸️ Kubernetes Deployment

### Manifests Structure

```
openshift/
├── base/                        # Base Kubernetes resources
│   ├── deployment.yaml         # Application deployment
│   ├── service.yaml            # Service definition
│   ├── route.yaml              # OpenShift route
│   ├── hpa.yaml                # Horizontal Pod Autoscaler
│   ├── pdb.yaml                # Pod Disruption Budget
│   ├── serviceaccount.yaml     # Service account
│   └── configmap.yaml          # Configuration
└── overlays/                    # Environment-specific configs
    ├── staging/                # Staging environment
    └── production/             # Production environment
```

### Resource Configuration

#### Staging
- **Replicas:** 3-10 (auto-scaling)
- **CPU:** 250m request, 1000m limit
- **Memory:** 512Mi request, 1Gi limit

#### Production
- **Replicas:** 5-20 (auto-scaling)
- **CPU:** 500m request, 2000m limit
- **Memory:** 1Gi request, 2Gi limit

## 🔍 Monitoring & Health Checks

### Health Endpoints

```bash
# Overall health
curl https://payment-app-production.apps.itz-anmwwn.hub01-lb.techzone.ibm.com/actuator/health

# Readiness probe
curl https://payment-app-production.apps.itz-anmwwn.hub01-lb.techzone.ibm.com/actuator/health/readiness

# Liveness probe
curl https://payment-app-production.apps.itz-anmwwn.hub01-lb.techzone.ibm.com/actuator/health/liveness

# Metrics
curl https://payment-app-production.apps.itz-anmwwn.hub01-lb.techzone.ibm.com/actuator/metrics
```

### Logging

```bash
# View application logs
oc logs -f deployment/payment-app-production -n payment-app-production

# View logs from specific pod
oc logs <pod-name> -n payment-app-production
```

## 🔒 Security

### Security Features
- ✅ Non-root container user (UID 1001)
- ✅ Read-only root filesystem
- ✅ Dropped all capabilities
- ✅ OWASP dependency scanning
- ✅ TLS termination at route level
- ✅ Secrets management via OpenShift Secrets

### Security Scanning

The CI/CD pipeline includes automated security scanning:
- **OWASP Dependency Check** - Identifies vulnerable dependencies
- **Trivy** - Container image vulnerability scanning
- **Fail threshold:** CVSS >= 7

## 🛠️ Development

### API Endpoints

```
GET  /                          # Home page
GET  /api/payments              # List payments
POST /api/payments              # Create payment
GET  /api/payments/{id}         # Get payment details
GET  /actuator/health           # Health check
GET  /actuator/metrics          # Application metrics
```

### Configuration

Environment-specific configuration is managed through:
- **ConfigMaps:** Non-sensitive configuration
- **Secrets:** Sensitive data (passwords, API keys)
- **Environment Variables:** Runtime configuration

### Local Development Setup

```bash
# Set environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/payment_db
export REDIS_HOST=localhost
export SPRING_PROFILES_ACTIVE=dev

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📚 Documentation

Comprehensive documentation is available in the `docs/` directory:

- **[Deployment Guide](../docs/DEPLOYMENT.md)** - Complete deployment procedures
- **[GitHub Actions Setup](../docs/GITHUB_ACTIONS_SETUP.md)** - CI/CD configuration
- **[Troubleshooting Guide](../docs/troubleshooting-guide.md)** - Common issues and solutions
- **[Helper Scripts](../docs/scripts/)** - Manual deployment scripts

## 🤝 Contributing

### Development Workflow

1. Create a feature branch from `develop`
2. Make your changes
3. Run tests: `mvn test`
4. Commit with descriptive message
5. Push and create pull request
6. Wait for CI/CD checks to pass
7. Request review

### Code Quality

```bash
# Run code formatting
mvn spring-javaformat:apply

# Run static analysis
mvn checkstyle:check

# Generate test coverage
mvn jacoco:report
```

## 🐛 Troubleshooting

### Common Issues

**Build Failures:**
```bash
# Clean Maven cache
mvn clean install -U

# Skip tests temporarily
mvn package -DskipTests
```

**Deployment Issues:**
```bash
# Check pod status
oc get pods -n payment-app-production

# View pod logs
oc logs <pod-name> -n payment-app-production

# Describe pod for events
oc describe pod <pod-name> -n payment-app-production
```

**Image Pull Issues:**
```bash
# Verify image exists
oc get imagestream payment-app -n payment-app-production

# Check build status
oc get builds -n payment-app-production
```

For detailed troubleshooting, see [Troubleshooting Guide](../docs/troubleshooting-guide.md).

## 📞 Support

- **Issues:** Create a GitHub issue
- **Documentation:** See `docs/` directory
- **DevOps Team:** devops@example.com
- **On-Call:** oncall@example.com

## 📄 License

[Add your license information here]

---

**Version:** 1.0.0  
**Last Updated:** 2026-02-26  
**Maintained By:** DevOps Team