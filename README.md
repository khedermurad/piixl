# Piixl


Piixl is a high-performance, distributed system blueprint built with **Java 21**, **Spring Boot 3.4**, and **Spring Cloud**. 

Designed as a technical demonstration rather than a consumer product, this repository implements complex backend patterns including **JWT security propagation**, **service discovery**, and **event-driven communication** in a containerized environment.

## Purpose

Piixl exists to demonstrate how to design and implement a production-style
microservices architecture using Spring Boot and Spring Cloud.

It is intended for:
- Learning distributed systems patterns
- Experimenting with service-to-service communication
- Showcasing backend engineering skills

## Technologies Used
The project leverages a modern tech stack focused on high performance and developer productivity:

* **Backend:** Java 21 (LTS), Spring Boot 3.4+, Spring Cloud (Gateway & Eureka)
* **Security:** Spring Security, JWT (JSON Web Tokens), BCrypt encryption
* **Messaging:** RabbitMQ (Spring AMQP) for asynchronous event processing
* **Databases:** 
    * **PostgreSQL:** Relational storage for user identity and authentication
    * **MongoDB:** Document storage for media and post metadata
* **Frontend:** Angular (TypeScript)
* **Containerization:** Docker & Docker Compose


## System Architecture
The system is built on a **Microservices Architecture** utilizing the following patterns:

* **Service Discovery (Netflix Eureka):** All services register with a central server, allowing for dynamic scaling and location transparency.
* **API Gateway (Spring Cloud Gateway):** A single entry point for the frontend that handles cross-cutting concerns like CORS, global error handling, and JWT validation.
* **Database per Service:** Auth data is strictly decoupled from application data, ensuring independent scalability and schema management.
* **Token Propagation:** Authenticated user context is passed seamlessly from the gateway to downstream services via HTTP headers.



## Development Workflow and Local Installation

### Prerequisites
* **Java JDK 21**
* **npm / Node 18+** 
* **Docker & Docker Compose** (for MongoDB and optionally PostgreSQL if you do not have local DBs)

### Local Setup & Execution

---
1. **Clone the Repo**
``` bash
git clone https://github.com/khedermurad/piixl
```
---
2. **Start the Required Databases**

* **PostgreSQL for auth-service**
    * Open a new terminal and run this (Terminal 1)
``` bash
docker run -d --name piixl-postgres -p 5433:5432 \
    -e POSTGRES_DB=auth_db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=root \
postgres:15
```

* **MongoDB for media-service**
    *  Open a new terminal and run this (Terminal 2)
``` bash
docker run -d --name piixl-mongodb -p 8094:27017 \
    -e MONGO_INITDB_ROOT_USERNAME=root \
    -e MONGO_INITDB_ROOT_PASSWORD=root \
mongo
```
This starts MongoDB on localhost:8094

* **MongoDB for profile-service**
    * Open a new terminal and run this (Terminal 3)
``` bash
docker run -d --name profile-mongo -p 8095:27017 \
    -e MONGO_INITDB_ROOT_USERNAME=root \
    -e MONGO_INITDB_ROOT_PASSWORD=root \
mongo
```
This starts MongoDB on localhost:8095

* **RabbitMQ**
    * Open a new terminal and run this (Terminal 4)
``` bash
docker run -d --name piixl-rabbitmq -p 5672:5672 -p 15672:15672 \
    -e RABBITMQ_DEFAULT_USER=guest \
    -e RABBITMQ_DEFAULT_PASS=guest \
rabbitmq:3-management
```
This starts MongoDB on localhost:8095

---
3. **Verify Databases**
Open a new terminal and run this (Terminal 5)
``` bash
docker ps -a
```
You should see: 
 * piixl-postgres (port 5433)
 * piixl-mongodb (port 8094)
 * profile-mongo (port 8095)
---
4. **Start backend services in order**
    * **Discovery Server**
    Open a new terminal and run this (Terminal 6)

    ```bash
    cd piixl/discovery-server
    ./mvnw spring-boot:run
    ```
    * **Auth Service**
    Open a new terminal and run this (Terminal 7)

    ```bash
    cd piixl/auth-service
    ./mvnw spring-boot:run
    ```
    * **Media Service**
    Open a new terminal and run this (Terminal 8)

    ```bash
    cd piixl/media-service
    ./mvnw spring-boot:run
    ```
    * **Profile Service**
    Open a new terminal and run this (Terminal 9)

    ```bash
    cd piixl/profile-service
    ./mvnw spring-boot:run
    ```
    * **API Gateway**
    Open a new terminal and run this (Terminal 10)

    ```bash
    cd piixl/api-gateway
    ./mvnw spring-boot:run
    ```
---
5. **Start Frontend**
Open a new terminal and run this (Terminal 11)
    ```bash
    cd piixl/frontend
    npm install
    npm start
    ```
---
6. **Final Check**
Once everything is running, you should be able to acess the links below in the Environment and Access section.

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
