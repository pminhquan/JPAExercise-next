<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Home - Newest Products</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<%@ include file="fragments/navbar.jsp" %>

<main class="container py-5">

    <div class="page-header">

        <div class="page-header__text">
            <h1 class="page-header__title">Newest Products</h1>

            <p class="page-header__subtitle">
                <c:set var="count" value="${fn:length(newestProducts)}" />
                <c:choose>
                    <c:when test="${count == 0}">
                        Showing 0 products
                    </c:when>
                    <c:when test="${count == 1}">
                        Showing 1 product
                    </c:when>
                    <c:when test="${count < 10}">
                        Showing <c:out value="${count}"/> products
                    </c:when>
                    <c:otherwise>
                        Showing exactly 10 products
                    </c:otherwise>
                </c:choose>
            </p>
        </div>
    </div>

    <c:choose>

        <c:when test="${empty newestProducts}">

            <div class="card shadow-sm">
                <div class="card-body">

                    <div class="empty-state">
                        <p class="empty-state__title">
                            No products found.
                        </p>
                        <p class="empty-state__text">
                            Newest products will appear here once they are added.
                        </p>
                    </div>

                </div>
            </div>

        </c:when>

        <c:otherwise>

            <div class="product-grid">

                <c:forEach var="product" items="${newestProducts}">

                    <div class="product-card">

                        <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=home"
                           class="product-card__media"
                           aria-label="View product details">

                            <c:choose>

                                <c:when test="${not empty product.images}">
                                    <c:choose>

                                        <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                            <img src="${fn:escapeXml(product.images)}"
                                                 alt="${fn:escapeXml(product.productname)}"
                                                 onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                            <span class="text-empty" style="display:none;">No image</span>
                                        </c:when>

                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                 alt="${fn:escapeXml(product.productname)}"
                                                 onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                            <span class="text-empty" style="display:none;">No image</span>
                                        </c:otherwise>

                                    </c:choose>
                                </c:when>

                                <c:otherwise>
                                    <span class="text-empty">
                                        No image
                                    </span>
                                </c:otherwise>

                            </c:choose>

                        </a>

                        <div class="product-card__body">

                            <span class="product-card__category">
                                <c:out value="${product.category.categoryname}"/>
                            </span>

                            <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=home"
                               class="product-card__name">
                                <c:out value="${product.productname}"/>
                            </a>

                            <c:if test="${not empty product.description}">
                                <span class="product-card__desc">
                                    <c:out value="${product.description}"/>
                                </span>
                            </c:if>

                            <div class="product-card__footer">

                                <span class="product-card__price">
                                    <fmt:formatNumber value="${product.price}" pattern="#,##0"/> ₫
                                </span>

                                <c:choose>

                                    <c:when test="${product.status == 1}">
                                        <span class="badge bg-success badge-status">
                                            Active
                                        </span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="badge bg-secondary badge-status">
                                            Inactive
                                        </span>
                                    </c:otherwise>

                                </c:choose>

                            </div>

                        </div>

                    </div>

                </c:forEach>

            </div>

        </c:otherwise>

    </c:choose>

</main>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
