# JSON Dataset Query API

A Spring Boot application to manage and query JSON dataset records with group-by and sort-by capabilities.

## How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/SupreethRagavendra/json-query-api.git
   cd dataset-api
   ```
2. **Build the project:**
   ```bash
   mvn clean install
   ```
3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
The application runs on `http://localhost:8080`.

## API Endpoints

### 1. Insert Record API
Insert a single JSON record into a specific dataset.

- **URL:** `POST /api/dataset/{datasetName}/record`
- **Request Body:**
  ```json
  {
      "id": 1,
      "name": "John Doe",
      "age": 30,
      "department": "Engineering"
  }
  ```
- **Response:**
  ```json
  {
      "message": "Record added successfully",
      "dataset": "employee_dataset",
      "recordId": 1
  }
  ```

### 2. Query API (Group-By)
Query a dataset and group records by a specific field.

- **URL:** `GET /api/dataset/{datasetName}/query`
- **Query Param:** `groupBy={fieldName}`
- **Example:** `/api/dataset/employees/query?groupBy=department`
- **Response:**
  ```json
  {
      "groupedRecords": {
          "Engineering": [ ... ],
          "Marketing": [ ... ]
      }
  }
  ```

### 3. Query API (Sort-By)
Query a dataset and sort records by a specific field.

- **URL:** `GET /api/dataset/{datasetName}/query`
- **Query Params:** `sortBy={fieldName}&order={asc|desc}`
- **Example:** `/api/dataset/employees/query?sortBy=age&order=desc`
- **Response:**
  ```json
  {
      "sortedRecords": [ ... ]
  }
  ```

---

### Batch Insert
Enables inserting multiple records in a single request.

- **URL:** `POST /api/dataset/{datasetName}/batch`
- **Request Body:**
  ```json
  [
    { "id": 1, ... },
    { "id": 2, ... }
  ]
  ```

## Technologies
- Java 17
- Spring Boot 3.2.0
- H2 Database (In-memory)
- Spring Data JPA
- Lombok

## Testing Preview


<img width="1920" alt="Postman Request Example" src="https://github.com/user-attachments/assets/a18f70cf-1537-49fb-906e-df968385d9b6" />



<img width="1920" alt="Browser Query Example" src="https://github.com/user-attachments/assets/070fd698-a107-45e3-9c31-a6a2d891e26a" />


