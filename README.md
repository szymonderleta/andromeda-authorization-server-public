# Andromeda Authorization Server

<div align="left">

![Author](https://img.shields.io/badge/Author-Szymon%20Derleta-white?style=for-the-badge)

![Release](https://img.shields.io/badge/Release-Public%20Release-green?style=for-the-badge)  
![Version](https://img.shields.io/badge/Version-3.1.1-green?style=for-the-badge)

📄 Changelog: [CHANGELOG.md](info/CHANGELOG.md)  
🔗 Repository: [GitHub - Andromeda Authorization Server](https://github.com/szymonderleta/andromeda-authorization-server-public)  
🛠️ Jenkins Pipeline: [JENKINS.MD](info/JENKINS.MD)
</div>

---
## Overview

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

---
## Endpoints Examples:

This application supports and issues JWT-based cookies for accessToken and refreshToken. These tokens are used for authentication and session management. The accessToken provides authorization for accessing protected resources, while the refreshToken allows the renewal of the accessToken when it expires.
Example Endpoint

The following example demonstrates how to use the refreshToken to obtain a new accessToken. The request is sent to the /api/v1/auth/refresh-access endpoint.

```http request
POST http://localhost:8443/api/v1/auth/refresh-access
Content-Type: application/json
X-Requesting-App: nebula_rest_api
Cookie: refreshToken=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDExLHhic212dXpmYXl5emp4ZHhha0Bja3B0ci5jb20iLCJpc3MiOiJEYkNvbm5lY3Rpb25BcHAiLCJyb2xlcyI6W3siaWQiOjEsIm5hbWUiOiJST0xFX1VTRVIifSx7ImlkIjo0LCJuYW1lIjoiUk9MRV9BRE1JTiJ9XSwiaWF0IjoxNzQ2MjY3NDg4LCJleHAiOjE3NDg4NTk0ODh9.vohJv_OODIQ7uSMPoHSOutLrPSVO1OyMi_7Eg32PFPfFzPLOxcnrqm6BV-bI_1WSBKJBOuc5m65aMRSSM019uw
```

Examples of endpoints in the form of .http files supported by the IntelliJ environment can be found at the location test/endpoints.

---
## Building the Project

To build the Andromeda Authorization Server project, follow the steps below:

1. **Clone the Repository**  
   Open a terminal and clone the repository using the following command:
   ```bash
   git clone https://github.com/szymonderleta/andromeda-authorization-server-public.git
   ```
   Navigate to the project directory:
   ```bash
   cd andromeda-authorization-server-public
   ```

2. **Prerequisites**  
   Ensure you have the following installed on your system:
   - **Java SDK 21**  
     Download and install [Java SDK 21](https://jdk.java.net/21/) to support the project.
   - **Maven**  
     Install [Apache Maven](https://maven.apache.org/download.cgi) to manage project dependencies and builds.
   - **MariaDB**  
     Set up and run a MariaDB server that will be used for the database connection.

3. **Configure Application Properties**  
   Edit the `application.properties` file in the `src/main/resources` directory to provide your database and email
   configuration:
   ```properties
   spring.datasource.url=jdbc:mariadb://<your-database-host>:<port>/<database-name>
   spring.datasource.username=<your-database-username>
   spring.datasource.password=<your-database-password>

   # Google email configuration (for email services)
   spring.mail.username=<your-email@gmail.com>
   spring.mail.password=<your-email-password>
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.protocol=smtp
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

   Replace the placeholders (e.g., `<your-database-host>`, `<your-email@gmail.com>`) with your actual configuration
   values.

4. **Build the Project**  
   Use Maven to package the application. Run the following command in the terminal within the project directory:
   ```bash
   mvn clean package
   ```
   This will generate a JAR file in the `target` directory.

5. **Run the Application**  
   Start the application using the following command:
   ```bash
   java -jar target/andromeda-authorization-server-3.0.0.jar
   ```
   Ensure that your MariaDB server is running and reachable.

6. **Access the Application**  
   The server will start by default on `http://localhost:8080`. You can update the port or other settings in the
   `application.properties` file if needed.

7. **Verify the Setup**  
   Test the API endpoints or access the server to ensure everything is configured correctly.
