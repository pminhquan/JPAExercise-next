# JPAExercise-next

A Java web application built with Jakarta Servlet, JSP, JPA/Hibernate, Maven, and Microsoft SQL Server.

The project demonstrates a layered web architecture and includes authentication, OTP-based account flows, category management, product management, pagination, and database integration using JPA/Hibernate.

## Author

* **Name:** Phạm Minh Quân
* **Student ID:** 24110311
* **Class:** 241103A

## Repository

`https://github.com/pminhquan/JPAExercise-next`

---

## Technologies

* Java 21
* Maven Wrapper
* Jakarta Servlet 6.0
* JSP / JSTL
* JPA
* Hibernate ORM 6.6.1.Final
* Microsoft SQL Server
* Jakarta Mail
* Apache Tomcat 10.1
* JUnit 5

---

## Project Architecture

The project follows a layered architecture:

```text
JSP
 ↓
Controller
 ↓
Service
 ↓
DAO
 ↓
JPA EntityManager
 ↓
Hibernate
 ↓
Microsoft SQL Server
```

Main packages:

```text
src/main/java/com/hcmute/jpa
├── config
├── controller
├── dao
├── entity
├── filter
└── service
```

JPA configuration:

```text
src/main/resources/META-INF/persistence.xml
```

Web configuration:

```text
src/main/webapp/WEB-INF/web.xml
```

Views:

```text
src/main/webapp/views
```

---

# Main Features

## Authentication

* User registration
* Login
* Logout
* Authentication filter
* Account activation
* OTP verification
* Forgot password
* Reset password

## Category Management

* Create category
* View category list
* Update category
* Delete category

## Product Management

* Create product
* View product list
* View product details
* Update product
* Delete product
* Product pagination
* Category–Product relationship

## OTP

OTP is used for:

* Account registration verification
* Forgot password
* Password reset

OTP information is persisted through the `OtpToken` JPA entity.

---

# Project Structure

Important backend files:

```text
src/main/java/com/hcmute/jpa

config/
└── JpaConfig.java

controller/
├── CategoryController.java
├── ForgotPasswordController.java
├── HomeController.java
├── LoginController.java
├── LogoutController.java
├── ProductController.java
├── RegisterController.java
├── ResetPasswordController.java
└── VerifyOtpController.java

dao/
├── CategoryDao.java
├── ICategoryDao.java
├── IOtpTokenDao.java
├── IProductDao.java
├── IUserDao.java
├── OtpTokenDao.java
├── ProductDao.java
└── UserDao.java

entity/
├── Category.java
├── OtpPurpose.java
├── OtpToken.java
├── Product.java
└── User.java

filter/
└── AuthenticationFilter.java

service/
├── CategoryServiceImpl.java
├── EmailServiceImpl.java
├── ICategoryService.java
├── IEmailService.java
├── IOtpService.java
├── IProductService.java
├── IUserService.java
├── OtpServiceImpl.java
├── ProductServiceImpl.java
└── UserServiceImpl.java
```

Frontend:

```text
src/main/webapp
├── assets/
│   ├── css/
│   └── js/
│
├── views/
│   ├── category-add.jsp
│   ├── category-edit.jsp
│   ├── category-list.jsp
│   ├── error-404.jsp
│   ├── forgot-password.jsp
│   ├── home.jsp
│   ├── login.jsp
│   ├── product-add.jsp
│   ├── product-detail.jsp
│   ├── product-edit.jsp
│   ├── product-list.jsp
│   ├── register.jsp
│   ├── reset-password.jsp
│   └── verify-otp.jsp
│
└── WEB-INF/
    └── web.xml
```

---

# JPA Entities

The application uses the following main JPA entities:

* `Category`
* `Product`
* `User`
* `OtpToken`

Common JPA operations used in the project include:

* `persist()` for INSERT
* `find()` for retrieving entities by ID
* `merge()` for UPDATE
* `remove()` for DELETE
* JPQL for querying entities

---

# Database Setup

The application uses Microsoft SQL Server.

Default configuration:

```text
Server   : localhost
Port     : 1433
Database : jakartaJPA
Username : sa
```

Database credentials are not stored in the repository.

## Step 1 — Create the Database

