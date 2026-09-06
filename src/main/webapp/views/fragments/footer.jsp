<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<footer class="site-footer">
    <div class="container">
        <div class="row gy-4 align-items-center">
            <div class="col-12 col-md-5">
                <a class="site-footer__brand" href="${pageContext.request.contextPath}/home" aria-label="JPAExercise Home">
                    <span class="site-brand__mark" aria-hidden="true">J</span>
                    <span class="site-footer__title">JPAExercise</span>
                </a>
                <p class="site-footer__desc">
                    Jakarta Persistence API &bull; Servlet 6.0 &bull; Hibernate ORM 6.6
                </p>
            </div>
            <div class="col-12 col-md-4 text-md-center">
                <div class="site-footer__links">
                    <a href="${pageContext.request.contextPath}/home" class="site-footer__link">Home</a>
                    <span class="site-footer__sep">&bull;</span>
                    <a href="${pageContext.request.contextPath}/product" class="site-footer__link">Catalog</a>
                    <c:choose>
                        <c:when test="${not empty sessionScope.authenticatedUserId}">
                            <span class="site-footer__sep">&bull;</span>
                            <a href="${pageContext.request.contextPath}/products" class="site-footer__link">Products</a>
                            <span class="site-footer__sep">&bull;</span>
                            <a href="${pageContext.request.contextPath}/categories" class="site-footer__link">Categories</a>
                            <span class="site-footer__sep">&bull;</span>
                            <a href="${pageContext.request.contextPath}/logout" class="site-footer__link">Logout</a>
                        </c:when>
                        <c:otherwise>
                            <span class="site-footer__sep">&bull;</span>
                            <a href="${pageContext.request.contextPath}/login" class="site-footer__link">Login</a>
                            <span class="site-footer__sep">&bull;</span>
                            <a href="${pageContext.request.contextPath}/register" class="site-footer__link">Register</a>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="site-footer__team">
                    Developed by <strong>Team HCMUTE</strong> &bull; Advanced Web Programming
                </div>
            </div>
            <div class="col-12 col-md-3 text-md-end">
                <p class="site-footer__copy mb-0">
                    &copy; 2026 JPAExercise. All rights reserved.
                </p>
            </div>
        </div>
    </div>
</footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
