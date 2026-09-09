<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" scope="page"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Dashboard - JPAExercise</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>

<jsp:include page="/views/fragments/navbar.jsp" />

<main class="container py-4 py-md-5">

    <!-- Page Header & Quick Actions -->
    <div class="page-header d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div class="page-header__text">
            <h1 class="page-header__title h3 fw-bold mb-1">Admin Dashboard</h1>
            <p class="page-header__subtitle text-secondary mb-0">Overview of system users, product catalog, and categories.</p>
        </div>
        <div class="page-header__actions d-flex flex-wrap gap-2">
            <a href="${pageContext.request.contextPath}/products/add" class="btn btn-primary d-inline-flex align-items-center gap-1">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <line x1="12" y1="5" x2="12" y2="19"></line>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                <span>Add Product</span>
            </a>
            <a href="${pageContext.request.contextPath}/categories?action=add" class="btn btn-outline-primary d-inline-flex align-items-center gap-1">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <line x1="12" y1="5" x2="12" y2="19"></line>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                <span>Add Category</span>
            </a>
        </div>
    </div>

    <!-- Summary Metrics Cards -->
    <div class="row g-3 g-md-4 mb-4 mb-md-5">
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100 border-0 card--metric">
                <div class="card-body p-4 d-flex flex-column justify-content-between">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <span class="text-uppercase small fw-semibold text-secondary">Total Users</span>
                        <div class="metric-icon rounded-3 p-2 bg-primary-subtle text-primary" aria-hidden="true">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                <circle cx="9" cy="7" r="4"></circle>
                                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                            </svg>
                        </div>
                    </div>
                    <div>
                        <div class="metric-value h2 fw-bold text-dark mb-1">
                            <c:out value="${totalUsers}"/>
                        </div>
                        <p class="small text-muted mb-0">Registered system accounts</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100 border-0 card--metric">
                <div class="card-body p-4 d-flex flex-column justify-content-between">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <span class="text-uppercase small fw-semibold text-secondary">Total Products</span>
                        <div class="metric-icon rounded-3 p-2 bg-success-subtle text-success" aria-hidden="true">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                                <line x1="3" y1="6" x2="21" y2="6"></line>
                                <path d="M16 10a4 4 0 0 1-8 0"></path>
                            </svg>
                        </div>
                    </div>
                    <div>
                        <div class="metric-value h2 fw-bold text-dark mb-1">
                            <c:out value="${totalProducts}"/>
                        </div>
                        <p class="small text-muted mb-0">Active catalog offerings</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100 border-0 card--metric">
                <div class="card-body p-4 d-flex flex-column justify-content-between">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <span class="text-uppercase small fw-semibold text-secondary">Total Categories</span>
                        <div class="metric-icon rounded-3 p-2 bg-info-subtle text-info" aria-hidden="true">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
                            </svg>
                        </div>
                    </div>
                    <div>
                        <div class="metric-value h2 fw-bold text-dark mb-1">
                            <c:out value="${totalCategories}"/>
                        </div>
                        <p class="small text-muted mb-0">Product classification groups</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Recent Products Table Section -->
    <div class="card shadow-sm border-0 card--flush">
        <div class="card-header bg-white py-3 border-bottom d-flex align-items-center justify-content-between">
            <h2 class="h5 mb-0 fw-bold">Recent Products</h2>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-sm btn-ghost text-primary">
                View all products &rarr;
            </a>
        </div>

        <c:choose>
            <c:when test="${empty recentProducts}">
                <div class="card-body">
                    <div class="empty-state py-5 text-center" role="status" aria-live="polite">
                        <div class="empty-state__icon text-muted mb-2" aria-hidden="true">
                            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                                <line x1="3" y1="6" x2="21" y2="6"></line>
                                <path d="M16 10a4 4 0 0 1-8 0"></path>
                            </svg>
                        </div>
                        <h3 class="empty-state__title h6 mb-1">No recent products found</h3>
                        <p class="empty-state__text text-muted small mb-3">Add products to see them appear on your dashboard.</p>
                        <a href="${pageContext.request.contextPath}/products/add" class="btn btn-primary btn-sm">
                            + Add Product
                        </a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card-body p-0 table-wrap table-responsive">
                    <table class="table table-hover table--data align-middle mb-0">
                        <thead class="table-light">
                        <tr>
                            <th scope="col" style="width: 80px;">Product image</th>
                            <th scope="col">Product name</th>
                            <th scope="col">Category</th>
                            <th scope="col" class="text-end">Price</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="product" items="${recentProducts}">
                            <tr>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty product.images}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                                    <img src="${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="thumb"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-flex';" />
                                                    <div class="thumb align-items-center justify-content-center text-muted" style="display:none;" aria-label="No image available" role="img">
                                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                            <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                            <polyline points="21 15 16 10 5 21"></polyline>
                                                        </svg>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="thumb"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-flex';" />
                                                    <div class="thumb align-items-center justify-content-center text-muted" style="display:none;" aria-label="No image available" role="img">
                                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                            <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                            <polyline points="21 15 16 10 5 21"></polyline>
                                                        </svg>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="thumb d-inline-flex align-items-center justify-content-center text-muted" aria-label="No image available" role="img">
                                                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                    <polyline points="21 15 16 10 5 21"></polyline>
                                                </svg>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=products"
                                       class="cell-strong fw-semibold text-decoration-none">
                                        <c:out value="${product.productname}" />
                                    </a>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty product.category and not empty product.category.categoryname}">
                                            <span class="cell-category">
                                                <c:out value="${product.category.categoryname}" />
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-empty text-muted fst-italic">
                                                Uncategorized
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <span class="cell-price">
                                        <fmt:formatNumber value="${product.price}" pattern="#,##0"/> <span class="currency-symbol" aria-label="Vietnamese Dong">₫</span>
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</main>

<jsp:include page="/views/fragments/footer.jsp" />

</body>
</html>
