# Piixl

Piixl is a high-performance, distributed system blueprint built with **Java 21**, **Spring Boot 3.4**, and **Spring Cloud**. 

Designed as a technical demonstration rather than a consumer product, this repository implements complex backend patterns including **JWT security propagation**, **service discovery**, and **event-driven communication** in a containerized environment.

---

## Technologies Used
The project leverages a modern tech stack focused on high performance and developer productivity:

* **Backend:** Java 21 (LTS), Spring Boot 3.4+, Spring Cloud (Gateway & Eureka)
* **Security:** Spring Security, JWT (JSON Web Tokens), BCrypt encryption
* **Messaging:** RabbitMQ (Spring AMQP) for asynchronous event processing
* **Databases:** * **PostgreSQL:** Relational storage for user identity and authentication
    * **MongoDB:** Document storage for media and post metadata
* **Frontend:** Angular (TypeScript)
* **Containerization:** Docker & Docker Compose

---

## System Architecture
The system is built on a **Microservices Architecture** utilizing the following patterns:

* **Service Discovery (Netflix Eureka):** All services register with a central server, allowing for dynamic scaling and location transparency.
* **API Gateway (Spring Cloud Gateway):** A single entry point for the frontend that handles cross-cutting concerns like CORS, global error handling, and JWT validation.
* **Database per Service:** Auth data is strictly decoupled from application data, ensuring independent scalability and schema management.
* **Token Propagation:** Authenticated user context is passed seamlessly from the gateway to downstream services via HTTP headers.

---

## Development Workflow

### Prerequisites
* **Java JDK 21**
* **npm / Node 18+** 
* **Docker & Docker Compose** (for MongoDB and optionally PostgreSQL if you do not have local DBs)

### Local Setup & Execution

1.  Clone the Repo and Navigate to its Folder
2. Start the Required Databases
* **MongoDB for media-service**
``` bash
cd media-service
docker compose up -d
```
This starts MongoDB on localhost:8094

* **MongoDB for profile-service**
``` bash
cd profile-service
docker compose up -d
```
This starts MongoDB on localhost:8095

* **PostgreSQL for auth-service**
The app expects:
    * host: localhost
    * port: 5433
    * database: auth_db
    * user: postgres
    * password: root
If you do not already have PostgreSQL, use:
``` bash
docker run -d --name piixl-postgres -p 5433:5432 \
    -e POSTGRES_DB=auth_db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=root \
postgres:15
```

3. **Start backend services in order**
Open your piixl folder in separate terminals or use a multiplexer.
    * **Discovery Server**
    Run in a new terminal

    ```bash
    cd discovery-server
    ./mvnw spring-boot:run
    ```
    * **Auth Service**
    Run in a new terminal

    ```bash
    cd auth-service
    ./mvnw spring-boot:run
    ```
    * **Media Service**
    Run in a new terminal

    ```bash
    cd media-service
    ./mvnw spring-boot:run
    ```
    * **Profile Service**
    Run in a new terminal

    ```bash
    cd profile-service
    ./mvnw spring-boot:run
    ```
    * **API Gateway**
    Run in a new terminal

    ```bash
    cd api-gateway
    ./mvnw spring-boot:run
    ```


### Running Services Individually
If you need to debug a specific service without Docker:
* **Step 1:** Start the `discovery-server` (Required for service mesh visibility).
* **Step 2:** Boot the `api-gateway`.
* **Step 3:** Run the target service:
    ```bash
    ./mvnw spring-boot:run -pl :service-name
    ```

---

## Environment & Access

| Service | Access URL | Port |
| :--- | :--- | :--- |
| **Frontend UI** | [http://localhost:4200](http://localhost:4200) | 4200 |
| **API Gateway** | [http://localhost:8080](http://localhost:8080) | 8080 |
| **Eureka Dashboard** | [http://localhost:8761](http://localhost:8761) | 8761 |

### Key Configuration Variables
The system uses the following environment variables (defined in `docker-compose.yml`):
* `POSTGRES_DB/USER/PASSWORD`: Database identity for the Auth service.
* `JWT_SECRET`: Secret key for signing/verifying session tokens.
* `EUREKA_CLIENT_SERVICE_URL`: Discovery registry path (Default: `http://discovery:8761`).

---

> **Note:** Upon launch, please allow 30-60 seconds for the "heartbeat" registration to complete in Eureka before attempting to log in via the Frontend.
