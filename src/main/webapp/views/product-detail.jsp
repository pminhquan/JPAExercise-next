<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Product Details</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <c:choose>
                <c:when test="${not empty error}">
                    <div class="card shadow-sm border-danger">
                        <div class="card-header bg-danger text-white">
                            <h4 class="mb-0">Error</h4>
                        </div>
                        <div class="card-body">
                            <div class="alert alert-danger">
                                <c:out value="${error}" />
                            </div>
                            <a href="${pageContext.request.contextPath}/product" class="btn btn-secondary">Back to Products</a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="card shadow-sm">
                        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                            <h4 class="mb-0">Product Details</h4>
                            <span class="badge ${product.status == 1 ? 'bg-success' : 'bg-secondary'}">
                                <c:out value="${product.status == 1 ? 'Active' : 'Inactive'}" />
                            </span>
                        </div>
                        <div class="card-body">
                            <table class="table table-bordered">
                                <tr>
                                    <th width="30%">Product ID</th>
                                    <td><c:out value="${product.productid}" /></td>
                                </tr>
                                <tr>
                                    <th>Product Name</th>
                                    <td><strong><c:out value="${product.productname}" /></strong></td>
                                </tr>
                                <tr>
                                    <th>Category</th>
                                    <td><c:out value="${product.category.categoryname}" /></td>
                                </tr>
                                <tr>
                                    <th>Price</th>
                                    <td>$<c:out value="${product.price}" /></td>
                                </tr>
                                <tr>
                                    <th>Description</th>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty product.description}">
                                                <c:out value="${product.description}" />
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">No description provided.</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                                <tr>
                                    <th>Image</th>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty product.images}">
                                                <c:choose>
                                                    <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                                        <img src="${fn:escapeXml(product.images)}" alt="${fn:escapeXml(product.productname)}" style="max-height: 200px; max-width: 100%;" class="img-thumbnail" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}" alt="${fn:escapeXml(product.productname)}" style="max-height: 200px; max-width: 100%;" class="img-thumbnail" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">No image</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </table>

                            <div class="mt-4 d-flex justify-content-between">
                                <a href="${pageContext.request.contextPath}/product" class="btn btn-secondary">
                                    Back to Products
                                </a>
                                <div>
                                    <a href="${pageContext.request.contextPath}/products/edit?id=${fn:escapeXml(product.productid)}" class="btn btn-warning">
                                        Edit Product
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

</body>
</html>
