<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Verify OTP</title>
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>

<main class="auth-shell">

    <a class="auth-brand" href="${pageContext.request.contextPath}/home">
        <span class="auth-brand__mark" aria-hidden="true">J</span>
        JPAExercise
    </a>

    <header class="auth-header">
        <h1 class="auth-header__title">Verify OTP</h1>
        <c:if test="${empty success}">
            <p class="auth-header__subtitle">
                A verification code has been sent to your email.
                Please enter the 6-digit code below. It expires in 10 minutes.
            </p>
        </c:if>
    </header>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success" role="alert">
            <c:out value="${success}"/>
        </div>
    </c:if>

    <c:if test="${empty success}">

        <form class="auth-form" action="${pageContext.request.contextPath}/verify-otp" method="post">

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
                        <c:when test="${not empty sessionScope.pendingVerifyEmail}">
                            Enter the 6-digit code we sent to
                            <strong><c:out value="${sessionScope.pendingVerifyEmail}"/></strong>.
                        </c:when>
                        <c:otherwise>
                            Enter the 6-digit code from your email.
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary">
                    Verify
                </button>
            </div>

        </form>

    </c:if>

    <c:if test="${not empty success}">
        <div class="auth-links auth-links--center">
            <a href="${pageContext.request.contextPath}/register">Go back to registration</a>
        </div>
    </c:if>

</main>

<script src="${pageContext.request.contextPath}/assets/js/otp.js"></script>

</body>
</html>
