# CritiQ-Backend

A modern, feature-rich backend application built with Spring Boot 3.5.4, providing comprehensive services for a social platform with e-commerce capabilities.

## 🚀 Features

### Core Features
- **User Management**: Registration, login, email verification, and profile management
- **Blog System**: Create, read, update, and delete blog posts with comments
- **Social Features**: Follow/unfollow users, follower relationships
- **E-commerce Integration**: Voucher management and flash sale (seckill) functionality
- **Shop Management**: Multi-category shop and product management

### Technical Highlights
- **JWT-based Authentication**: Secure token-based authorization
- **Distributed Locking**: Redis-based distributed locks for high-concurrency scenarios
- **Caching Strategy**: Redis caching with penetration and breakdown protection
- **Message Queue**: RabbitMQ for asynchronous order processing
- **Email Service**: Automated email verification and notifications
- **API Documentation**: OpenAPI/Swagger integration for comprehensive API docs

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Runtime Environment |
| **Spring Boot** | 3.5.4 | Application Framework |
| **MyBatis Plus** | 3.5.12 | Database ORM |
| **MySQL** | - | Primary Database |
| **Redis** | - | Caching & Session Store |
| **Redisson** | 3.50.0 | Distributed Locking |
| **RabbitMQ** | - | Message Queue |
| **Spring Security** | - | Authentication & Authorization |
| **Hutool** | 5.8.39 | Utility Library |
| **SpringDoc OpenAPI** | 2.8.9 | API Documentation |
| **AWS S3** | 3.4.0 | Object Storage |

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **RabbitMQ 3.8+**

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/KarigenBrown/CritiQ-Backend.git
cd CritiQ-Backend
```

### 2. Database Configuration
Create a MySQL database and configure the connection in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/critiq_db
    username: your_username
    password: your_password
```

### 3. Redis Configuration
Configure Redis in `application.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 4. RabbitMQ Configuration
Configure RabbitMQ in `application.yml`:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 5. Build and Run
```bash
# Using Maven Wrapper
./mvnw clean install
./mvnw spring-boot:run

# Or using Maven
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, you can access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🏗️ Project Structure

```
src/main/java/me/critiq/backend/
├── CritiQBackendApplication.java    # Main application class
├── config/                          # Configuration classes
│   ├── AsyncConfig.java             # Async configuration
│   ├── MybatisPlusConfig.java       # MyBatis Plus configuration
│   └── SecurityConfig.java          # Security configuration
├── controller/                      # REST API controllers
├── domain/                          # Domain models
│   ├── entity/                      # JPA entities
│   ├── dto/                         # Data Transfer Objects
│   └── vo/                          # Value Objects
├── service/                         # Business logic services
├── mapper/                          # MyBatis mappers
├── mq/                              # Message queue components
│   ├── consumer/                    # RabbitMQ consumers
│   └── producer/                    # RabbitMQ producers
├── util/                            # Utility classes
│   └── lock/                        # Custom lock implementations
└── exception/                       # Exception handling
```

## 🔧 Key Implementation Details

### Authentication & Security
- JWT-based stateless authentication
- OAuth2 Resource Server integration
- Redis-based session management
- Password encryption with BCrypt
- Google OIDC integration

### Caching Strategy
- Multi-level caching with Redis and Caffeine
- Cache penetration and breakdown protection
- Logical expiration pattern for performance optimization
- Custom cache client with comprehensive utilities

### Distributed Systems
- Redisson-based distributed locks for concurrency control
- RabbitMQ for asynchronous order processing
- Circuit breaker pattern with Resilience4j

### Performance Optimizations
- Connection pooling for database and Redis
- Asynchronous processing for email and order handling
- Efficient pagination and scrolling mechanisms

## 🧪 Testing

Run the test suite:
```bash
./mvnw test
```

## 📝 Development Guidelines

### Code Style
- Follow Java conventions and Spring Boot best practices
- Use Lombok for reducing boilerplate code
- Implement proper exception handling with `@RestControllerAdvice`

### Database
- Use MyBatis Plus for enhanced ORM capabilities
- Follow naming conventions for entities and tables
- Implement proper indexing for performance

### Security
- Validate all input parameters using `@Valid`
- Implement proper authorization checks
- Use prepared statements to prevent SQL injection

## 🚀 Deployment

### Docker Deployment
```bash
# Build the application
./mvnw clean package

# Run with Docker (Dockerfile required)
docker build -t critiq-backend .
docker run -p 8080:8080 critiq-backend
```

### Environment Variables
Configure the following environment variables for production:
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `REDIS_URL`
- `RABBITMQ_URL`
- `JWT_SECRET`
