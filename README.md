Andromeda Authorization Server

Author: Szymon Derleta  
Current Version: 3.0.0

Changelog for version 3.0.0:

1. **Added over 500 JUnit and integration tests**: Enhanced the reliability and robustness of the application by
   ensuring comprehensive test coverage across all key components.
2. **Rebuilt the codebase**: Refactored and optimized the existing code for improved performance, maintainability, and
   scalability.
3. **Added support for refresh tokens**: Implemented support for secure refresh tokens. Both access and refresh tokens
   are now generated as `HttpOnly` cookies to enhance security and prevent client-side access.

Overview

Andromeda Authorization Server is a robust and versatile application built with Java SDK 21 and the advanced Spring Boot
Framework 3.2. This server effectively manages access and authorization, prioritizing security with the use of JWT (JSON
Web Token) authentication. It also integrates seamlessly with MariaDB using JDBC connections and supports email
communication via Google accounts, offering an all-in-one solution for access control and user authentication. It can
also serve as an open-source example of Jakarta EE interoperability with Spring Data JDBC and Spring MVC, showcasing
modern enterprise-level application development.
Key Features

    1. JWT Token Authentication: Ensures enhanced security by implementing JSON Web Tokens, providing a reliable and secure authentication mechanism to safeguard applications from unauthorized access.

    2. MariaDB Integration: Effortlessly integrates with MariaDB using JDBC for efficient and secure management of access and authorization data.

    3. Email Services with Google Accounts: Comes equipped with a built-in email service utilizing Google accounts for convenient handling of password resets and activation link emails, simplifying the user account management experience.

    4. Jakarta EE and Spring Compatibility: Demonstrates seamless interoperability between Jakarta EE technologies and Spring, combining the strengths of both frameworks for scalable, secure enterprise applications.

    5. Enterprise-Level Scalability: Leverages modern Java SDK 21 features and advanced Spring Boot capabilities to provide a highly scalable and maintainable solution for handling access control and user authentication in enterprise systems.

Note: All endpoints should be verified carefully during the final stages of development to ensure security and proper
functionality. Some of them might need to be removed or restricted for security reasons. Consider this a warning to
review all endpoints thoroughly, especially those handling sensitive data or authentication, to avoid potential
vulnerabilities.

Endpoints Examples:

1. **POST request to login with username and get cookie with jwtToken**

   **Description:** Allows a user to log in using their username. The server responds with a JWT token set as a cookie.

   **Endpoint:**  
   `POST http://localhost:8087/api/v3/auth/login`

   **Headers:**
   - `Content-Type: application/json`
   - `X-Requesting-App: nebula_rest_api`

   **Request Body:**
    ```json
    {
        "login": "user",
        "password": "password"
    }
    ```

2. **POST request to login with email and get cookie with jwtToken**

   **Description:** Allows a user to log in using their email. The server responds with a JWT token set as a cookie.

   **Endpoint:**  
   `POST http://localhost:8087/api/v3/auth/login`

   **Headers:**
   - `Content-Type: application/json`
   - `X-Requesting-App: nebula_rest_api`

   **Request Body:**
    ```json
    {
        "login": "user@local.com",
        "password": "password"
    }
    ```

3. **GET request for token page (using cookie authorization)**

   **Description:** Fetches a paginated list of tokens. Authorization is handled via a JWT token included in the cookie.

   **Endpoint:**  
   `GET http://localhost:8087/api/v1/table/tokens?page=0&size=5`

   **Headers:**
   - `Content-Type: application/json`
   - `X-Requesting-App: nebula_rest_api`
   - `Authorization: Bearer <your_jwt_token>`

   **Sample Bearer:**
    ```
    eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDExLHhic212dXpmYXl5emp4ZHhha0Bja3B0ci5jb20iLCJpc3MiOiJEYkNvbm5lY3Rpb25BcHAiLCJyb2xlcyI6W3siaWQiOjEsIm5hbWUiOiJST0xFX1VTRVIifSx7ImlkIjo0LCJuYW1lIjoiUk9MRV9BRE1JTiJ9XSwiaWF0IjoxNzQzODU3MDA2LCJleHAiOjE3NDM4NjA2MDZ9.R9s7rPSaj0QD7qPhlPLQ_KTjSX8krhSa3OxMxwOvnLzSC4_tKacMxEtkQqZs9XYyeOQTDAp8id_6yJFZel0ppQ

    ```

4. **GET request for roles by role name filter (using cookie authorization)**

   **Description:** Retrieves roles filtered by role name. Authorization is handled via a JWT token included in the
   cookie.

   **Endpoint:**  
   `GET http://localhost:8087/api/v1/table/roles?roleNameFilter=er`

   **Headers:**
   - `Content-Type: application/json`
   - `X-Requesting-App: nebula_rest_api`
   - `Authorization: Bearer <your_jwt_token>`

   **Sample Bearer:**
    ```
    eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDExLHhic212dXpmYXl5emp4ZHhha0Bja3B0ci5jb20iLCJpc3MiOiJEYkNvbm5lY3Rpb25BcHAiLCJyb2xlcyI6W3siaWQiOjEsIm5hbWUiOiJST0xFX1VTRVIifSx7ImlkIjo0LCJuYW1lIjoiUk9MRV9BRE1JTiJ9XSwiaWF0IjoxNzQzODU3MDA2LCJleHAiOjE3NDM4NjA2MDZ9.R9s7rPSaj0QD7qPhlPLQ_KTjSX8krhSa3OxMxwOvnLzSC4_tKacMxEtkQqZs9XYyeOQTDAp8id_6yJFZel0ppQ

    ```

Building and Running the Project
To build and run the Andromeda Authorization Server, follow these steps:
Prerequisites:

1. **Java SDK 21**: Ensure that Java SDK 21 or higher is installed and set up on your system.
2. **Maven**: The project uses Maven for dependency management and build automation. Ensure Maven (version 3.8 or
   higher) is installed.
3. **MariaDB**: Install and configure MariaDB as the application requires a database to manage access and authorization
   data.
4. **Google Account for Email Services**: Set up a Google account for email integration, as it will be used for password
   reset and activation email functionality.
   Steps to Build and Run:
1. Clone the repository or download the project source code:
    ```bash
    git clone <repository_url>
    cd andromeda-authorization-server
    ```
2. Configure the `application.properties` or `application.yml` file located in the `src/main/resources` directory.
   Update the following:
   - Database connection details:
     ```properties
     spring.datasource.url=jdbc:mariadb://<host>:<port>/<database>
     spring.datasource.username=<your_database_username>
     spring.datasource.password=<your_database_password>
     ```
   - Google email account settings:
     ```properties
     spring.mail.host=smtp.gmail.com
     spring.mail.port=587
     spring.mail.username=<your_google_email>
     spring.mail.password=<your_email_password>
     spring.mail.protocol=smtp
     spring.mail.properties.mail.smtp.auth=true
     spring.mail.properties.mail.smtp.starttls.enable=true
     ```
3. Build the project using Maven:
    ```bash
    mvn clean install
    ```
4. Run the application:
    ```bash
    mvn spring-boot:run
    ```
   Alternatively, if you prefer to run the built JAR file:
    ```bash
    java -jar target/andromeda-authorization-server-3.0.0-beta.jar
    ```
5. Access the application at:
    ```url
    http://localhost:8443
    ```

Important Notes:

1. MariaDB must be running, and the database mentioned in the `spring.datasource.url` must exist.
2. Ensure that the Google account used for email services has less secure app access enabled or an app password
   generated if required.
3. Logs will be generated in the `logs` directory for monitoring purposes during development and debugging.
