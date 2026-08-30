<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login</title>
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>

<main class="auth-shell">

    <a class="auth-brand" href="${pageContext.request.contextPath}/home">
        <span class="auth-brand__mark" aria-hidden="true">J</span>
        JPAExercise
    </a>

    <header class="auth-header">
        <h1 class="auth-header__title">Login</h1>
        <p class="auth-header__subtitle">
            Enter your username or email and password to continue.
        </p>
    </header>

    <c:if test="${not empty param.message}">
        <div class="alert alert-success" role="alert">
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
                    autocomplete="username"
                    required
                    autofocus>
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

</body>
</html>
