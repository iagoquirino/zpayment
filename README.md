# Java Seed Project

This is a seed project demonstrating best practices for building microservices with Spring Boot, Kafka Streams, and clean architecture.

## Tech Stack
- **Java 25**
- **Spring Boot 4.0.5**
- **Spring Cloud Stream** (Kafka Streams)
- **Avro** (Confluent Schema Registry)
- **OpenAPI** (Contract-First approach)
- **Flyway** (Database migration)
- **PostgreSQL**
- **Testcontainers** (Integration testing)

## Architecture
1. **User Endpoint**: Publishes `UserEvent` to `user_topic`.
2. **Order Endpoint**: Publishes `OrderEvent` to `order_topic`.
3. **Topology**: Joins `UserEvent` (KTable) and `OrderEvent` (KStream) to produce `UserOrderEvent`.
4. **Listener**: Persists `UserOrderEvent` to the `user_order` table.
5. **API**: Exposes user order information.

## Getting Started

### Prerequisites
- Docker & Docker Compose
- JDK 25
- Maven

### Running Locally
1. Start infrastructure:
   ```bash
   docker-compose up -d
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Testing
- Run all tests:
  ```bash
  ./mvnw test
  ```
- Run integration tests:
  ```bash
  ./mvnw verify
  ```

### API Contract
The API is defined using OpenAPI in `src/main/resources/api/v1.yaml`. DTOs and Controller interfaces are generated during the Maven compile phase.

### Avro Schemas
Avro schemas are defined in `src/main/resources/avro/`. They are compiled into Java classes during the Maven generate-sources phase.

### Manual Testing
A Postman collection is available in `.postman/java-seed.postman_collection.json`.

1. Import the collection into Postman.
2. Call `POST /v1/users` to create a user.
3. Call `POST /v1/orders` with the returned `userId` to create an order.
4. Call `GET /v1/user-orders` to see the joined result persisted in the database.
