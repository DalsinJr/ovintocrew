# Ovinto Crew Employee Proximity API
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Swagger](https://img.shields.io/badge/swagger-85EA2D.svg?style=for-the-badge&logo=swagger&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230096ED.svg?style=for-the-badge&logo=docker&logoColor=white)

This project is a Spring Boot application that processes employee data in XML format to determine:

- Which employees live within a given range (e.g., 30 km) of the Ovinto HQ.
- Which employee lives the furthest away from the HQ.

The application retrieves employee coordinates via an external location service, calculates distances (using the Google Distance Matrix API when possible, and falling back to a Haversine formula otherwise), and returns a JSON response with proximity information.

## Features

- **XML Parsing:**  
  Parse employee data from XML files (multipart file or XML request body).

- **Geolocation:**  
  Retrieve employee coordinates via an external location service.

- **Distance Calculation:**  
  Compute the distance from the HQ using the Google Distance Matrix API with a fallback to the Haversine formula.

- **Asynchronous Processing:**  
  Processes employee locations incrementally and in parallel.

- **API Documentation:**  
  Documented using SpringDoc OpenAPI/Swagger.  
  Access the documentation at: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

- **Global Exception Handling:**  
  Uses `@ControllerAdvice` to handle errors (including unsupported media types, HTTP 415) and return simple error messages.

## Technology Stack

- **Java:** 21
- **Spring Boot:** 3.x
- **Lombok**
- **SpringDoc OpenAPI**
- **Maven**
- **JUnit 5 & Mockito**

## Configuration

The application uses externalized configuration in `application.yml`. For example:

```yaml
employee:
  hq:
    lat: 51.10
    lon: 3.43
  threshold:
    km: 30.0

google:
  distance:
    api-key: "YOUR_GOOGLE_API_KEY"
    base-url: "https://maps.googleapis.com/maps/api/distancematrix/json"

# Other Spring Boot configuration...
```
> **Note:** Replace `"YOUR_GOOGLE_API_KEY"` with your actual Google API key.

## API Endpoints

### Process Employees from Multipart File
- **URL:** `/api/employees/process`
- **Method:** `POST`
- **Content-Type:** `multipart/form-data`
- **Parameter:** `file` – XML file containing employee data

### Process Employees from XML Body
- **URL:** `/api/employees/process`
- **Method:** `POST`
- **Content-Type:** `application/xml`
- **Body:** Raw XML content

Both endpoints return a JSON response of type `EmployeeProximityResponse`, which includes:
- A list of employees within the threshold distance.
- The employee who is the furthest from the HQ.

---

## Running the Application

### Build the project with Maven:
```bash
  mvn clean install
```
## Run the application:
```bash
  mvn spring-boot:run
```
Alternatively, run the generated jar:
```bash
    java -jar target/ovinto-demo-0.0.1-SNAPSHOT.jar
```

## Running the application with Docker

### Build the project and create the JAR
```bash
  mvn clean package
```

### Build and run the container:

```bash
  docker-compose up --build -d
```
## Check logs:

```bash
  docker logs -f ovintocrew
```
###  Stop the container:
```bash
    docker-compose down
```



## API Documentation

Once the application is running, you can access the Swagger UI documentation at: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


## Logging & Error Handling

### Logging
- The application logs key events such as employee parsing, distance calculations, and fallback actions.

### Global Exception Handling
- A `@ControllerAdvice` handles exceptions across the application.
- For example, unsupported media types (HTTP 415) and other errors return a simple error message.
