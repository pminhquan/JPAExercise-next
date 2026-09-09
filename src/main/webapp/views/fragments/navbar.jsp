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

    <c:when test="${currentServletPath == '/admin/dashboard'
                    or currentServletPath == '/WEB-INF/views/admin-dashboard.jsp'
                    or fn:endsWith(currentRequestURI, '/admin/dashboard')}">
        <c:set var="activeNav" value="dashboard" />
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

                    <c:if test="${sessionScope.authenticatedUserRole == 'ADMIN'}">

                        <li class="nav-item site-nav__divider d-none d-lg-flex" aria-hidden="true"></li>

                        <li class="nav-item">

                            <a class="nav-link ${activeNav == 'dashboard' ? 'active' : ''}"
                               href="${pageContext.request.contextPath}/admin/dashboard"
                               ${activeNav == 'dashboard' ? 'aria-current="page"' : ''}>

                                Dashboard

                            </a>

                        </li>

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

                            <c:set var="account" value="${not empty sessionScope.account ? sessionScope.account : account}" />
                            <c:set var="displayUsername" value="${not empty account.username ? account.username : 'User'}" />
                            <c:set var="userInitial" value="${not empty displayUsername ? fn:toUpperCase(fn:substring(displayUsername, 0, 1)) : 'U'}" />
                            <c:set var="userRole" value="${not empty sessionScope.authenticatedUserRole ? sessionScope.authenticatedUserRole : account.role}" />
                            <c:choose>
                                <c:when test="${not empty userRole}">
                                    <c:set var="userMenuLabel" value="User account menu for ${displayUsername} (${userRole})" />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="userMenuLabel" value="User account menu for ${displayUsername}" />
                                </c:otherwise>
                            </c:choose>

                            <div class="dropdown site-nav__user-dropdown">
                                <button class="btn btn-ghost btn-sm dropdown-toggle site-nav__user-toggle ${activeNav == 'profile' ? 'active' : ''}"
                                        type="button"
                                        id="navbarUserDropdown"
                                        data-bs-toggle="dropdown"
                                        aria-expanded="false"
                                        aria-label="${fn:escapeXml(userMenuLabel)}">
                                    <span class="site-nav__avatar" aria-hidden="true">
                                        <c:choose>
                                            <c:when test="${not empty account.images}">
                                                <c:choose>
                                                    <c:when test="${fn:startsWith(account.images, 'http://') or fn:startsWith(account.images, 'https://')}">
                                                        <c:set var="avatarImgSrc" value="${account.images}" />
                                                    </c:when>
                                                    <c:when test="${fn:startsWith(account.images, '/')}">
                                                        <c:set var="avatarImgSrc" value="${pageContext.request.contextPath}${account.images}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set var="avatarImgSrc" value="${pageContext.request.contextPath}/uploads/${account.images}" />
                                                    </c:otherwise>
                                                </c:choose>
                                                <img src="${fn:escapeXml(avatarImgSrc)}"
                                                     alt="${fn:escapeXml(displayUsername)}"
                                                     class="site-nav__avatar-img"
                                                     onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
                                                <span class="site-nav__avatar-fallback" style="display:none;">
                                                    <c:out value="${userInitial}" />
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="site-nav__avatar-fallback">
                                                    <c:out value="${userInitial}" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span class="site-nav__username">
                                        <c:out value="${displayUsername}" />
                                    </span>
                                    <c:if test="${not empty userRole}">
                                        <span class="badge ${userRole == 'ADMIN' ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-secondary'} site-nav__role-badge">
                                            <c:out value="${userRole}" />
                                        </span>
                                    </c:if>
                                </button>
                                <ul class="dropdown-menu dropdown-menu-end shadow-sm" aria-labelledby="navbarUserDropdown">
                                    <li>
                                        <a class="dropdown-item ${activeNav == 'profile' ? 'active' : ''}"
                                           href="${pageContext.request.contextPath}/profile"
                                           ${activeNav == 'profile' ? 'aria-current="page"' : ''}>
                                            Profile
                                        </a>
                                    </li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li>
                                        <a class="dropdown-item"
                                           href="${pageContext.request.contextPath}/logout">
                                            Logout
                                        </a>
                                    </li>
                                </ul>
                            </div>

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