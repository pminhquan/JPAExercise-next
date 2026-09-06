<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Forgot Password</title>
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
        <h1 class="auth-header__title">Forgot Password</h1>
        <c:choose>
            <c:when test="${empty sessionScope.pendingResetEmail}">
                <p class="auth-header__subtitle">
                    Enter your email address to receive a secure 6-digit recovery code.
                </p>
            </c:when>
            <c:otherwise>
                <p class="auth-header__subtitle">
                    Enter the 6-digit verification code sent to your email to continue.
                </p>
            </c:otherwise>
        </c:choose>
    </header>

    <c:if test="${empty sessionScope.pendingResetEmail}">
        <div class="alert alert-info d-flex align-items-start gap-2 mb-4" role="note">
            <div>
                <strong>Password Recovery:</strong> Provide the email registered with your account. If an account exists, a 6-digit reset code valid for 10 minutes will be delivered to your inbox.
            </div>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>
    <c:if test="${not empty message}">
        <div class="alert alert-success" role="status">
            <c:out value="${message}"/>
        </div>
    </c:if>

    <c:if test="${empty sessionScope.pendingResetEmail}">

        <p class="auth-steps">
            <span class="auth-steps__current">Step 1 of 2</span>
            &middot; Request recovery code
        </p>

        <form class="auth-form" action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="action" value="request"/>

            <div class="form-field">
                <label class="form-label label-required" for="email">
                    Email Address
                </label>
                <input
                        type="email"
                        id="email"
                        name="email"
                        class="form-control"
                        value="${fn:escapeXml(param.email)}"
                        placeholder="e.g. john@example.com"
                        autocomplete="email"
                        aria-describedby="email-hint"
                        required
                        autofocus>
                <span class="form-hint" id="email-hint">
                    Enter the email address associated with your JPAExercise account.
                </span>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary">
                    Send Reset Code
                </button>
            </div>

        </form>

        <div class="auth-links">
            <a href="${pageContext.request.contextPath}/login">
                &larr; Remember your password? Login
            </a>
            <span>
                New user?
                <a href="${pageContext.request.contextPath}/register">Create account</a>
            </span>
        </div>

    </c:if>

    <c:if test="${not empty sessionScope.pendingResetEmail}">

        <p class="auth-steps">
            <span class="auth-steps__current">Step 2 of 2</span>
            &middot; Verify reset code
        </p>

        <form class="auth-form" action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="action" value="verify"/>

            <div class="otp-field${not empty error ? ' otp-field--error' : ''}" data-otp>
                <label class="form-label label-required" for="otp">
                    Verification Code
                </label>
                <div class="otp-boxes">
                    <div class="otp-input" aria-hidden="true">
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                    </div>
                    <input
                            type="text"
                            id="otp"
                            name="otp"
                            class="form-control otp-real"
                            maxlength="6"
                            pattern="[0-9]{6}"
                            placeholder="123456"
                            inputmode="numeric"
                            autocomplete="one-time-code"
                            aria-describedby="otp-hint"
                            required
                            autofocus>
                </div>
                <span class="form-hint" id="otp-hint">
                    <c:choose>
                        <c:when test="${not empty sessionScope.pendingResetEmail}">
                            Enter the 6-digit numeric code sent to
                            <strong><c:out value="${sessionScope.pendingResetEmail}"/></strong>. Code expires in 10 minutes.
                        </c:when>
                        <c:otherwise>
                            Enter the 6-digit code from your recovery email.
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary">
                    Verify Code &amp; Continue
                </button>
            </div>

        </form>

        <div class="auth-links">
            <a href="${pageContext.request.contextPath}/forgot-password?action=cancel">
                &larr; Cancel and try different email
            </a>
            <span>
                Return to
                <a href="${pageContext.request.contextPath}/login">Login</a>
            </span>
        </div>

    </c:if>

</main>

<script src="${pageContext.request.contextPath}/assets/js/otp.js"></script>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
