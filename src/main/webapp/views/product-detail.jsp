<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Product Details</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<main class="container py-5">

    <div class="row justify-content-center">

        <div class="col-md-9">

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

                                <a href="${pageContext.request.contextPath}/product"
                                   class="btn btn-secondary">
                                    Back to Products
                                </a>

                            </div>

                        </div>

                    </div>

                </c:when>

                <c:otherwise>

                    <div class="page-header">

                        <div class="page-header__text">
                            <h1 class="page-header__title">Product Details</h1>
                            <p class="page-header__subtitle">
                                Product #<c:out value="${product.productid}"/>
                            </p>
                        </div>

                        <div class="page-header__actions">
                            <a href="${pageContext.request.contextPath}/product"
                               class="btn btn-ghost">
                                &larr; Back to list
                            </a>
                            <c:if test="${not empty sessionScope.authenticatedUserId}">
                                <a href="${pageContext.request.contextPath}/logout"
                                   class="btn btn-ghost">
                                    Logout
                                </a>
                            </c:if>
                        </div>

                    </div>

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
                                                         onerror="this.outerHTML='<div class=&quot;detail-media__empty&quot;><span class=&quot;text-empty&quot;>No image</span></div>'"/>
                                                </c:when>

                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="detail-media__img"
                                                         onerror="this.outerHTML='<div class=&quot;detail-media__empty&quot;><span class=&quot;text-empty&quot;>No image</span></div>'"/>
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

                                    <div class="detail-titlebar">
                                        <h2 class="detail-title">
                                            <c:out value="${product.productname}"/>
                                        </h2>
                                        <span class="badge ${product.status == 1 ? 'bg-success' : 'bg-secondary'} badge-status">
                                            <c:out value="${product.status == 1 ? 'Active' : 'Inactive'}"/>
                                        </span>
                                    </div>

                                    <p class="detail-price">
                                        $<fmt:formatNumber value="${product.price}" pattern="#,##0.00"/>
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

                                </div>

                            </div>

                            <div class="detail-actions">

                                <a href="${pageContext.request.contextPath}/product"
                                   class="btn btn-secondary">
                                    Back to Products
                                </a>

                                <a href="${pageContext.request.contextPath}/products/edit?id=${fn:escapeXml(product.productid)}"
                                   class="btn btn-primary">
                                    Edit Product
                                </a>

                            </div>

                        </div>

                    </div>

                </c:otherwise>

            </c:choose>

        </div>

    </div>

</main>

</body>
</html>
