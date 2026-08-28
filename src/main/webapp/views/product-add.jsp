<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Add Product</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow-sm">
                <div class="card-header">
                    <h4 class="mb-0">Add Product</h4>
                </div>
                <div class="card-body">

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">
                            <c:out value="${error}" />
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/products/add" method="post">
                        <div class="mb-3">
                            <label class="form-label">Product Name</label>
                            <input type="text" name="productname" class="form-control"
                                   value="${fn:escapeXml(param.productname)}" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Description</label>
                            <textarea name="description" class="form-control" rows="3">${fn:escapeXml(param.description)}</textarea>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Price</label>
                            <input type="number" step="0.01" name="price" class="form-control"
                                   value="${fn:escapeXml(param.price)}" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Image / Image URL</label>
                            <input type="text" name="images" class="form-control"
                                   value="${fn:escapeXml(param.images)}" placeholder="Example: phone.jpg">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Category</label>
                            <select name="categoryid" class="form-select" required>
                                <option value="">-- Select Category --</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.categoryid}" ${param.categoryid == cat.categoryid ? 'selected' : ''}>
                                        <c:out value="${cat.categoryname}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Status</label>
                            <select name="status" class="form-select">
                                <option value="1" ${param.status == '1' || empty param.status ? 'selected' : ''}>Active</option>
                                <option value="0" ${param.status == '0' ? 'selected' : ''}>Inactive</option>
                            </select>
                        </div>

                        <button type="submit" class="btn btn-primary">Save</button>
                        <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary">Back</a>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
