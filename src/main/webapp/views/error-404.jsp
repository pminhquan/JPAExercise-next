<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Page Not Found</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<main class="auth-shell">

    <a class="auth-brand" href="${pageContext.request.contextPath}/home">
        <span class="auth-brand__mark" aria-hidden="true">J</span>
        JPAExercise
    </a>

    <header class="auth-header">
        <h1 class="auth-header__title">Page not found</h1>
        <p class="auth-header__subtitle">
            The page you are looking for does not exist or may have been moved.
        </p>
    </header>

    <div class="auth-actions">
        <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">
            Back to Home
        </a>
    </div>

</main>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
