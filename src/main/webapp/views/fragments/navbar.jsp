<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="currentServletPath" value="${pageContext.request.servletPath}" />
<c:set var="currentRequestURI" value="${pageContext.request.requestURI}" />

<c:choose>

    <c:when test="${not empty activeTab}">
        <c:set var="activeNav" value="${activeTab}" />
    </c:when>

    <c:when test="${currentServletPath == '/views/home.jsp'
                    or currentServletPath == '/home'
                    or currentServletPath == '/index.jsp'}">
        <c:set var="activeNav" value="home" />
    </c:when>

    <c:when test="${currentServletPath == '/views/product-list.jsp'}">
        <c:choose>
            <c:when test="${managementView}">
                <c:set var="activeNav" value="products" />
            </c:when>
            <c:otherwise>
                <c:set var="activeNav" value="catalog" />
            </c:otherwise>
        </c:choose>
    </c:when>

    <c:when test="${currentServletPath == '/product'}">
        <c:set var="activeNav" value="catalog" />
    </c:when>

    <c:when test="${currentServletPath == '/products'
                    or currentServletPath == '/products/add'
                    or currentServletPath == '/products/edit'
                    or currentServletPath == '/views/product-add.jsp'
                    or currentServletPath == '/views/product-edit.jsp'}">
        <c:set var="activeNav" value="products" />
    </c:when>

    <c:when test="${currentServletPath == '/categories'
                    or currentServletPath == '/views/category-list.jsp'
                    or currentServletPath == '/views/category-add.jsp'
                    or currentServletPath == '/views/category-edit.jsp'}">
        <c:set var="activeNav" value="categories" />
    </c:when>

    <c:when test="${currentServletPath == '/views/product-detail.jsp'
                    or currentServletPath == '/products/detail'}">

        <c:choose>

            <c:when test="${param.from == 'manage'
                            or param.from == 'products'
                            or (not empty sessionScope.authenticatedUserId
                                and not empty param.from
                                and param.from != 'home'
                                and param.from != 'catalog'
                                and param.from != 'product')}">

                <c:set var="activeNav" value="products" />

            </c:when>

            <c:otherwise>
                <c:set var="activeNav" value="catalog" />
            </c:otherwise>

        </c:choose>

    </c:when>

    <c:when test="${currentServletPath == '/profile'
                    or currentServletPath == '/WEB-INF/views/profile.jsp'
                    or fn:endsWith(currentRequestURI, '/profile')}">
        <c:set var="activeNav" value="profile" />
    </c:when>

    <c:when test="${currentServletPath == '/views/login.jsp'
                    or currentServletPath == '/login'}">
        <c:set var="activeNav" value="login" />
    </c:when>

    <c:when test="${currentServletPath == '/views/register.jsp'
                    or currentServletPath == '/register'}">
        <c:set var="activeNav" value="register" />
    </c:when>

    <c:otherwise>
        <c:set var="activeNav" value="" />
    </c:otherwise>

</c:choose>

<header class="site-header">

    <nav class="site-nav navbar navbar-expand-lg"
         aria-label="Main navigation">

        <div class="container">

            <a class="navbar-brand site-brand"
               href="${pageContext.request.contextPath}/home"
               aria-label="JPAExercise Home">

                <span class="site-brand__mark" aria-hidden="true">J</span>

                <span class="site-brand__text">
                    JPAExercise
                </span>

            </a>

            <button class="navbar-toggler"
                    type="button"
                    data-bs-toggle="collapse"
                    data-bs-target="#mainNavbar"
                    aria-controls="mainNavbar"
                    aria-expanded="false"
                    aria-label="Toggle navigation">

                <span class="navbar-toggler-icon"></span>

            </button>

            <div class="collapse navbar-collapse" id="mainNavbar">

                <ul class="navbar-nav ms-auto mb-2 mb-lg-0 site-nav__links">

                    <li class="nav-item">

                        <a class="nav-link ${activeNav == 'home' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/home"
                           ${activeNav == 'home' ? 'aria-current="page"' : ''}>

                            Home

                        </a>

                    </li>

                    <li class="nav-item">

                        <a class="nav-link ${activeNav == 'catalog' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/product"
                           ${activeNav == 'catalog' ? 'aria-current="page"' : ''}>

                            Browse Products

                        </a>

                    </li>

                    <c:if test="${not empty sessionScope.authenticatedUserId}">

                        <li class="nav-item site-nav__divider d-none d-lg-flex" aria-hidden="true"></li>

                        <li class="nav-item">

                            <a class="nav-link ${activeNav == 'products' ? 'active' : ''}"
                               href="${pageContext.request.contextPath}/products"
                               ${activeNav == 'products' ? 'aria-current="page"' : ''}>

                                Manage Products

                            </a>

                        </li>

                        <li class="nav-item">

                            <a class="nav-link ${activeNav == 'categories' ? 'active' : ''}"
                               href="${pageContext.request.contextPath}/categories"
                               ${activeNav == 'categories' ? 'aria-current="page"' : ''}>

                                Manage Categories

                            </a>

                        </li>

                    </c:if>

                </ul>

                <div class="site-nav__auth d-flex align-items-center gap-2 ms-lg-2">

                    <c:choose>

                        <c:when test="${not empty sessionScope.authenticatedUserId}">

                            <a href="${pageContext.request.contextPath}/profile"
                               class="btn btn-ghost btn-sm ${activeNav == 'profile' ? 'active' : ''}"
                               ${activeNav == 'profile' ? 'aria-current="page"' : ''}>

                                Profile

                            </a>

                            <a href="${pageContext.request.contextPath}/logout"
                               class="btn btn-ghost btn-sm">

                                Logout

                            </a>

                        </c:when>

                        <c:otherwise>

                            <a href="${pageContext.request.contextPath}/login"
                               class="btn btn-ghost btn-sm ${activeNav == 'login' ? 'active' : ''}">

                                Login

                            </a>

                            <a href="${pageContext.request.contextPath}/register"
                               class="btn btn-primary btn-sm ${activeNav == 'register' ? 'active' : ''}">

                                Register

                            </a>

                        </c:otherwise>

                    </c:choose>

                </div>

            </div>

        </div>

    </nav>

</header>