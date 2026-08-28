<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Forgot Password</title>
</head>
<body>
    <h2>Forgot Password</h2>
    <%
        String error = (String) request.getAttribute("error");
        String message = (String) request.getAttribute("message");
        if (error != null) {
    %>
        <p style="color: red;"><%= error %></p>
    <%
        }
        if (message != null) {
    %>
        <p style="color: green;"><%= message %></p>
    <%
        }
    %>

    <%
        String pendingEmail = (String) session.getAttribute("pendingResetEmail");
        if (pendingEmail == null) {
    %>
        <form action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="action" value="request"/>
            <div>
                <label>Email Address:</label>
                <input type="email" name="email" required/>
            </div>
            <button type="submit">Send Reset Code</button>
        </form>
    <%
        } else {
    %>
        <p>A 6-digit verification code has been sent. It expires in 10 minutes.</p>
        <form action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="action" value="verify"/>
            <div>
                <label>Verification Code:</label>
                <input type="text" name="otp" maxLength="6" required/>
            </div>
            <button type="submit">Verify Code</button>
        </form>
        <p><a href="${pageContext.request.contextPath}/forgot-password?action=cancel">Cancel and try again</a></p>
    <%
        }
    %>
</body>
</html>
