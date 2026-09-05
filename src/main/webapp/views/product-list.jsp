<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageContext.request.servletPath == '/products' ? 'Product Management' : 'Products'}</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<c:set var="managementView" value="${pageContext.request.servletPath == '/products'}"/>
<c:set var="listPath" value="${managementView ? '/products' : '/product'}"/>

<main class="container py-5">

    <div class="page-header">

        <div class="page-header__text">
            <h1 class="page-header__title">
                <c:choose>
                    <c:when test="${managementView}">Product Management</c:when>
                    <c:otherwise>Products</c:otherwise>
                </c:choose>
            </h1>
            <p class="page-header__subtitle">
                <c:choose>
                    <c:when test="${totalProducts == 1}">
                        1 product in total
                    </c:when>
                    <c:otherwise>
                        ${totalProducts} products in total
                    </c:otherwise>
                </c:choose>
            </p>
        </div>

        <div class="page-header__actions">
            <c:if test="${managementView}">
                <a href="${pageContext.request.contextPath}/categories"
                   class="btn btn-ghost">
                    Manage Categories
                </a>
                <a href="${pageContext.request.contextPath}/products/add"
                   class="btn btn-primary">
                    + Add Product
                </a>
            </c:if>
            <c:if test="${not empty sessionScope.authenticatedUserId}">
                <a href="${pageContext.request.contextPath}/logout"
                   class="btn btn-ghost">
                    Logout
                </a>
            </c:if>
        </div>

    </div>

    <c:if test="${param.message == 'add_success'}">
        <div class="alert alert-success" role="status">
            Product added successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'update_success'}">
        <div class="alert alert-success" role="status">
            Product updated successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'delete_success'}">
        <div class="alert alert-success" role="status">
            Product deleted successfully.
        </div>
    </c:if>

    <c:if test="${param.error == 'invalid_id'}">
        <div class="alert alert-danger" role="alert">
            Invalid product ID provided.
        </div>
    </c:if>

    <div class="card shadow-sm card--flush">

        <div class="card-body table-wrap">

            <table class="table table-hover table--data align-middle">

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Product Name</th>
                    <th>Description</th>
                    <th>Price</th>
                    <th>Image</th>
                    <th>Category</th>
                    <th>Status</th>
                    <c:if test="${managementView}">
                        <th class="col-actions">Action</th>
                    </c:if>
                </tr>
                </thead>

                <tbody>

                <c:forEach var="product" items="${products}">
                    <tr>

                        <td>
                            <span class="cell-id">
                                <c:out value="${product.productid}"/>
                            </span>
                        </td>

                        <td>
                            <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}"
                               class="cell-strong">
                                <c:out value="${product.productname}"/>
                            </a>
                        </td>

                        <td>
                            <c:choose>

                                <c:when test="${not empty product.description}">
                                    <span class="cell-truncate"
                                          tabindex="0"
                                          title="${fn:escapeXml(product.description)}">
                                        <c:out value="${product.description}"/>
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="text-empty">
                                        No description
                                    </span>
                                </c:otherwise>

                            </c:choose>
                        </td>

                        <td>
                            <span class="cell-price">
                                <fmt:formatNumber value="${product.price}" pattern="#,##0"/> ₫
                            </span>
                        </td>

                        <td>
                            <c:choose>

                                <c:when test="${not empty product.images}">
                                    <c:choose>

                                        <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                            <img src="${fn:escapeXml(product.images)}"
                                                 alt="${fn:escapeXml(product.productname)}"
                                                 class="thumb"
                                                 onerror="this.outerHTML='<span class=&quot;text-empty&quot;>No image</span>'"/>
                                        </c:when>

                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                 alt="${fn:escapeXml(product.productname)}"
                                                 class="thumb"
                                                 onerror="this.outerHTML='<span class=&quot;text-empty&quot;>No image</span>'"/>
                                        </c:otherwise>

                                    </c:choose>
                                </c:when>

                                <c:otherwise>
                                    <span class="text-empty">
                                        No image
                                    </span>
                                </c:otherwise>

                            </c:choose>
                        </td>

                        <td>
                            <c:out value="${product.category.categoryname}"/>
                        </td>

                        <td>
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
                        </td>

                        <c:if test="${managementView}">
                            <td>
                                <div class="table-actions">
                                    <a href="${pageContext.request.contextPath}/products/edit?id=${fn:escapeXml(product.productid)}"
                                       class="btn btn-secondary btn-sm">
                                        Edit
                                    </a>
                                    <form action="${pageContext.request.contextPath}/products/delete"
                                          method="post"
                                          class="form-inline"
                                          onsubmit="return confirm('Are you sure you want to delete this product?');">
                                        <input type="hidden" name="id" value="${fn:escapeXml(product.productid)}"/>
                                        <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                    </form>
                                </div>
                            </td>
                        </c:if>

                    </tr>
                </c:forEach>

                <c:if test="${empty products}">
                    <tr>
                        <td colspan="${managementView ? 8 : 7}">

                            <div class="empty-state">
                                <p class="empty-state__title">
                                    No products found.
                                </p>
                                <p class="empty-state__text">
                                    Add your first product to get started.
                                </p>
                            </div>

                        </td>
                    </tr>
                </c:if>

                </tbody>

            </table>

        </div>

        <c:if test="${totalPages > 1}">

            <div class="pagination-bar">

                <span class="pagination-info">
                    Page ${currentPage} of ${totalPages}
                </span>

                <nav aria-label="Product page navigation">
                    <ul class="pagination justify-content-center mb-0">
                        <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                            <c:choose>
                                <c:when test="${currentPage == 1}">
                                    <span class="page-link" aria-disabled="true">Previous</span>
                                </c:when>
                                <c:otherwise>
                                    <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${currentPage - 1}">Previous</a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <li class="page-item ${currentPage == i ? 'active' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${i}"${currentPage == i ? ' aria-current="page"' : ''}>${i}</a>
                            </li>
                        </c:forEach>
                        <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                            <c:choose>
                                <c:when test="${currentPage == totalPages}">
                                    <span class="page-link" aria-disabled="true">Next</span>
                                </c:when>
                                <c:otherwise>
                                    <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${currentPage + 1}">Next</a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </ul>
                </nav>

            </div>

        </c:if>

    </div>

</main>

</body>
</html>
