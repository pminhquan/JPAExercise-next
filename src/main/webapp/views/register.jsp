<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Register Account</title>
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>

<main class="auth-shell">

    <a class="auth-brand" href="${pageContext.request.contextPath}/home">
        <span class="auth-brand__mark" aria-hidden="true">J</span>
        JPAExercise
    </a>

    <header class="auth-header">
        <h1 class="auth-header__title">Register Account</h1>
        <p class="auth-header__subtitle">
            Create your account to get started.
        </p>
    </header>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <form class="auth-form" action="${pageContext.request.contextPath}/register" method="post">

        <div class="form-field">
            <label class="form-label label-required" for="username">
                Username
            </label>
            <input
                    type="text"
                    id="username"
                    name="username"
                    class="form-control"
                    autocomplete="username"
                    required
                    autofocus>
            <span class="form-hint">
                This is the name you will use to sign in.
            </span>
        </div>

        <div class="form-field">
            <label class="form-label label-required" for="email">
                Email
            </label>
            <input
                    type="email"
                    id="email"
                    name="email"
                    class="form-control"
                    autocomplete="email"
                    required>
            <span class="form-hint">
                We will send a verification code to this address.
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
                        autocomplete="new-password"
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

        <div class="form-field">
            <label class="form-label label-required" for="confirmPassword">
                Confirm Password
            </label>
            <div class="password-field" data-password-field>
                <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        class="form-control"
                        autocomplete="new-password"
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
                Register
            </button>
        </div>

    </form>

    <div class="auth-links auth-links--center">
        <span>
            Already have an account?
            <a href="${pageContext.request.contextPath}/login">Login</a>
        </span>
    </div>

</main>

<script src="${pageContext.request.contextPath}/assets/js/password-toggle.js"></script>

</body>
</html>