Open:

```text
database.sql
```

in SQL Server Management Studio and execute it.

Run this script once against a new or empty SQL Server database context. It creates the `jakartaJPA` database if it does not already exist, creates the `categories`, `users`, `products`, and `otp_tokens` tables, and seeds one category plus ten sample products.

```sql
IF DB_ID(N'jakartaJPA') IS NULL
BEGIN
    CREATE DATABASE jakartaJPA;
END
GO

USE jakartaJPA;
GO
```

The seeded product image names match the files committed under `src/main/webapp/uploads`. The script is intended for initial setup; rerunning it against an already initialized database will fail when the tables already exist.

---

# Hibernate Schema Management

The JPA configuration contains:

```xml
<property name="hibernate.hbm2ddl.auto"
          value="update"/>
```

Hibernate therefore creates or updates the application schema based on the JPA entities.

The schema includes tables corresponding to:

```text
Category
Product
User
OtpToken
```

---

# Database Connection

Database configuration is handled by:

```text
src/main/java/com/hcmute/jpa/config/JpaConfig.java
```

Default JDBC URL:

```text
jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true
```

Default username:

```text
sa
```

The database password must be provided externally.

## Windows PowerShell

Before running the application:

```powershell
$env:DB_PASSWORD="your-sql-server-password"
```

If the SQL Server username is different:

```powershell
$env:DB_USER="your-sql-server-user"
```

If SQL Server uses another server, instance, port, or database:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true"
```

Example:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true"
$env:DB_USER="sa"
$env:DB_PASSWORD="your-sql-server-password"
```

Do not commit real database passwords into:

```text
persistence.xml
JpaConfig.java
README.md
```

---

# Email / SMTP Configuration

The application uses Jakarta Mail to send OTP emails for:

* Account registration verification
* Forgot password
* Password reset

SMTP credentials are not stored in the repository.

The application reads:

| Variable        | Description                |
| --------------- | -------------------------- |
| `SMTP_HOST`     | SMTP server hostname       |
| `SMTP_PORT`     | SMTP server port           |
| `SMTP_USERNAME` | SMTP username              |
| `SMTP_PASSWORD` | SMTP password              |
| `SMTP_AUTH`     | Enable SMTP authentication |
| `SMTP_STARTTLS` | Enable STARTTLS            |

If SMTP configuration is not provided, the application defaults to:

```text
SMTP_HOST = localhost
SMTP_PORT = 25
```

This only works when a local SMTP server is available.

Example PowerShell configuration:

```powershell
$env:SMTP_HOST="smtp.example.com"
$env:SMTP_PORT="587"
$env:SMTP_USERNAME="your-email@example.com"
$env:SMTP_PASSWORD="your-smtp-password"
$env:SMTP_AUTH="true"
$env:SMTP_STARTTLS="true"
```

OTP email delivery requires a reachable and correctly configured SMTP server.

The application generates 6-digit OTP codes for registration verification and password reset flows.

---

# Image and Upload Handling

The sample product images are stored in:

```text
src/main/webapp/uploads
```

The database stores each image filename, and the application serves local images from the deployed `/uploads` path. Product forms accept `.jpg`, `.jpeg`, `.png`, and `.webp` files up to 5 MB per file (6 MB per request). New uploads are renamed to unique filenames and written to the deployed web application's writable `uploads` directory; configure persistent storage separately if uploaded files must survive a redeploy. The seeded images are packaged into the WAR automatically.

---

# Required Software

To run the project, install:

* JDK 21
* Microsoft SQL Server
* SQL Server Management Studio
* Apache Tomcat 10.1

For development, IntelliJ IDEA can be used with Tomcat 10.1.

A separate Maven installation is not required because Maven Wrapper is included.

SQL Server must:

* be running
* allow SQL Server Authentication
* accept the configured SQL Server account
* expose the configured TCP port

The default configuration uses:

```text
localhost:1433
```

---

# Build

> [!WARNING]
> Before running `.\mvnw.cmd clean package` or `.\mvnw.cmd clean test`, configure the database password and ensure SQL Server/database are running:
>
> ```powershell
> $env:DB_PASSWORD="your-sql-server-password"
> ```

On Windows:

