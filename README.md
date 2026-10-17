
# Klinikus API

REST API for a medical tele-expertise platform that connects nurses, general practitioners, and specialists to facilitate medical consultation requests and responses.

## Technologies

- **Java 17**
- **JAX-RS (Jersey)** — REST API development
- **JPA / Hibernate** — Persistence and ORM
- **PostgreSQL** — Relational database
- **Jackson** — JSON serialization and deserialization
- **Maven** — Dependency management and build
- **Apache Tomcat 10.1+** — Deployment
- **Postman** — API testing
- **BCrypt** — Password hashing
- **HTTP Basic Authentication** — Stateless authentication

## Features

- User authentication and identification
- Role-based access control
- Stateless authentication using HTTP Basic Authentication
- Password hashing with BCrypt
- Retrieval of the authenticated user's information
- Search for specialists by medical specialty
- Creation and management of tele-expertise requests
- Responses to tele-expertise requests
- JSON request and response handling
- Integration with the existing Klinikus PostgreSQL database

## User Roles

The API supports three main roles:

| Role | Description |
|---|---|
| `INFIRMIER` | Creates and manages tele-expertise requests according to permissions |
| `GENERALISTE` | Participates in medical tele-expertise workflows according to permissions |
| `SPECIALISTE` | Receives and responds to relevant tele-expertise requests according to permissions |

Access to protected resources is controlled by the application's authorization rules.

## API Endpoints

Base URL:

```text
http://localhost:8080/api/
```

The exact context path depends on the deployment configuration.

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/login` | Authenticate a user |
| GET | `/auth/me` | Retrieve the authenticated user's information |

### Specialists

| Method | Endpoint | Description |
|---|---|---|
| GET | `/specialistes` | Retrieve specialists |
| GET | `/specialistes?specialite=cardiologie` | Filter specialists by specialty |

The specialty value is an example; use a specialty supported by the application.

### Tele-expertise Requests

| Method | Endpoint | Description |
|---|---|---|
| POST | `/demandes` | Create a tele-expertise request |
| GET | `/demandes` | Retrieve tele-expertise requests |
| PUT | `/demandes/{id}/reponse` | Submit a response to a request |

**Note:** Access permissions, required fields, and available filters depend on the implemented business rules.

## Example Requests

### 1. Authenticate

```http
POST /api/auth/login
Content-Type: application/json
```

Example request body (adapt the fields to the implemented DTO):

```json
{
  "email": "user@example.com",
  "password": "your-password"
}
```

### 2. Retrieve the Current User

```http
GET /api/auth/me
Authorization: Basic <base64-credentials>
```

Use the credentials configured for an authorized user.

### 3. Search Specialists

```http
GET /api/specialistes?specialite=cardiologie
Authorization: Basic <base64-credentials>
```

### 4. Create a Tele-expertise Request

```http
POST /api/demandes
Content-Type: application/json
Authorization: Basic <base64-credentials>
```

The JSON body must match the request DTO defined in the application.

### 5. Respond to a Request

```http
PUT /api/demandes/1/reponse
Content-Type: application/json
Authorization: Basic <base64-credentials>
```

The request body must contain the response fields expected by the API.

## Security

- Passwords are hashed using BCrypt rather than stored as plain text.
- HTTP Basic Authentication is stateless: each protected request supplies authentication credentials.
- `SecurityContext` and authorization rules are used to identify the current user and restrict access.
- Protected endpoints must enforce the appropriate role permissions.
- Sensitive credentials must not be committed to version control.
- HTTPS should be used in production to protect credentials in transit.

## Database Configuration

The application uses PostgreSQL and shares the database with the Klinikus application.

Before starting the API:

1. Install and start PostgreSQL.
2. Create or configure the required database.
3. Configure the database connection in the application's persistence or configuration files.
4. Verify that the database credentials and schema match the application.
5. Ensure that Hibernate is configured appropriately for the environment.

Do not commit real database passwords or other secrets.

## Installation and Setup

### Prerequisites

- JDK 17
- Maven
- PostgreSQL
- Apache Tomcat 10.1+ if deploying as a WAR
- Postman or another HTTP client

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Klinikus-API
```

Replace `<repository-url>` with the actual repository URL.

### 2. Configure the Database

Update the database connection settings to match your local PostgreSQL environment.

### 3. Build the Project

```bash
mvn clean package
```

The generated WAR file should be available in the `target/` directory if the project is configured for WAR packaging.

### 4. Deploy the Application

Deploy the generated WAR to Tomcat, then start the server.

The actual API URL depends on the deployed WAR name and the application's JAX-RS configuration.

For example, if the deployed application context is `api` and the JAX-RS base path is `/api/`, verify the resulting URL to avoid duplicating the path.

### 5. Test with Postman

Test the authentication endpoint first, then test protected endpoints using the authentication mechanism and roles configured in the application.

## Testing Checklist

- [ ] Application starts successfully.
- [ ] PostgreSQL connection works.
- [ ] Valid credentials authenticate successfully.
- [ ] Invalid credentials are rejected.
- [ ] The current-user endpoint returns the authenticated user.
- [ ] Specialist retrieval works.
- [ ] Filtering by specialty works.
- [ ] Authorized users can create tele-expertise requests.
- [ ] Unauthorized users cannot access restricted resources.
- [ ] Valid responses to requests are accepted.
- [ ] Invalid request bodies return appropriate HTTP errors.
- [ ] Passwords are never exposed in API responses.

## Project Structure

The application is organized around REST resources, services, persistence, domain models, and security components.

```text
Klinikus-API/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ma/klinikus/
│   │   │       ├── resource/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── model/
│   │   │       ├── dto/
│   │   │       ├── filter/
│   │   │       └── config/
│   │   └── resources/
│   └── test/
├── pom.xml
└── README.md
```

This is a conceptual structure; adapt it to the actual packages and directories in the repository.

## Architecture

The API follows a layered architecture:

- **Resources:** Expose HTTP endpoints and handle requests and responses.
- **Services:** Implement business logic and coordinate operations.
- **Repositories / DAOs:** Handle data access and persistence.
- **Models:** Represent domain entities.
- **DTOs:** Define the data exchanged through the API.
- **Filters and security:** Handle authentication and request authorization.
- **JPA / Hibernate:** Map Java entities to relational database tables.

## Error Handling

The API should return appropriate HTTP status codes, for example:

| Status | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | Resource created successfully |
| `400 Bad Request` | Invalid request data |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Insufficient permissions |
| `404 Not Found` | Resource not found |
| `500 Internal Server Error` | Unexpected server error |

Actual status codes depend on the endpoint implementation.

## Author

**Badr FENNI**

Full Stack Developer  
Java · JAX-RS · Hibernate · PostgreSQL · REST APIs

## License

Add the appropriate license if the project is intended for public distribution.
