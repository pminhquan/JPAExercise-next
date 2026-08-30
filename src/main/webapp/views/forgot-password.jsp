<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Forgot Password</title>
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>

<main class="auth-shell">

    <a class="auth-brand" href="${pageContext.request.contextPath}/home">
        <span class="auth-brand__mark" aria-hidden="true">J</span>
        JPAExercise
    </a>

    <header class="auth-header">
        <h1 class="auth-header__title">Forgot Password</h1>
        <c:choose>
            <c:when test="${empty sessionScope.pendingResetEmail}">
                <p class="auth-header__subtitle">
                    Enter your email address and we will send you a password reset code.
                </p>
            </c:when>
            <c:otherwise>
                <p class="auth-header__subtitle">
                    A 6-digit verification code has been sent. It expires in 10 minutes.
                </p>
            </c:otherwise>
        </c:choose>
    </header>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>
    <c:if test="${not empty message}">
        <div class="alert alert-success" role="alert">
            <c:out value="${message}"/>
        </div>
    </c:if>

    <c:if test="${empty sessionScope.pendingResetEmail}">

        <p class="auth-steps">
            <span class="auth-steps__current">Step 1 of 2</span>
            &middot; Email address
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
                        autocomplete="email"
                        required
                        autofocus>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary">
                    Send Reset Code
                </button>
            </div>

        </form>

    </c:if>

    <c:if test="${not empty sessionScope.pendingResetEmail}">

        <p class="auth-steps">
            <span class="auth-steps__current">Step 2 of 2</span>
            &middot; Verification code
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
                            maxLength="6"
                            inputmode="numeric"
                            autocomplete="one-time-code"
                            aria-describedby="otp-hint"
                            required
                            autofocus>
                </div>
                <span class="form-hint" id="otp-hint">
                    <c:choose>
                        <c:when test="${not empty sessionScope.pendingResetEmail}">
                            Enter the 6-digit code we sent to
                            <strong><c:out value="${sessionScope.pendingResetEmail}"/></strong>.
                        </c:when>
                        <c:otherwise>
                            Enter the 6-digit code from your email.
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary">
                    Verify Code
                </button>
            </div>

        </form>

        <div class="auth-links auth-links--center">
            <a href="${pageContext.request.contextPath}/forgot-password?action=cancel">
                Cancel and try again
            </a>
        </div>

    </c:if>

</main>

<script src="${pageContext.request.contextPath}/assets/js/otp.js"></script>

</body>
</html>
