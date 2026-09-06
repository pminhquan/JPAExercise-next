<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reset Password</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>

<%@ include file="fragments/navbar.jsp" %>

<main class="auth-shell">

    <div class="mb-4">
        <a href="${pageContext.request.contextPath}/home" class="btn btn-ghost btn-sm">
            &larr; Back to Home
        </a>
    </div>

    <header class="auth-header">
        <h1 class="auth-header__title">Reset Password</h1>
        <p class="auth-header__subtitle">
            Create a new, strong password to secure your JPAExercise account.
        </p>
    </header>

    <div class="alert alert-info d-flex align-items-start gap-2 mb-4" role="note">
        <div>
            <c:choose>
                <c:when test="${not empty sessionScope.resetEmail}">
                    <strong>Account Verified:</strong> Setting a new password for <strong><c:out value="${sessionScope.resetEmail}"/></strong>. Once updated, you will be redirected to log in.
                </c:when>
                <c:otherwise>
                    <strong>Account Verified:</strong> Choose your new password below. Once updated, you will be redirected to log in.
                </c:otherwise>
            </c:choose>
        </div>
    </div>

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
                        placeholder="Enter your new password"
                        autocomplete="new-password"
                        aria-describedby="password-hint"
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
            <span class="form-hint" id="password-hint">
                Must be at least 6 characters (recommended: include uppercase, numbers, or symbols).
            </span>
        </div>

        <div class="form-field">
            <label class="form-label label-required" for="confirmPassword">
                Confirm New Password
            </label>
            <div class="password-field" data-password-field>
                <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        class="form-control"
                        placeholder="Re-enter your new password"
                        autocomplete="new-password"
                        aria-describedby="confirm-hint"
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
            <span class="form-hint" id="confirm-hint">
                Ensure this matches the new password entered above.
            </span>
        </div>

        <div class="auth-actions">
            <button type="submit" class="btn btn-primary">
                Update Password
            </button>
        </div>

    </form>

    <div class="auth-links auth-links--center">
        <span>
            Remember your password?
            <a href="${pageContext.request.contextPath}/login">Back to Login</a>
        </span>
    </div>

</main>

<script src="${pageContext.request.contextPath}/assets/js/password-toggle.js"></script>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
