# JPAExercise

Category CRUD web application using Jakarta Servlet, JSP, JPA/Hibernate and Microsoft SQL Server.

## Technologies

- Java 21
- Maven 3.9+
- Jakarta Servlet 6.0
- JPA / Hibernate 6.6.1.Final
- JSP / JSTL
- Microsoft SQL Server
- Apache Tomcat 10.1

## Project Architecture

The project follows a layered architecture:

Controller -> Service -> DAO -> JPA EntityManager -> Hibernate -> SQL Server

## Project Structure

src/main/java/com/hcmute/jpa
- config/JpaConfig.java
- controller/CategoryController.java
- dao/CategoryDao.java
- dao/ICategoryDao.java
- entity/Category.java
- service/CategoryServiceImpl.java
- service/ICategoryService.java

JPA configuration:

src/main/resources/META-INF/persistence.xml

Views:

src/main/webapp/views/category-list.jsp
src/main/webapp/views/category-add.jsp
src/main/webapp/views/category-edit.jsp

## Features

- Create Category
- View Category list
- Update Category
- Delete Category

JPA operations used:

- persist() for INSERT
- find() for finding an entity by ID
- merge() for UPDATE
- remove() for DELETE
- JPQL / NamedQuery for retrieving Category list

## Database

The project uses Microsoft SQL Server.

Default configuration:

- Server: localhost
- Port: 1433
- Database: jakartaJPA
- Username: sa
- Password: 123

Before running the application, execute database.sql.

If SQL Server on another machine uses different connection information, update:

src/main/resources/META-INF/persistence.xml

Hibernate uses:

hibernate.hbm2ddl.auto = update

Therefore Hibernate automatically creates or updates the categories table based on the Category entity.

Default JDBC URL:

jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true

## Build

Run tests:

mvn clean test

Build WAR:

mvn clean package

Generated WAR:

target/JPAExercise.war

## Run

Application server:

Apache Tomcat 10.1

Deploy artifact:

JPAExercise:war exploded

Application context:

/JPAExercise

Open:

http://localhost:8080/JPAExercise/

or:

http://localhost:8080/JPAExercise/categories

## Category URLs

- GET /JPAExercise/categories
- GET /JPAExercise/categories?action=add
- POST /JPAExercise/categories?action=insert
- GET /JPAExercise/categories?action=edit&id={id}
- POST /JPAExercise/categories?action=update
- GET /JPAExercise/categories?action=delete&id={id}

## Testing

JUnit tests are located at:

src/test/java/com/hcmute/jpa

Current tests:

- JpaTest.java
- CategoryDaoTest.java

Run all tests:

mvn clean test

## Notes

- The project uses Jakarta packages instead of javax packages.
- Tomcat 10.1 is used with Jakarta Servlet 6.0.
- SQL Server must be running and TCP port 1433 must be accessible.
- If SQL Server configuration differs, update persistence.xml.
- Hibernate manages the categories table automatically.
