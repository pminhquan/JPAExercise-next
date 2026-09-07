<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:choose><c:when test="${not empty product}">${fn:escapeXml(product.productname)} - Products</c:when><c:otherwise>Product Details</c:otherwise></c:choose></title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<%@ include file="fragments/navbar.jsp" %>

<main class="container py-5">

    <div class="row justify-content-center">

        <div class="col-lg-10 col-xl-9">

            <c:set var="isManagement" value="${param.from == 'manage' or param.from == 'products' or (not empty sessionScope.authenticatedUserId and param.from != 'home' and param.from != 'catalog' and param.from != 'product')}"/>

            <c:choose>
                <c:when test="${param.from == 'home'}">
                    <c:set var="backUrl" value="${pageContext.request.contextPath}/home"/>
                    <c:set var="backLabel" value="Back to Home"/>
                </c:when>
                <c:when test="${isManagement}">
                    <c:set var="backUrl" value="${pageContext.request.contextPath}/products"/>
                    <c:set var="backLabel" value="Back to Management"/>
                </c:when>
                <c:otherwise>
                    <c:set var="backUrl" value="${pageContext.request.contextPath}/product"/>
                    <c:set var="backLabel" value="Back to Products"/>
                </c:otherwise>
            </c:choose>

            <c:choose>

                <c:when test="${not empty error}">

                    <div class="card shadow-sm">

                        <div class="card-body">

                            <div class="empty-state">

                                <h1 class="empty-state__title">
                                    Product unavailable
                                </h1>

                                <p class="empty-state__text" role="alert">
                                    <c:out value="${error}"/>
                                </p>

                            </div>

                            <div class="detail-actions detail-actions--center">

                                <a href="${backUrl}"
                                   class="btn btn-secondary">
                                    <c:out value="${backLabel}"/>
                                </a>

                            </div>

                        </div>

                    </div>

                </c:when>

                <c:otherwise>

                    <c:choose>
                        <c:when test="${isManagement}">
                            <div class="page-header">
                                <div class="page-header__text">
                                    <h1 class="page-header__title">Product Details</h1>
                                    <p class="page-header__subtitle">
                                        Product #<c:out value="${product.productid}"/>
                                    </p>
                                </div>

                                <div class="page-header__actions">
                                    <a href="${backUrl}"
                                       class="btn btn-ghost">
                                        &larr; <c:out value="${backLabel}"/>
                                    </a>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="page-header page-header--detail">
                                <div class="page-header__actions">
                                    <a href="${backUrl}"
                                       class="btn btn-ghost"
                                       aria-label="${fn:escapeXml(backLabel)}">
                                        &larr; <c:out value="${backLabel}"/>
                                    </a>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="card shadow-sm">

                        <div class="card-body">

                            <div class="detail-grid">

                                <div class="detail-media">

                                    <c:choose>

                                        <c:when test="${not empty product.images}">
                                            <c:choose>

                                                <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                                    <img src="${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="detail-media__img"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='flex';"/>
                                                    <div class="detail-media__empty" style="display:none;">
                                                        <span class="text-empty">No image</span>
                                                    </div>
                                                </c:when>

                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="detail-media__img"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='flex';"/>
                                                    <div class="detail-media__empty" style="display:none;">
                                                        <span class="text-empty">No image</span>
                                                    </div>
                                                </c:otherwise>

                                            </c:choose>
                                        </c:when>

                                        <c:otherwise>
                                            <div class="detail-media__empty">
                                                <span class="text-empty">
                                                    No image
                                                </span>
                                            </div>
                                        </c:otherwise>

                                    </c:choose>

                                </div>

                                <div class="detail-body">

                                    <c:choose>
                                        <c:when test="${isManagement}">
                                            <div class="detail-titlebar">
                                                <h2 class="detail-title">
                                                    <c:out value="${product.productname}"/>
                                                </h2>
                                                <span class="badge ${product.status == 1 ? 'bg-success' : 'bg-secondary'} badge-status">
                                                    <c:out value="${product.status == 1 ? 'Active' : 'Inactive'}"/>
                                                </span>
                                            </div>

                                            <p class="detail-price">
                                                <span class="detail-price__amount"><fmt:formatNumber value="${product.price}" pattern="#,##0"/></span>
                                                <span class="detail-price__currency" aria-label="Vietnamese Dong">₫</span>
                                            </p>

                                            <div class="detail-facts">

                                                <div class="detail-fact">
                                                    <span class="detail-fact__label">Category</span>
                                                    <span class="detail-fact__value">
                                                        <c:out value="${product.category.categoryname}"/>
                                                    </span>
                                                </div>

                                                <div class="detail-fact">
                                                    <span class="detail-fact__label">Product ID</span>
                                                    <span class="detail-fact__value cell-id">
                                                        <c:out value="${product.productid}"/>
                                                    </span>
                                                </div>

                                            </div>

                                            <div class="detail-section">
                                                <h3 class="detail-section__title">Description</h3>
                                                <c:choose>
                                                    <c:when test="${not empty product.description}">
                                                        <p class="detail-description">
                                                            <c:out value="${product.description}"/>
                                                        </p>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <p class="detail-description detail-description--empty">
                                                            No description provided.
                                                        </p>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </c:when>

                                        <c:otherwise>
                                            <c:if test="${not empty product.category.categoryname}">
                                                <span class="product-card__category detail-category">
                                                    <c:out value="${product.category.categoryname}"/>
                                                </span>
                                            </c:if>

                                            <h1 class="detail-title">
                                                <c:out value="${product.productname}"/>
                                            </h1>

                                            <p class="detail-price">
                                                <span class="detail-price__amount"><fmt:formatNumber value="${product.price}" pattern="#,##0"/></span>
                                                <span class="detail-price__currency" aria-label="Vietnamese Dong">₫</span>
                                            </p>

                                            <div class="detail-section">
                                                <h2 class="detail-section__title">Description</h2>
                                                <c:choose>
                                                    <c:when test="${not empty product.description}">
                                                        <p class="detail-description">
                                                            <c:out value="${product.description}"/>
                                                        </p>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <p class="detail-description detail-description--empty">
                                                            No description provided.
                                                        </p>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                </div>

                            </div>

                            <div class="detail-actions">

                                <a href="${backUrl}"
                                   class="btn btn-secondary">
                                    <c:out value="${backLabel}"/>
                                </a>

                                <c:choose>
                                    <c:when test="${not empty sessionScope.authenticatedUserId}">
                                        <a href="${pageContext.request.contextPath}/products/edit?id=${fn:escapeXml(product.productid)}"
                                           class="btn btn-primary">
                                            Edit Product
                                        </a>
                                    </c:when>
                                    <c:when test="${param.from == 'home'}">
                                        <a href="${pageContext.request.contextPath}/product"
                                           class="btn btn-primary">
                                            Browse Products
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/home"
                                           class="btn btn-primary">
                                            Home
                                        </a>
                                    </c:otherwise>
                                </c:choose>

                            </div>

                        </div>

                    </div>

                </c:otherwise>

            </c:choose>

        </div>

    </div>

</main>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
