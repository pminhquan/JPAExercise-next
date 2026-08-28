<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Verify OTP</title>
</head>
<body>
    <h2>Verify OTP</h2>
    <%
        String error = (String) request.getAttribute("error");
        String success = (String) request.getAttribute("success");
        if (error != null) {
    %>
        <p style="color: red;"><%= error %></p>
    <%
        }
        if (success != null) {
    %>
        <p style="color: green;"><%= success %></p>
    <%
        }
    %>

    <% if (success == null) { %>
        <p>A verification code has been sent to your email. Please enter the 6-digit code below. It expires in 10 minutes.</p>
        <form action="${pageContext.request.contextPath}/verify-otp" method="post">
            <div>
                <label>Verification Code:</label>
                <input type="text" name="otp" maxLength="6" required/>
            </div>
            <button type="submit">Verify</button>
        </form>
    <% } else { %>
        <p><a href="${pageContext.request.contextPath}/register">Go back to registration</a></p>
    <% } %>
</body>
</html>
