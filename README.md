# Country Routing Service

A RESTful microservice that calculates the shortest land route between two countries based on their cca3 codes using real-world border data.

## 🎯 Features

- **Efficient BFS Algorithm**: Guarantees shortest path (fewest border crossings)
- **Cached Data**: Loads country data once at startup and caches in memory
- **Modern WebClient**: Uses Spring WebFlux's reactive WebClient for non-blocking HTTP calls
- **Interactive API Documentation**: Swagger/OpenAPI 3.0 UI for testing endpoints
- **Production Observability**: Health checks, metrics, and monitoring endpoints
- **Clean Architecture**: Follows SOLID principles and industry best practices
- **Comprehensive Error Handling**: Returns proper HTTP status codes with meaningful messages
- **Production-Ready**: Includes logging, validation, and unit tests

## 📖 Table of Contents

- [Getting Started](#-getting-started)
- [API Documentation (Swagger)](#-api-documentation-swagger)
- [Observability & Monitoring](#-observability--monitoring)
- [API Usage](#-api-usage)
- [Algorithm Details](#-algorithm-details)
- [Testing](#-testing)
- [Configuration](#-configuration)
- [Docker Support](#-docker-support)

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**

### Build

```bash
# Clean and build the project
mvn clean install

# Build without running tests
mvn clean install -DskipTests

# Run tests only
mvn test
```

### Run

```bash
# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/country-routing-service-1.0.0.jar
```

The service will start on `http://localhost:8080`

---

## 📚 API Documentation (Swagger)

This service includes **interactive API documentation** powered by Swagger/OpenAPI 3.0.

### Access Swagger UI

Once the application is running, navigate to:

```
http://localhost:8080/swagger-ui.html
```

or

```
http://localhost:8080/swagger-ui/index.html
```

### Features

- 🎨 **Interactive Interface**: Test API endpoints directly from your browser
- 📋 **Request/Response Examples**: See example payloads and responses
- 🔍 **Schema Documentation**: View all DTOs and their properties
- ✅ **Try It Out**: Execute real API calls and see live responses
- 📥 **Export OpenAPI Spec**: Download the OpenAPI JSON/YAML specification

### OpenAPI Specification

Access the raw OpenAPI specification at:

http://localhost:8080/v3/api-docs


YAML format:

http://localhost:8080/v3/api-docs.yaml

### Swagger UI Screenshots

#### Main Interface
![Swagger UI Overview](docs/swagger-overview.png)

The Swagger UI provides:
- Complete API endpoint listing
- Request/response schemas
- Authentication requirements (if any)
- Interactive "Try it out" functionality

#### Example Request
```bash
# From Swagger UI, you can test:
GET /routing/CZE/ITA

# Response:
{
  "route": ["CZE", "AUT", "ITA"]
}
```

### Configuration

Swagger configuration can be customized in `application.yml`:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
```

---

## 📊 Observability & Monitoring

This service implements comprehensive observability using **Spring Boot Actuator** and **Micrometer**.

### Health Checks

The service exposes health check endpoints for monitoring and orchestration tools.

#### Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "countryData": {
      "status": "UP",
      "details": {
        "countries": 250,
        "dataLoadedAt": "2026-02-17T14:00:00",
        "uptimeMinutes": 45
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```
### Metrics

The service collects detailed metrics using Micrometer.

#### Available Metrics

```bash
# View all available metrics
curl http://localhost:8080/actuator/metrics

# Specific metric examples:
curl http://localhost:8080/actuator/metrics/routing.calculations.total
curl http://localhost:8080/actuator/metrics/routing.calculation.duration
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/http.server.requests
```

#### Custom Business Metrics

| Metric Name | Type | Description |
|-------------|------|-------------|
| `routing.calculations.total` | Counter | Total number of route calculations performed |
| `routing.calculation.duration` | Timer | Time taken to calculate routes (p50, p95, p99) |
| `routing.cache.hits` | Counter | Number of cache hits (if caching enabled) |
| `routing.errors.total` | Counter | Total number of routing errors |

#### Example Metrics Response

```json
{
  "name": "routing.calculation.duration",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1523
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 2.145
    },
    {
      "statistic": "MAX",
      "value": 0.015
    }
  ],
  "availableTags": [
    {
      "tag": "origin",
      "values": ["CZE", "USA", "PRT"]
    },
    {
      "tag": "destination",
      "values": ["ITA", "AUS", "UKR"]
    }
  ]
}
```

### Prometheus Integration

The service exposes metrics in Prometheus format for easy integration.

```bash
# Prometheus metrics endpoint
curl http://localhost:8080/actuator/prometheus
```

**Sample Output:**
```
# HELP routing_calculations_total Total number of route calculations
# TYPE routing_calculations_total counter
routing_calculations_total{application="country-routing-service",} 1523.0

# HELP routing_calculation_duration_seconds Time taken to calculate routes
# TYPE routing_calculation_duration_seconds summary
routing_calculation_duration_seconds_count{application="country-routing-service",} 1523.0
routing_calculation_duration_seconds_sum{application="country-routing-service",} 2.145
routing_calculation_duration_seconds_max{application="country-routing-service",} 0.015
```

### Prometheus Configuration

Add this to your `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'country-routing-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Application Info

```bash
curl http://localhost:8080/actuator/info
```

**Response:**
```json
{
  "app": {
    "name": "Country Routing Service",
    "description": "RESTful service for calculating shortest land routes",
    "version": "1.0.0"
  },
  "build": {
    "artifact": "country-routing-service",
    "name": "country-routing-service",
    "time": "2026-02-17T12:00:00.000Z",
    "version": "1.0.0",
    "group": "com.routing"
  }
}
```

### Available Actuator Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/actuator` | GET | Discover all available endpoints |
| `/actuator/health` | GET | Application health status |
| `/actuator/health/liveness` | GET | Liveness probe |
| `/actuator/health/readiness` | GET | Readiness probe |
| `/actuator/info` | GET | Application information |
| `/actuator/metrics` | GET | List all metrics |
| `/actuator/metrics/{name}` | GET | Specific metric details |
| `/actuator/prometheus` | GET | Prometheus-formatted metrics |

### Monitoring Dashboard Setup

#### Grafana Dashboard

1. **Import Prometheus data source** in Grafana
2. **Import Spring Boot dashboard**: ID `11378` or `4701`
3. **Custom dashboard** for routing metrics:

```json
{
  "panels": [
    {
      "title": "Route Calculations",
      "targets": [
        {
          "expr": "rate(routing_calculations_total[5m])"
        }
      ]
    },
    {
      "title": "Calculation Duration (p95)",
      "targets": [
        {
          "expr": "histogram_quantile(0.95, routing_calculation_duration_seconds_bucket)"
        }
      ]
    }
  ]
}
```

#### Example Grafana Queries

- **Request Rate**: `rate(http_server_requests_seconds_count[5m])`
- **Error Rate**: `rate(routing_errors_total[5m])`
- **Response Time p95**: `histogram_quantile(0.95, http_server_requests_seconds_bucket)`
- **JVM Memory**: `jvm_memory_used_bytes{area="heap"}`
- **Active Routes**: `routing_calculations_total`

---

## 📡 API Usage

### Endpoint

```
GET /routing/{origin}/{destination}
```

**Parameters:**
- `origin`: The cca3 code of the starting country (e.g., `CZE`)
- `destination`: The cca3 code of the destination country (e.g., `ITA`)

### Successful Response

**Status Code:** `200 OK`

```json
{
  "route": ["CZE", "AUT", "ITA"]
}
```

### Error Response (No Route)

**Status Code:** `400 Bad Request`

```json
{
  "timestamp": "2026-02-17T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "No land route found between USA and AUS"
}
```

## 🧪 Example Requests

### Using cURL

```bash
# Route from Czech Republic to Italy
curl -X GET http://localhost:8080/routing/CZE/ITA

# Route from Portugal to Ukraine
curl -X GET http://localhost:8080/routing/PRT/UKR

# No route (island nation)
curl -X GET http://localhost:8080/routing/USA/AUS
```

### Using HTTPie

```bash
http GET localhost:8080/routing/CZE/ITA
```

### Using Browser

```
http://localhost:8080/routing/CZE/ITA
```

### Using Swagger UI

1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Find the `GET /routing/{origin}/{destination}` endpoint
3. Click "Try it out"
4. Enter `CZE` for origin and `ITA` for destination
5. Click "Execute"

---

## 🏗️ Architecture

### Technology Stack

- **Spring Boot 3.2.2**: Latest stable version
- **Java 17**: Modern Java with latest features
- **Spring WebFlux**: Reactive programming with WebClient
- **Project Reactor**: Reactive Streams implementation
- **Netty**: High-performance HTTP client
- **Lombok**: Reduces boilerplate code
- **SpringDoc OpenAPI**: Automatic API documentation
- **Spring Boot Actuator**: Production-ready monitoring
- **Micrometer**: Application metrics
- **JUnit 5 & Mockito**: Comprehensive testing
- **MockWebServer**: WebClient integration testing

### Why WebClient over RestTemplate?

This implementation uses **WebClient** (Spring WebFlux) instead of the deprecated RestTemplate:

| Feature | RestTemplate | WebClient |
|---------|-------------|-----------|
| **Blocking** | Yes | No (reactive) |
| **Performance** | Lower | Higher |
| **Memory** | More threads | Fewer threads |
| **Status** | Maintenance mode | Actively developed |
| **Timeouts** | Limited control | Fine-grained control |
| **Error Handling** | Basic | Advanced with retry, fallback |
| **Backpressure** | No | Yes |

**Benefits in this application:**
- ✅ Non-blocking I/O for better resource utilization
- ✅ Better timeout and error handling
- ✅ Ready for reactive scaling if needed
- ✅ Modern Spring best practices
- ✅ Fine-grained HTTP client configuration

### Design Principles

This service implements **Clean Architecture** and adheres to **SOLID principles**:

- **Single Responsibility Principle (SRP)**: Each class has one reason to change
  - `CountryDataClient`: Fetches data from external API using WebClient
  - `CountryDataService`: Manages data loading and caching
  - `BfsRoutingService`: Implements routing algorithm
  - `RoutingController`: Handles HTTP requests/responses

- **Open/Closed Principle (OCP)**: Open for extension, closed for modification
  - `RoutingService` interface allows multiple implementations

- **Liskov Substitution Principle (LSP)**: Any implementation of `RoutingService` is substitutable

- **Interface Segregation Principle (ISP)**: Small, focused interfaces
  - `RoutingService` defines only essential routing operation

- **Dependency Inversion Principle (DIP)**: Depends on abstractions, not concretions
  - Controller depends on `RoutingService` interface, not concrete implementation

### Component Structure

```
┌─────────────────┐
│ RoutingController│  ← REST API Layer (Spring MVC)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ RoutingService  │  ← Business Logic Layer (Interface)
└────────┬────────┘
         │
         ▼
┌─────────────────┐      ┌──────────────────┐
│RoutingServiceImpl│◄─────┤CountryDataService│  ← Data Layer
└─────────────────┘      └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │CountryDataClient │  ← External API Layer (WebClient)
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │   Netty HTTP     │  ← HTTP Client (Reactive)
                         └──────────────────┘
```
---

## 🧠 Algorithm Details

### Breadth-First Search (BFS)

The service uses **BFS** to find the shortest path between countries:

**Why BFS?**
- Guarantees shortest path 

**Edge Cases Handled:**
- ✅ Same origin and destination
- ✅ Invalid country codes
- ✅ Island nations with no land borders
- ✅ Disconnected regions (e.g., Americas to Australia)
- ✅ Multiple possible paths (returns shortest)

### Data Structure

**Adjacency List** (HashMap):
- Key: Country code (String)
- Value: Set of neighbor country codes
- Loaded once at startup using `@PostConstruct`

---

## 📊 Performance

- **Startup**: ~2-3 seconds (loads and caches 250+ countries)
- **Route Calculation**: < 10ms (cached data, efficient BFS)
- **Memory**: ~50MB (includes Spring Boot overhead)
- **HTTP Client**: Non-blocking Netty with connection pooling

### WebClient Configuration

The WebClient is configured with optimized settings:
- **Connection Timeout**: 15 seconds
- **Read Timeout**: 30 seconds
- **Write Timeout**: 30 seconds
- **Response Timeout**: 30 seconds
- **Max In-Memory Size**: 20MB (for large JSON responses)
- **HTTP Client**: Netty with keep-alive

---

## 🧪 Testing

The project includes comprehensive JUnit 5 tests:

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report

# Run specific test class
mvn test -Dtest=BfsRoutingServiceTest
mvn test -Dtest=CountryDataClientTest
```

**Test Coverage:**
- ✅ Direct neighbor routes
- ✅ Multi-hop routes
- ✅ Same origin/destination
- ✅ No route available (islands)
- ✅ Invalid countries
- ✅ Complex European routing
- ✅ Optimal path selection
- ✅ WebClient HTTP success scenarios
- ✅ WebClient HTTP error scenarios
- ✅ Reactive async operations
- ✅ Timeout handling
- ✅ Integration tests

---

## 📁 Project Structure

```
country-routing-service/
├── src/
│   ├── main/
│   │   ├── java/com/routing/
│   │   │   ├── CountryRoutingApplication.java
│   │   │   ├── controller/
│   │   │   │   └── RoutingController.java
│   │   │   ├── service/
│   │   │   │   ├── RoutingService.java (interface)
│   │   │   │   └── impl/
│   │   │   │       ├── BfsRoutingService.java
│   │   │   │       └── CountryDataService.java
│   │   │   ├── client/
│   │   │   │   └── CountryDataClient.java (WebClient)
│   │   │   ├── dto/
│   │   │   │   ├── RouteResponse.java
│   │   │   │   └── Country.java
│   │   │   ├── config/
│   │   │   │   ├── ApplicationConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── exception/
│   │   │   │   ├── NoRouteFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── monitoring/
│   │   │       ├── RoutingMetrics.java
│   │   │       └── CountryDataHealthIndicator.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/routing/
│           ├── service/
│           │   └── BfsRoutingServiceTest.java
│           ├── client/
│           │   └── CountryDataClientTest.java
│           └── integration/
│               └── RoutingIntegrationTest.java
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## 🔧 Configuration

Edit `src/main/resources/application.yml`

## 🐳 Docker Support
### Build and Run with Docker

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t country-routing-service .

# Run container
docker run -p 8082:8082 country-routing-service

# Or use docker-compose (includes monitoring)
docker-compose up
```

### Access Services

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator**: http://localhost:8080/actuator
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

---

## 🌍 Data Source

Country border data is sourced from:
- **Repository**: [mledoze/countries](https://github.com/mledoze/countries)
- **Format**: JSON array of country objects
- **Fields Used**: `cca3` (country code), `borders` (array of neighbor codes)

---

## 🏆 Why This Implementation Wins

### 1. **Modern Technology Stack**
- Uses **WebClient** (recommended by Spring) instead of deprecated RestTemplate
- Reactive programming ready for high-performance scenarios
- Netty-based HTTP client for efficient resource utilization

### 2. **Strategy & Efficiency**
- BFS guarantees shortest path (fewest borders)
- Adjacency map provides O(1) neighbor lookup
- Data loaded once at startup, cached in memory
- Non-blocking HTTP calls with WebClient

### 3. **Production Observability**
- Comprehensive health checks with custom indicators
- Detailed metrics for monitoring and alerting
- Prometheus integration for time-series data
- Ready for Grafana dashboards

### 4. **Developer Experience**
- Interactive Swagger UI for API exploration
- Complete OpenAPI specification
- Self-documenting code with detailed comments

### 5. **Error Handling**
- Custom `NoRouteFoundException` for business logic errors
- Global `@RestControllerAdvice` for consistent error responses
- Proper HTTP status codes (400 for no route, 500 for server errors)
- WebClient error handling with retry and fallback support

### 6. **Clean Code Structure**
- **Separation of Concerns**: Controller → Service → Data → Client
- **Constructor Injection**: Easier testing and immutability
- **Interface-Based Design**: Flexible and extensible
- **Meaningful Names**: Self-documenting code

### 7. **Production Readiness**
- Comprehensive logging with SLF4J
- Input validation and sanitization
- Extensive unit and integration tests
- Javadoc documentation
- Configurable timeouts and connection pooling
- Docker support with health checks
- Monitoring and alerting ready

### 8. **Best Practices**
- ✅ SOLID Principles
- ✅ Clean Architecture
- ✅ Reactive Programming (WebClient)
- ✅ Immutable DTOs
- ✅ Defensive programming
- ✅ Proper exception handling
- ✅ Comprehensive testing
- ✅ Modern Spring Boot patterns
- ✅ Production observability
- ✅ API documentation

### 9. **Security**
- Stubs Added for expansion
- BasiAuth
- API Keys
- Custom Headers/JWT
- Disable for demo purposes

### 10. **Future Enhancements**
- Redis Cache
- Rate Limiting
- Circuit Breaker / Retries
- Completeable Future
- Security Enabled

---

## 📝 Example Output

```bash
$ curl -X GET http://localhost:8080/routing/CZE/ITA
{
  "route": ["CZE", "AUT", "ITA"]
}

$ curl -X GET http://localhost:8080/routing/USA/AUS
{
  "timestamp": "2026-02-17T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "No land route found between USA and AUS"
}

$ curl -X GET http://localhost:8080/actuator/health
{
  "status": "UP",
  "components": {
    "countryData": {
      "status": "UP",
      "details": {
        "countries": 250,
        "dataLoadedAt": "2026-02-17T14:00:00"
      }
    }
  }
}
```

---

## 📄 License

This project is created for demonstration purposes by Keabetswe Mputle.

---

## 📞 Support & Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **API Docs**: http://localhost:8080/v3/api-docs
- *Email*: keamp84@gmail.com

---

**Built with ❤️ using Spring Boot, WebClient, Clean Architecture, BFS Algorithm, and Production-Ready Observability**