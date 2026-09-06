<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>

<main class="auth-shell">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <a class="auth-brand mb-0" href="${pageContext.request.contextPath}/home">
            <span class="auth-brand__mark" aria-hidden="true">J</span>
            JPAExercise
        </a>
        <a href="${pageContext.request.contextPath}/home" class="btn btn-ghost btn-sm">
            &larr; Back to Home
        </a>
    </div>

    <header class="auth-header">
        <h1 class="auth-header__title">Login</h1>
        <p class="auth-header__subtitle">
            Enter your username or email and password to continue.
        </p>
    </header>

    <c:if test="${not empty param.message}">
        <div class="alert alert-success" role="status">
            <c:out value="${param.message}"/>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post">

        <div class="form-field">
            <label class="form-label label-required" for="identifier">
                Username or Email
            </label>
            <input
                    type="text"
                    id="identifier"
                    name="identifier"
                    class="form-control"
                    value="${fn:escapeXml(param.identifier)}"
                    placeholder="e.g. user1 or user@example.com"
                    autocomplete="username"
                    required
                    autofocus>
            <span class="form-hint">
                Enter your registered username or email address.
            </span>
        </div>

        <div class="form-field">
            <label class="form-label label-required" for="password">
                Password
            </label>
            <div class="password-field" data-password-field>
                <input
                        type="password"
                        id="password"
                        name="password"
                        class="form-control"
                        placeholder="Enter your password"
                        autocomplete="current-password"
                        required>
                <button
                        type="button"
                        class="password-toggle"
                        data-password-toggle
                        hidden
                        aria-pressed="false">
                    Show
                </button>
            </div>
            <span class="form-hint">
                Enter your account password.
            </span>
        </div>

        <div class="form-check my-3 d-flex align-items-center gap-2">
            <input class="form-check-input mt-0"
                   type="checkbox"
                   id="rememberMe"
                   name="rememberMe"
                   value="true">
            <label class="form-check-label small text-secondary" for="rememberMe">
                Remember me on this device
            </label>
        </div>

        <div class="auth-actions">
            <button type="submit" class="btn btn-primary">
                Login
            </button>
        </div>

    </form>

    <div class="auth-links">
        <a href="${pageContext.request.contextPath}/forgot-password">
            Forgot password?
        </a>
        <span>
            New here?
            <a href="${pageContext.request.contextPath}/register">Create account</a>
        </span>
    </div>

</main>

<script src="${pageContext.request.contextPath}/assets/js/password-toggle.js"></script>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
