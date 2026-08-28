<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
</head>
<body>
    <h2>Login</h2>
    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
        <p style="color: red;"><%= error %></p>
    <%
        }
    %>
    <form action="${pageContext.request.contextPath}/login" method="post">
        <div>
            <label>Username or Email:</label>
            <input type="text" name="identifier" required/>
        </div>
        <div>
            <label>Password:</label>
            <input type="password" name="password" required/>
        </div>
        <button type="submit">Login</button>
    </form>
</body>
</html>
