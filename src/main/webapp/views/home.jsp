<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Home - Newest Products</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Home - Newest Products</h2>
        <div>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-outline-primary me-2">
                Manage Products
            </a>
            <a href="${pageContext.request.contextPath}/categories" class="btn btn-outline-secondary">
                Manage Categories
            </a>
        </div>
    </div>

    <div class="card shadow-sm">
        <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Up to 10 Newest Products</h5>
        </div>
        <div class="card-body">
            <c:set var="count" value="${fn:length(newestProducts)}" />
            <div class="mb-3">
                <c:choose>
                    <c:when test="${count == 0}">
                        <span class="badge bg-secondary">Showing 0 products</span>
                    </c:when>
                    <c:when test="${count < 10}">
                        <span class="badge bg-info">Showing <c:out value="${count}"/> products</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge bg-success">Showing exactly 10 products</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <table class="table table-hover align-middle">
                <thead class="table-dark">
                <tr>
                    <th>ID</th>
                    <th>Product Name</th>
                    <th>Description</th>
                    <th>Price</th>
                    <th>Image</th>
                    <th>Category</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="product" items="${newestProducts}">
                    <tr>
                        <td>
                            <c:out value="${product.productid}" />
                        </td>
                        <td>
                            <c:out value="${product.productname}" />
                        </td>
                        <td>
                            <c:out value="${product.description}" />
                        </td>
                        <td>
                            $<c:out value="${product.price}" />
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty product.images}">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                            <img src="${fn:escapeXml(product.images)}" alt="${fn:escapeXml(product.productname)}" style="max-height: 50px; max-width: 100px;" class="img-thumbnail" />
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}" alt="${fn:escapeXml(product.productname)}" style="max-height: 50px; max-width: 100px;" class="img-thumbnail" />
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">No image</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:out value="${product.category.categoryname}" />
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${product.status == 1}">
                                    <span class="badge bg-success">Active</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Inactive</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty newestProducts}">
                    <tr>
                        <td colspan="7" class="text-center text-muted py-4">
                            No products found.
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

</body>
</html>
