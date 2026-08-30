<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reset Password</title>
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>

<main class="auth-shell">

    <a class="auth-brand" href="${pageContext.request.contextPath}/home">
        <span class="auth-brand__mark" aria-hidden="true">J</span>
        JPAExercise
    </a>

    <header class="auth-header">
        <h1 class="auth-header__title">Reset Password</h1>
        <p class="auth-header__subtitle">
            Choose a new password for your account.
        </p>
    </header>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <form class="auth-form" action="${pageContext.request.contextPath}/reset-password" method="post">

        <div class="form-field">
            <label class="form-label label-required" for="password">
                New Password
            </label>
            <div class="password-field" data-password-field>
                <input
                        type="password"
                        id="password"
                        name="password"
                        class="form-control"
                        autocomplete="new-password"
                        required
                        autofocus>
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
                Enter the new password for your account.
            </span>
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
            <span class="form-hint">
                Re-enter the new password.
            </span>
        </div>

        <div class="auth-actions">
            <button type="submit" class="btn btn-primary">
                Reset Password
            </button>
        </div>

    </form>

    <div class="auth-links auth-links--center">
        <a href="${pageContext.request.contextPath}/login">Back to Login</a>
    </div>

</main>

<script src="${pageContext.request.contextPath}/assets/js/password-toggle.js"></script>

</body>
</html>
