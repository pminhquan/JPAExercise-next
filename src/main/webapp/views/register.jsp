<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Register Account</title>
</head>
<body>
    <h2>Register Account</h2>
    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
        <p style="color: red;"><%= error %></p>
    <%
        }
    %>
    <form action="${pageContext.request.contextPath}/register" method="post">
        <div>
            <label>Username:</label>
            <input type="text" name="username" required/>
        </div>
        <div>
            <label>Email:</label>
            <input type="email" name="email" required/>
        </div>
        <div>
            <label>Password:</label>
            <input type="password" name="password" required/>
        </div>
        <div>
            <label>Confirm Password:</label>
            <input type="password" name="confirmPassword" required/>
        </div>
        <button type="submit">Register</button>
    </form>
</body>
</html>
