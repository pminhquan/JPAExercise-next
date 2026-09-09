# JPAExercise-next

A Java Web application built with Jakarta Servlet, JSP, JPA/Hibernate, Maven, and Microsoft SQL Server.

The project demonstrates a layered MVC architecture and includes authentication, OTP-based account flows, category management, product management, pagination, profile management, and database integration using JPA/Hibernate.

---

# Author

- Name: Phạm Minh Quân
- Student ID: 24110311
- Class: 241103A

---

# Repository

https://github.com/pminhquan/JPAExercise-next

---

# Technologies

- Java 21
- Maven Wrapper
- Jakarta Servlet 6.0
- JSP / JSTL
- JPA
- Hibernate ORM 6.6.1.Final
- Microsoft SQL Server
- Jakarta Mail
- Apache Tomcat 10.1
- JUnit 5
- Bootstrap
- SiteMesh Decorator 3

---

# Project Architecture

The project follows MVC 3-tier architecture:

```
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

```
src/main/java/com/hcmute/jpa

├── config
├── controller
├── dao
├── entity
├── filter
└── service
```

---

# Main Features

## Authentication

- User registration
- OTP verification
- Account activation
- Login
- Logout
- Forgot password
- Reset password
- Authentication filter
- Password hashing using BCrypt


## Category Management

- Create category
- View category list
- Update category
- Delete category


## Product Management

- Create product
- View product list
- Product detail
- Update product
- Delete product
- Product pagination
- Category - Product relationship
- Product image upload


## User Profile

- View profile
- Update fullname
- Update phone
- Update avatar image


## OTP

OTP is used for:

- Account registration verification
- Forgot password
- Password reset

OTP data is managed through the `OtpToken` JPA entity.

---

# JPA Entities

Main entities:

```
Category
Product
User
OtpToken
```

Common JPA operations:

- persist() - INSERT
- find() - SELECT
- merge() - UPDATE
- remove() - DELETE
- JPQL queries

---

# Database Setup

Database:

```
Microsoft SQL Server
```

Default configuration:

```
Server:
localhost

Port:
1433

Database:
jakartaJPA

Username:
sa
```

Database password is configured externally and is not stored in repository.

---

## Create Database

Run:

```
database.sql
```

The script creates:

- categories
- users
- products
- otp_tokens

and inserts sample data.

---

# Database Configuration

Environment variables:

```powershell
$env:DB_USER="sa"
$env:DB_PASSWORD="your-password"
```

Optional:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true"
```

---

# Email / SMTP Configuration

OTP email requires SMTP configuration.

Variables:

| Variable | Description |
|-|-|
| SMTP_HOST | SMTP server |
| SMTP_PORT | SMTP port |
| SMTP_USERNAME | Email account |
| SMTP_PASSWORD | Email password |
| SMTP_AUTH | Authentication |
| SMTP_STARTTLS | STARTTLS |

Example:

```powershell
$env:SMTP_HOST="smtp.example.com"
$env:SMTP_PORT="587"
$env:SMTP_USERNAME="email@example.com"
$env:SMTP_PASSWORD="password"
$env:SMTP_AUTH="true"
$env:SMTP_STARTTLS="true"
```

---

# Image Upload

Product images are stored in:

```
src/main/webapp/uploads
```

Supported:

- jpg
- jpeg
- png
- webp

Upload limit:

```
5MB/file
```

Images are renamed before saving to avoid duplicated filenames.

---

# Required Software

Install:

- JDK 21
- Microsoft SQL Server
- SQL Server Management Studio
- Apache Tomcat 10.1

Maven installation is not required because Maven Wrapper is included.

---

# Build Project

Windows:

```powershell
.\mvnw.cmd clean package
```

Generated WAR:

```
target/JPAExercise.war
```

---

# Run With Tomcat

Copy:

```
target/JPAExercise.war
```

to:

```
<TOMCAT_HOME>/webapps/
```

Start Tomcat:

```powershell
<TOMCAT_HOME>\bin\startup.bat
```

Open:

```
http://localhost:8080/JPAExercise/
```

---

# Test Accounts

## Admin Account

Username:

```
test_admin
```

Password:

```
password
```

Permissions:

- Manage Category
- Manage Product
- Access admin functions


## Customer Account

Username:

```
test_customer
```

Password:

```
password
```

Permissions:

- Login
- Browse products
- View product details
- Update profile

---

# Completed Feature Checklist

## Authentication

[x] User registration  
[x] OTP verification  
[x] Login  
[x] Logout  
[x] Forgot password  
[x] Reset password  
[x] BCrypt password hashing  


## Product Management

[x] Product listing  
[x] Product detail  
[x] Product pagination  
[x] Product CRUD  
[x] Category relationship  
[x] Product image upload  


## Category Management

[x] Category listing  
[x] Add category  
[x] Edit category  
[x] Delete category  


## User Profile

[x] View profile  
[x] Update fullname  
[x] Update phone  
[x] Update avatar image  


## UI / Frontend

[x] JSP/JSTL  
[x] Bootstrap interface  
[x] SiteMesh Decorator 3  
[x] Responsive navigation  
[x] Error handling  


---

# Runtime Verification

Environment:

- Java 21
- Apache Tomcat 10.1
- Microsoft SQL Server
- Maven Wrapper


Verified URL:

```
http://localhost:8080/JPAExercise/
```


Verified flows:

[x] Homepage loading  
[x] Product rendering  
[x] Login page  
[x] Register page  
[x] Authentication navigation  
[x] CRUD pages loading  


---

# Testing

JUnit tests:

```
src/test/java/com/hcmute/jpa
```

Covered:

- JPA integration
- DAO behavior
- Category CRUD
- Product service
- Authentication
- Login/logout
- Registration
- OTP verification
- Forgot password
- Email service


Run:

```powershell
.\mvnw.cmd clean test
```

---

# Authentication Flow

Register:

```
Register
   ↓
Create User + OTP
   ↓
OTP Verification
   ↓
Account Activation
   ↓
Login
```


Forgot password:

```
Forgot Password
        ↓
OTP Verification
        ↓
Reset Password
        ↓
Login
```

Protected routes are handled by:

```
AuthenticationFilter
```

---

# Notes

- Uses jakarta.* instead of javax.*
- Tomcat 10.1 with Jakarta Servlet
- Maven Wrapper included
- SQL Server Authentication used
- Database credentials are not committed
- SMTP credentials are not committed
- Hibernate manages schema using hibernate.hbm2ddl.auto=update
- Upload images are stored in application upload directory

---

# Final Status

Project status:

```
BUILD      : PASS
DEPLOY     : PASS
DATABASE   : PASS
AUTH       : PASS
CRUD       : PASS
UI TEST    : PASS
```

Ready for submission.