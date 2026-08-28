<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Product Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Product Management</h2>
        <div>
            <a href="${pageContext.request.contextPath}/categories" class="btn btn-outline-secondary me-2">
                Manage Categories
            </a>
            <a href="${pageContext.request.contextPath}/products/add" class="btn btn-primary">
                + Add Product
            </a>
        </div>
    </div>

    <c:if test="${param.message == 'add_success'}">
        <div class="alert alert-success">
            Product added successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'update_success'}">
        <div class="alert alert-success">
            Product updated successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'delete_success'}">
        <div class="alert alert-success">
            Product deleted successfully.
        </div>
    </c:if>

    <c:if test="${param.error == 'invalid_id'}">
        <div class="alert alert-danger">
            Invalid product ID provided.
        </div>
    </c:if>

    <c:if test="${param.error == 'not_found'}">
        <div class="alert alert-danger">
            Product does not exist.
        </div>
    </c:if>

    <c:if test="${param.error == 'delete_failed'}">
        <div class="alert alert-danger">
            Failed to delete the product due to a database error.
        </div>
    </c:if>

    <div class="card shadow-sm">
        <div class="card-body">
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
                    <th width="180">Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="product" items="${products}">
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
                                    <c:out value="${product.images}" />
                                </c:when>
                                <c:otherwise>
                                    No image
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
                        <td>
                            <a href="${pageContext.request.contextPath}/products/edit?id=${fn:escapeXml(product.productid)}"
                               class="btn btn-warning btn-sm me-1">
                                Edit
                            </a>
                            <form action="${pageContext.request.contextPath}/products/delete" method="post"
                                  style="display: inline;"
                                  onsubmit="return confirm('Are you sure you want to delete this product?');">
                                <input type="hidden" name="id" value="${fn:escapeXml(product.productid)}" />
                                <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty products}">
                    <tr>
                        <td colspan="8" class="text-center text-muted py-4">
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
