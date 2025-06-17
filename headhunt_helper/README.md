# Job Application Database Manager

A Spring Boot application for managing job applications.

## Prerequisites

- Java 17 or higher
- Maven (optional, as the project includes Maven wrapper)

## Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application using one of these methods:

### Using Maven Wrapper (Recommended)

On Unix/Mac:
```bash
./mvnw spring-boot:run
```

On Windows:
```bash
mvnw.cmd spring-boot:run
```

### Using Maven (if installed)
```bash
mvn spring-boot:run
```

## Accessing the Application

Once the application is running:

1. The REST API will be available at: `http://localhost:8080/api/applications`
2. H2 Database Console: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:jobdb`
   - Username: `sa`
   - Password: `password`

## API Endpoints

- `GET /api/applications` - Get all job applications
- `GET /api/applications/{id}` - Get a specific job application
- `POST /api/applications` - Create a new job application
- `PUT /api/applications/{id}` - Update an existing job application
- `DELETE /api/applications/{id}` - Delete a job application
- `GET /api/applications/search/company?companyName=...` - Search by company name
- `GET /api/applications/search/position?position=...` - Search by position
- `GET /api/applications/status/{status}` - Get applications by status

## Example API Usage

### Create a new job application
```bash
curl -X POST http://localhost:8080/api/applications \
-H "Content-Type: application/json" \
-d '{
    "companyName": "Example Corp",
    "position": "Software Engineer",
    "jobUrl": "https://example.com/job",
    "status": "APPLIED",
    "location": "New York",
    "salary": "$100,000",
    "contactPerson": "John Doe",
    "contactEmail": "john@example.com",
    "notes": "Great opportunity"
}'
```

### Get all applications
```bash
curl http://localhost:8080/api/applications
``` 