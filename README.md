# Smart Queue API

A backend REST API for managing customer queues in a service center, built with Java and Spring Boot.

Smart Queue provides a simple and reliable way to register customers, manage waiting lists, call the next customer, and track queue positions. It also handles concurrent requests to ensure that the same customer cannot be called more than once.

## Overview

In a traditional service center, managing customer queues manually can lead to confusion, delays, and duplicate calls.

**Smart Queue API** addresses this by providing a centralized queue management system with the following capabilities:

- Register new customers in the queue.
- Retrieve all customers currently waiting.
- View customer details and their current queue position.
- Call the next waiting customer.
- Remove customers from the waiting list.
- Prevent duplicate customer calls during concurrent requests.

The project focuses on clean backend development, database interaction, RESTful API design, and concurrency handling.

## Technologies

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 3.5 | Backend framework |
| Spring Web | REST API development |
| Spring Data JPA | Database access and ORM |
| PostgreSQL | Relational database |
| Gradle | Build automation and dependency management |
| Jakarta Validation | Request validation |
| JUnit 5 | Unit and integration testing |
| Mockito | Mocking dependencies during unit tests |
| Docker Compose | Local PostgreSQL environment |

## Architecture

The application follows a package-by-feature structure to keep related components organized and maintainable.

```text
src/
├── main/
│   ├── java/az/nvprojectlab/smartqueue/
│   │   ├── customer/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   └── enums/
│   │   │
│   │   ├── common/
│   │   │   ├── exception/
│   │   │   └── response/
│   │   │
│   │   └── SmartQueueApplication.java
│   │
│   └── resources/
│       └── application.yml
│
└── test/
    ├── java/
    │   └── az/nvprojectlab/smartqueue/
    │       └── customer/
    │           └── service/
    └── resources/
```

### Main Components

- **Controller:** Exposes REST endpoints and handles incoming HTTP requests.
- **Service:** Contains queue management and business logic.
- **Repository:** Handles database operations using Spring Data JPA.
- **Entity:** Represents customer records in the database.
- **DTO:** Defines request and response structures.
- **Exception Handling:** Provides centralized exception handling and consistent error responses.

## Customer Model

Each customer contains the following information:

| Field | Type | Description |
|---|---|---|
| `id` | Long | Unique customer identifier |
| `name` | String | Customer name |
| `createdAt` | LocalDateTime | Registration date and time |
| `status` | QueueStatus | Current queue status |

### Queue Statuses

- `WAITING` — Customer is waiting to be served.
- `SERVING` — Customer has been called and is being served.
- `COMPLETED` — Service has been completed.

Customers in the waiting list are ordered by registration time, with their IDs used as a secondary sorting criterion.

## API Endpoints

Base URL:

```text
http://localhost:8080/api/queue
```

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/queue` | Register a new customer |
| GET | `/api/queue` | Retrieve all waiting customers |
| GET | `/api/queue/{id}` | Retrieve customer details |
| POST | `/api/queue/next` | Call the next waiting customer |
| DELETE | `/api/queue/{id}` | Remove a waiting customer |

### 1. Register a Customer

**POST** `/api/queue`

Request body:

```json
{
  "name": "John Doe"
}
```

Creates a new customer with the default `WAITING` status.

### 2. Get Waiting Customers

**GET** `/api/queue`

Returns all customers currently waiting in the queue, ordered by registration time.

Example response:

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "createdAt": "2026-11-23T10:30:00",
    "status": "WAITING",
    "position": 1
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "createdAt": "2026-11-23T10:35:00",
    "status": "WAITING",
    "position": 2
  }
]
```

### 3. Get Customer by ID

**GET** `/api/queue/{id}`

Returns the details of a specific customer.

If the customer is currently waiting, the response also includes their position in the queue.

### 4. Call Next Customer

**POST** `/api/queue/next`

Retrieves the first waiting customer and changes their status to `SERVING`.

If there are no waiting customers, the API returns an appropriate error response.

### 5. Remove a Customer

**DELETE** `/api/queue/{id}`

Removes a customer from the queue.

Only customers with the `WAITING` status can be removed. Customers who are already being served cannot be deleted through this endpoint.

## Concurrency Handling

One of the important challenges in queue management is handling multiple requests to the `/next` endpoint at the same time.

Without proper synchronization, two concurrent requests could retrieve the same waiting customer before their status is updated.

To prevent this, the application uses **Pessimistic Write Locking** provided by Spring Data JPA and PostgreSQL.

The implementation consists of two main parts:

1. **Database Locking:** The repository applies `PESSIMISTIC_WRITE` locking when selecting the first waiting customer. This translates to a database-level row lock.
2. **Transactional Processing:** The `callNextCustomer()` service method uses `@Transactional`, keeping the lock active until the customer's status is updated and the transaction is committed.

When multiple requests arrive simultaneously:

- The first request acquires the database lock.
- Other requests attempting to select the same waiting customer must wait.
- The first request updates the customer's status to `SERVING` and commits the transaction.
- The next request can then retrieve the next available `WAITING` customer.

This approach ensures that the same customer is not returned by multiple concurrent `/next` requests.

## Testing

The project includes unit and integration tests to verify the queue management logic and concurrency behavior.

**Unit Tests — JUnit 5 & Mockito**

- Customer registration.
- Calling the next customer.
- Handling an empty queue.
- Removing waiting customers.
- Preventing removal of customers who are already being served.
- Handling customers that do not exist.

**Concurrency Integration Test**

The concurrency test uses a real PostgreSQL database and executes two `/next` operations simultaneously.

It verifies that both requests receive different customer IDs and that both customers are correctly updated to the `SERVING` status.

## Getting Started

### Prerequisites

Make sure you have the following installed:

- Java 21
- Docker Desktop
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/smart-queue.git
cd smart-queue
```

### 2. Start PostgreSQL

The project includes a Docker Compose configuration for running PostgreSQL locally.

```bash
docker compose up -d
```

The database will be available at:

```text
localhost:5432
```

### 3. Run the Application

On Windows:

```bash
gradlew.bat bootRun
```

On Linux/macOS:

```bash
./gradlew bootRun
```

The API will start at:

```text
http://localhost:8080
```

### 4. Run Tests

On Windows:

```bash
gradlew.bat test
```

On Linux/macOS:

```bash
./gradlew test
```

To run a clean build:

```bash
gradlew.bat clean test
```

## Configuration

Database connection settings are defined in `application.yml`.

The default local configuration uses:

| Property | Value |
|---|---|
| Database | `smart_queue` |
| Host | `localhost` |
| Port | `5432` |
| Username | `queue` |
| Password | `queue12345` |

For a real deployment, database credentials should be supplied through environment variables or a secure configuration mechanism rather than committed to source control.

## Error Handling

The API uses centralized exception handling to return consistent HTTP error responses.

Common HTTP status codes include:

| Status Code | Description |
|---|---|
| 201 Created | Customer successfully registered |
| 204 No Content | Customer successfully removed |
| 400 Bad Request | Invalid request data |
| 404 Not Found | Customer does not exist |
| 409 Conflict | Requested operation cannot be performed |

## Future Improvements

Potential enhancements for future versions include:

- Completing customer service and updating the status to `COMPLETED`.
- Adding queue statistics and reporting.
- Introducing pagination for larger queues.
- Adding API documentation with Swagger / OpenAPI.
- Supporting multiple service counters.

## Author

**Sabir Huseynov**

Backend development project created as part of the **NV ProjectLab Backend Interview Task**.

---

*Built with Java, Spring Boot, and PostgreSQL.*