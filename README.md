# Customized Dashboard - Backend

This is the backend codebase for the Customized Dashboard Application, providing RESTful APIs to manage user dashboards and data visualizations.

## Features
- **RESTful API**: Supports CRUD operations for user dashboards and charts.
- **Data Storage**: Uses mock JSON files for storage and quick prototyping.
- **Dynamic Data Filtering**: Aggregates and filters data based on user inputs.
- **Budget Handling**: Supports financial domain data visualization (limited to read-only budgets).

## Technology Stack
- **Spring Boot**: Main framework for building RESTful APIs.
- **Jackson**: For reading and writing JSON data.
- **Mock JSON**: Temporary storage solution for dashboard and chart data.

## Installation and Setup
1. Clone the repository:
2. Build the project:
   ```
   ./mvnw clean install
   ```
3. Run the Spring Boot application
   ```
   ./mvnw spring-boot:run
   ```
4. The backend will be available at http://localhost:8080

## API Endpoints
- Dashboard Management
  - Fetch User Dashboard: GET /api/dashboard/{userId}
  - Add Chart to Dashboard: POST /api/dashboard/{userId}/add
  - Update Chart in Dashboard: PUT /api/dashboard/{userId}/update/{chartId}
  - Delete Chart from Dashboard: DELETE /api/dashboard/{userId}/delete/{chartId}
- Data Operations
  - Fetch Filtered Data: GET /api/data/filter
    - Query Parameters:
    - dimension: (e.g., CloudProvider, Region, Account)
    - groupBy: (e.g., day, month, quarter, year)
    - from: Start date (e.g., 2023-01-01)
    - to: End date (e.g., 2023-12-31)
    - maxDisplay: Maximum number of data points (e.g., 10)

## Project Structure
- /src/main/java/com/example/customized/dashboard/
  - controller/: Contains REST controllers.
  - repository/: Handles data storage and retrieval from JSON files.
  - service/: Implements business logic.
- /src/main/resources/data/
  - charts.json: Stores user dashboard and chart information.
  - mockData.json: Sample data for chart rendering.

## Improvements
- Replace JSON file storage with a database (e.g., MySQL, PostgreSQL).
- Implement support for budget modifications and real-time chart updates.
- Optimize APIs for large-scale datasets.
- Add authentication and authorization for user-specific dashboards.