```powershell
.\mvnw.cmd clean package
```

Generated WAR:

```text
target/JPAExercise.war
```

To build without running tests:

```powershell
.\mvnw.cmd package -DskipTests
```

---

# Run with IntelliJ IDEA and Tomcat

This is the recommended development setup.

## 1. Clone the repository

```powershell
git clone https://github.com/pminhquan/JPAExercise-next.git
cd JPAExercise-next
```

## 2. Open the project

Open the project folder in IntelliJ IDEA.

Allow IntelliJ to import the Maven project from:

```text
pom.xml
```

Configure the project SDK as:

```text
Java 21
```

## 3. Prepare SQL Server

Start Microsoft SQL Server.

Execute:

```text
database.sql
```

using SQL Server Management Studio.

## 4. Configure database credentials

Set the required environment variables before starting the application.

Example:

```powershell
$env:DB_USER="sa"
$env:DB_PASSWORD="your-sql-server-password"
```

If necessary:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true"
```

The same variables must be available to the Tomcat process launched from IntelliJ.

## 5. Configure Tomcat

Use:

```text
Apache Tomcat 10.1
```

Configure IntelliJ / SmartTomcat to deploy the application.

The application context is:

```text
/JPAExercise
```

## 6. Start the application

After Tomcat starts, open:

```text
http://localhost:8080/JPAExercise/
```

---

# Alternative: Deploy WAR Manually

The project does not require IntelliJ to run.

Build the WAR:

```powershell
.\mvnw.cmd clean package
```

The generated file is:

```text
target/JPAExercise.war
```

Copy it into:

```text
<TOMCAT_HOME>/webapps/
```

Start Tomcat on Windows:

```powershell
<TOMCAT_HOME>\bin\startup.bat
```

Then open:

```text
http://localhost:8080/JPAExercise/
```

---

# Testing

JUnit tests are located in:

```text
src/test/java/com/hcmute/jpa
```

The test suite covers:

* JPA integration
* DAO behavior
* Category CRUD
* Category–Product relationships
* Product services
* Product controllers
* Authentication
* Login and logout
* User services
* Registration
* OTP verification
* Forgot password
* Email service behavior
* Security-related validation

Important test classes include:

```text
AuditSpecificSecurityTests
AuthenticationProductIntegrationTest
CategoryDaoTest
CategoryProductTest
EmailServiceTest
ForgotPasswordFlowTest
HomeControllerTest
JpaTest
LoginAndLogoutFlowTest
OtpServiceTest
ProductControllerTest
ProductServiceTest
RegisterAndVerifyFlowTest
UserServiceTest
```

Run tests:

```powershell
.\mvnw.cmd clean test
```

Database-dependent tests require a working SQL Server connection.

At minimum:

```powershell
$env:DB_PASSWORD="your-sql-server-password"
```

must be configured.

---

# Category URLs

Typical category endpoints:

```text
GET  /JPAExercise/categories
GET  /JPAExercise/categories?action=add
POST /JPAExercise/categories?action=insert
GET  /JPAExercise/categories?action=edit&id={id}
POST /JPAExercise/categories?action=update
POST /JPAExercise/categories?action=delete&id={id}
```

---

# Authentication Flow

Registration:

```text
Register
   ↓
Create User / OTP
   ↓
OTP Verification
   ↓
Account Activation
   ↓
Login
```

Forgot password:

```text
Forgot Password
      ↓
OTP Verification
      ↓
Reset Password
      ↓
Login
```

Protected access is handled by:

```text
AuthenticationFilter
```

---

# Notes

* The project uses `jakarta.*` instead of `javax.*`.
* Tomcat 10.1 is used with Jakarta Servlet.
* Maven Wrapper is included.
* SQL Server Authentication is used.
* Database credentials are not stored in the repository.
* SMTP credentials are not stored in the repository.
* `database.sql` creates the database, tables, and sample product data.
* Hibernate manages the application tables through `hibernate.hbm2ddl.auto=update`.
* Integration tests require a valid SQL Server connection.
* Category and Product integration tests clean up records created during testing.
* If the local SQL Server configuration differs from the defaults, configure `DB_URL`, `DB_USER`, and `DB_PASSWORD` externally.
