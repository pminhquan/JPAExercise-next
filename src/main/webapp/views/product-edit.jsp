<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Edit Product</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow-sm">
                <div class="card-header">
                    <h4 class="mb-0">Edit Product</h4>
                </div>
                <div class="card-body">

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">
                            <c:out value="${error}" />
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/products/edit" method="post">
                        <input type="hidden" name="productid" value="${fn:escapeXml(product.productid)}">

                        <div class="mb-3">
                            <label class="form-label">Product ID</label>
                            <input type="text" class="form-control" value="${fn:escapeXml(product.productid)}" disabled>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Product Name</label>
                            <input type="text" name="productname" class="form-control"
                                   value="${fn:escapeXml(param.productname != null ? param.productname : product.productname)}" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Description</label>
                            <textarea name="description" class="form-control" rows="3">${fn:escapeXml(param.description != null ? param.description : product.description)}</textarea>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Price</label>
                            <input type="number" step="0.01" name="price" class="form-control"
                                   value="${fn:escapeXml(param.price != null ? param.price : product.price)}" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Image / Image URL</label>
                            <input type="text" name="images" class="form-control"
                                   value="${fn:escapeXml(param.images != null ? param.images : product.images)}" placeholder="Example: phone.jpg">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Category</label>
                            <select name="categoryid" class="form-select" required>
                                <c:set var="selectedCategoryId" value="${param.categoryid != null ? param.categoryid : (product.category != null ? product.category.categoryid : '')}" />
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.categoryid}" ${selectedCategoryId == cat.categoryid ? 'selected' : ''}>
                                        <c:out value="${cat.categoryname}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Status</label>
                            <c:set var="selectedStatus" value="${param.status != null ? param.status : product.status}" />
                            <select name="status" class="form-select">
                                <option value="1" ${selectedStatus == 1 || selectedStatus == '1' ? 'selected' : ''}>Active</option>
                                <option value="0" ${selectedStatus == 0 || selectedStatus == '0' ? 'selected' : ''}>Inactive</option>
                            </select>
                        </div>

                        <button type="submit" class="btn btn-primary">Update</button>
                        <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary">Back</a>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
