# Facebook Clone

## 1. Overview

This project is a full-stack social media platform engineered to emulate core functionalities of Facebook. It is developed with a Java-based backend and a JavaScript-driven frontend, structured around a microservices architecture for enhanced scalability and modularity.

![Demo](/Animationn.gif)

## 2. Architecture

The system employs a microservices architecture comprising two primary backend services, alongside a frontend application:

*   **Auth & Communication Service (Java)**: This service is responsible for all aspects of user authentication (registration, login, session management, JWT issuance), authorization, and handles communication channels such as email notifications, SMS alerts, and potentially real-time messaging notifications.
*   **Core Platform Service (Java)**: This service manages the primary social media functionalities, including post creation and management (with media uploads to AWS S3), feed generation, social graph interactions (friendships, follows), and content delivery.
*   **Frontend Application (JavaScript/React)**: A Single Page Application (SPA) that provides the user interface and interacts with the backend services via their respective APIs.
*   **API Communication**: Services expose RESTful APIs. Inter-service communication, if necessary beyond direct API calls, might utilize asynchronous messaging queues.

## 3. Tech Stack

Based on the repository's language composition and common practices for such applications:

### 3.1. Backend (Applicable to both Microservices where relevant)
*   **Primary Language**: Java (JDK 11/17/21+)
*   **Framework**: Likely Spring Boot (due to its prevalence for microservices and REST APIs in Java).
*   **Build Tool**: Maven or Gradle.
*   **Data Persistence**:
    *   **Auth & Communication Service**: Might use a relational database (PostgreSQL, MySQL) for user credentials and notification logs.
    *   **Core Platform Service**: Postgres managed via Spring Data JPA / Hibernate.
*   **Object Storage (for Core Platform Service)**:
    *   **AWS S3**: For storing user-uploaded media files (images, videos). Integration via AWS SDK for Java.
*   **Authentication/Authorization**: Spring Security with JWT.
*   **Caching**: Redis, Hazelcast, or Ehcache (for performance optimization in both services).
*   **API**: RESTful APIs.
*   **Email Service Integration**: JavaMail API, potentially with third-party services like SendGrid or AWS SES.
*   **SMS Service Integration**: Libraries for integrating with SMS gateways like Twilio or Vonage.

### 3.2. Frontend
*   **Primary Language**: JavaScript (ES6+) / TypeScript.
*   **Framework/Library**: React.
*   **Styling**: CSS3, SASS/LESS, CSS Modules, Styled Components, or utility-first CSS (e.g., Tailwind CSS).
*   **Build Tools**: Vite.
*   **API Consumption**: Axios, Fetch API.
*   **Real-time Communication**: WebSockets (e.g., connecting to the Auth & Communication Service for notifications).

## 4. Key Features

### 4.1. Auth & Communication Service
*   User registration and secure login.
*   Password management (hashing, recovery).
*   JWT-based session management.
*   Role-based access control.
*   Email notification system.
*   SMS notification system.

### 4.2. Core Platform Service
*   User profile management.
*   Content creation (text posts, media uploads to AWS S3).
*   Interactive news feed generation.
*   Social interactions (likes, comments, shares).
*   Friendship/Connection management.
*   Search functionality for users and content.

## 5. Prerequisites for Development
*   Java Development Kit (JDK version as per project).
*   Maven or Gradle build tool.
*   Node.js and npm/yarn (for frontend development).
*   Access to database instances (e.g., PostgreSQL, MongoDB running locally or in Docker).
*   AWS Account and configured credentials (for S3 integration, if not using local alternatives like MinIO for development).
*   IDE: IntelliJ IDEA (for Java), VS Code (for JavaScript/Java).

## 6. Project Setup and Configuration

### 6.1. Backend Services Setup (Example using Spring Boot with Maven)
*(General steps to be adapted for each microservice: `auth-communication-service` and `core-platform-service`)*

1.  Clone the repository: `git clone https://github.com/epuic/Facebook-clone.git`
2.  Navigate to the specific service directory:
    *   `cd Facebook-clone/auth-communication-service`
    *   OR `cd Facebook-clone/core-platform-service`
3.  Configure `application.properties` or `application.yml` within each service directory. This includes:
    *   Database connection details (unique for each service if they use separate databases).
    *   Server port (ensure distinct ports for each service, e.g., 8081 for auth, 8082 for core).
    *   Messaging queue connection details (if used).
    *   Credentials for external services (email, SMS gateways).
    *   **AWS S3 Credentials (for Core Platform Service)**: Access key, secret key, region, and bucket name. Ensure these are securely managed (e.g., via environment variables, AWS IAM roles if deployed on EC2/ECS).
    *   JWT secret keys (the auth service generates, the core service validates).
4.  Build the project: `mvn clean install` (within each service directory).
5.  Run the service: `mvn spring-boot:run` or `java -jar target/service-name.jar` (for each service, in separate terminals or managed by a script).

### 6.2. Frontend Setup (Example using React)
1.  Navigate to the frontend directory: `cd Facebook-clone/frontend-directory`
2.  Install dependencies: `npm install` or `yarn install`
3.  Configure environment variables (e.g., API base URLs for both backend services in `.env` files, pointing to their respective ports or an API Gateway).
4.  Start the development server: `npm start` or `yarn start`
