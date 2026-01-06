
# Berliz — Backend Application

[![Build Status](https://img.shields.io/badge/status-active-brightgreen)]()

The **Berliz backend** powers the fitness and combat sports platform, handling user data, workouts, media uploads, and other API requests. Built with **Spring Boot**, it connects to a **PostgreSQL database** and exposes RESTful APIs for the frontend. Strapi is also integrated as a headless CMS for managing media and content.

## 🚀 Live API
### The app is accessible at:  

#### [https://berliz.fitness](https://berliz.fitness) 

## 🛠️ Technologies & Libraries
- **Java & Spring Boot** — Backend framework and REST API  
- **Spring Data JPA** — Database access and ORM  
- **PostgreSQL** — Relational database  
- **Strapi** — Headless CMS for media management  
- **Maven** — Dependency management and build tool  
- **Spring Security** — Authentication and authorization (if implemented)  
- **Lombok** — Boilerplate code reduction  
- **Swagger / OpenAPI** — API documentation (if configured)  
- **Netlify / Heroku** — Hosting for APIs (if deployed)  
- **Git & GitHub** — Version control  

## 📁 Project Structure
```text
src/
  main/
    java/
      com/berliz/
        controllers/       # REST API endpoints
        models/            # Database entity models
        repositories/      # JPA repositories
        services/          # Business logic
        config/            # Application configuration
    resources/
      application.properties # App configuration
      static/                # Static resources
      templates/             # Templates (if any)
pom.xml                     # Maven dependencies and build config
target/                      # Compiled build artifacts (ignored in Git)
HELP.md                      # Additional instructions
.env                          # Environment variables (not committed)
.set_env_vars.sh             # Environment helper script (not committed)
.gitignore                    # Files to ignore in Git
```

## 🚦 Features Implemented
### RESTful API endpoints for:

- User management

- Workout tracking

- Media uploads (images, videos) via Strapi

- PostgreSQL database integration

- Strapi service for media CRUD operations

- Environment configuration via .env file

- Basic input validation and error handling

## 📌 Setup Instructions
### Clone the repository:

``` bash
git clone https://github.com/blessedtasela/com.berliz.git
cd com.berliz
```
### Install dependencies:

``` bash
mvn clean install
```

### Configure environment variables:
#### Create a .env file in the root (do not commit it):

``` ini
DB_HOST=localhost
DB_PORT=5432
DB_NAME=berliz
DB_USER=your_db_user
DB_PASSWORD=your_db_password
SECRET_KEY=your_secret_key
STRAPI_URL=https://strapi.berliz.fitness
```
### Run the application:

``` bash
mvn spring-boot:run
```
#### The API will be available at http://localhost:8080

Build for production:

``` bash
mvn clean package
```
#### The compiled JAR file will be in target/

### Run tests:

``` bash
mvn test
```
## 📚 Notes
- Keep .env secret; it contains sensitive credentials.

- Strapi integration is used for managing media content.

- All API endpoints are documented in the service and controller classes.