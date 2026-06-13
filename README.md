# Mendel Transaction Ledger

This is a RESTful web service for storing and retrieving transactions in memory, built with Spring Boot (Java 21). It was developed using **Test-Driven Development (TDD)** and incremental commits, following **SOLID** principles and clean architecture.

## Requirements Covered
- RESTful web service (in-memory storage)
- `PUT /transactions/{transaction_id}`
- `GET /transactions/types/{type}`
- `GET /transactions/sum/{transaction_id}`
- **Integration Tests** (11 tests covering all edges cases including cyclic loops, unknown parents, conflicts, and multi-child BFS sum).
- **Dockerized** application.
- Java 21.

## Architecture & Design Decisions
- **Thread-Safety & Concurrency**: The in-memory data store (`TransactionRepository`) uses `ConcurrentHashMap` and `CopyOnWriteArrayList` to handle concurrent HTTP requests safely without blocking threads.
- **Immutability**: Transactions are immutable once created. Attempting to overwrite an existing transaction ID returns a `409 Conflict`.
- **Cyclic Reference Prevention**: The `GET /sum` endpoint is implemented using an iterative Breadth-First Search (BFS) combined with a `visited` Set to completely avoid `StackOverflowError` in extremely deep trees and prevent infinite loops from corrupted cyclic data.
- **Fast Lookups**: Parent-Child relationships and Type groupings are indexed at insertion time to allow O(1) retrieval of lists for the `GET` endpoints.
- **Parent Validation**: When creating a transaction with a `parent_id`, the system strictly validates that the parent exists, returning `400 Bad Request` if it doesn't. To create a root transaction, the `parent_id` field must be completely omitted from the JSON body.

## API Documentation
Once the application is running, you can access the interactive Swagger/OpenAPI documentation at:
- 👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

## How to Run

### Using Docker (Recommended)
1. Build the Docker image:
   ```bash
   docker build -t transaction-ledger .
   ```
2. Run the container:
   ```bash
   docker run -p 8080:8080 transaction-ledger
   ```

### Using Maven Local
If you have Maven installed, you can simply run:
```bash
mvn spring-boot:run
```

## Running the Tests
To execute the comprehensive suite of Integration Tests:
```bash
mvn test
```