<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Reset Password</title>
</head>
<body>
    <h2>Reset Password</h2>
    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
        <p style="color: red;"><%= error %></p>
    <%
        }
    %>
    <form action="${pageContext.request.contextPath}/reset-password" method="post">
        <div>
            <label>New Password:</label>
            <input type="password" name="password" required/>
        </div>
        <div>
            <label>Confirm Password:</label>
            <input type="password" name="confirmPassword" required/>
        </div>
        <button type="submit">Reset Password</button>
    </form>
</body>
</html>
