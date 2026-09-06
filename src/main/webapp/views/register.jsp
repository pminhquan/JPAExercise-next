<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Register Account</title>
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
        <h1 class="auth-header__title">Register Account</h1>
        <p class="auth-header__subtitle">
            Create your account to get started. You will verify your email with an OTP code in the next step.
        </p>
    </header>

    <div class="alert alert-info d-flex align-items-start gap-2 mb-4" role="note">
        <div>
            <strong>Email Verification Notice:</strong> A 6-digit one-time code (OTP) will be sent to your email to verify and activate your account.
        </div>
    </div>

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
                    value="${fn:escapeXml(param.username)}"
                    placeholder="e.g. johndoe"
                    autocomplete="username"
                    required
                    autofocus>
            <span class="form-hint">
                Choose a unique username for signing in to your account.
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
                    value="${fn:escapeXml(param.email)}"
                    placeholder="e.g. john@example.com"
                    autocomplete="email"
                    required>
            <span class="form-hint">
                A valid email address where your 6-digit verification code will be sent.
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
                Must be at least 6 characters (recommended: include uppercase, numbers, or symbols).
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
                        placeholder="Re-enter your password"
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
                Ensure this matches the password entered above.
            </span>
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

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
