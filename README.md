# JPAExercise

Category CRUD web application using Jakarta Servlet, JSP, JPA/Hibernate and Microsoft SQL Server.

## Technologies

- Java 21
- Maven Wrapper (included)
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

The database password is not stored anywhere in the repository. `persistence.xml`
contains no credentials. The password is supplied externally at runtime and the
application and the JPA tests fail fast with a configuration error when it is
missing. Provide it either as:

- the `DB_PASSWORD` environment variable (for example `DB_PASSWORD=<your-db-password>`), or
- the `jakarta.persistence.jdbc.password` Java system property

Before running the application, open `database.sql` in SQL Server Management
Studio and execute it. The script creates `jakartaJPA` only when it does not
already exist.

If SQL Server uses different connection information, supply `DB_URL` and
`DB_USER` (or the `jakarta.persistence.jdbc.url` / `jakarta.persistence.jdbc.user`
system properties) in the same way. SQL Server Authentication is used. Do not
put credentials into `persistence.xml` or any source file.

Hibernate uses:

hibernate.hbm2ddl.auto = update

Therefore Hibernate automatically creates or updates the categories table based on the Category entity.

Default JDBC URL:

jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true

## Build

On Windows, use the included Maven Wrapper (no separate Maven installation is
required):

```powershell
.\mvnw.cmd clean test
```

Build WAR:

```powershell
.\mvnw.cmd clean package
```

If Maven is installed globally, `mvn clean test` and `mvn clean package` are
equivalent.

Generated WAR:

target/JPAExercise.war

## Run

Application server:

Apache Tomcat 10.1

Deploy artifact:

Copy `target/JPAExercise.war` to `<TOMCAT_HOME>/webapps/`, then start Tomcat.
Tomcat deploys the WAR with context path `/JPAExercise`.

On Windows, start Tomcat with:

```powershell
<TOMCAT_HOME>\bin\startup.bat
```

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

```powershell
.\mvnw.cmd clean test
```

## Notes

- The project uses Jakarta packages instead of javax packages.
- Tomcat 10.1 is used with Jakarta Servlet 6.0.
- SQL Server must be running and TCP port 1433 must be accessible.
- If SQL Server configuration differs, supply `DB_URL`, `DB_USER`, and `DB_PASSWORD` externally; credentials never go into `persistence.xml` or the source tree.
- Hibernate manages the categories table automatically.
