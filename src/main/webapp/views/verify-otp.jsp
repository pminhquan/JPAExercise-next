<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Verify OTP</title>
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
        <h1 class="auth-header__title">Verify OTP</h1>
        <c:choose>
            <c:when test="${not empty success}">
                <p class="auth-header__subtitle">
                    Account verification completed successfully.
                </p>
            </c:when>
            <c:otherwise>
                <p class="auth-header__subtitle">
                    Enter the 6-digit verification code sent to your email to activate your account.
                </p>
            </c:otherwise>
        </c:choose>
    </header>

    <c:if test="${empty success && not empty sessionScope.pendingVerifyEmail}">
        <div class="alert alert-info d-flex align-items-start gap-2 mb-4" role="note">
            <div>
                <strong>Code Sent:</strong> A 6-digit verification code was sent to <strong><c:out value="${sessionScope.pendingVerifyEmail}"/></strong>. Codes expire after 10 minutes.
            </div>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="alert alert-success" role="status">
            <c:out value="${success}"/>
        </div>
        <div class="auth-actions">
            <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">
                Proceed to Login &rarr;
            </a>
        </div>
        <div class="auth-links auth-links--center">
            <a href="${pageContext.request.contextPath}/home">Return to Home</a>
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
                        <c:when test="${not empty sessionScope.pendingVerifyEmail}">
                            Enter the 6-digit numeric code sent to
                            <strong><c:out value="${sessionScope.pendingVerifyEmail}"/></strong>.
                        </c:when>
                        <c:otherwise>
                            Enter the 6-digit numeric code from your verification email.
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary">
                    Verify &amp; Activate Account
                </button>
            </div>

        </form>

        <div class="auth-links">
            <a href="${pageContext.request.contextPath}/register">
                &larr; Re-register or change email
            </a>
            <span>
                Already activated?
                <a href="${pageContext.request.contextPath}/login">Login</a>
            </span>
        </div>

    </c:if>

</main>

<script src="${pageContext.request.contextPath}/assets/js/otp.js"></script>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
