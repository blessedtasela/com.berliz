# Backend Setup — Berliz

## Requirements
- Java 17
- Maven
- PostgreSQL

## Local Setup
```bash
git clone https://github.com/blessedtasela/com.berliz.git
cd com.berliz
mvn clean install
```
#### Environment Variables
Create .env (do not commit):

```ini
DB_HOST=localhost
DB_PORT=5432
DB_NAME=berliz
DB_USER=postgres
DB_PASSWORD=password
STRAPI_URL=https://strapi.berliz.fitness
SECRET_KEY=your_secret_key
```
Run
```bash
mvn spring-boot:run
```