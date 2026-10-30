# Digital Resource Management Platform

This is a web application I developed for a university project using Java, Spring Boot and MySQL.

The application is designed to manage university courses, learning materials and digital resources. It has four types of users, each with different permissions and features.

## Technologies Used

- Java
- Spring Boot
- MySQL
- Spring Data JPA and Hibernate
- Thymeleaf
- HTML, CSS and JavaScript
- Maven

## Features

- Four user roles: Student, Professor, Administrator and Auditor
- Course enrollment and learning materials
- Token allocation and resource usage tracking
- Resource requests and approvals
- Statistics and activity logs

## Project Structure

- `src/main/java` – Java code
- `src/main/resources/templates` – HTML pages using Thymeleaf
- `src/main/resources/static` – CSS and JavaScript files
- `pom.xml` – Maven dependencies

## How to Run

1. Clone the repository and open it in IntelliJ IDEA.
2. Make sure Java and MySQL are installed.
3. Create a MySQL database and configure the connection in `application.properties`.
4. Set the `DB_PASSWORD` environment variable with your database password.
5. Run the Spring Boot application.
