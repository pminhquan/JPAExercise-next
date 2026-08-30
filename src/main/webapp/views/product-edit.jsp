<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit Product</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<main class="container py-5">

    <div class="row justify-content-center">

        <div class="col-md-7">

            <div class="page-header">

                <div class="page-header__text">
                    <h1 class="page-header__title">Edit Product</h1>
                    <p class="page-header__subtitle">
                        Update the details of an existing product.
                    </p>
                </div>

                <div class="page-header__actions">
                    <a href="${pageContext.request.contextPath}/products"
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

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger" role="alert">
                            <c:out value="${error}"/>
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/products/edit"
                          method="post">

                        <input
                                type="hidden"
                                name="productid"
                                value="${fn:escapeXml(product.productid)}">

                        <div class="form-field">

                            <label class="form-label">
                                Product ID
                            </label>

                            <span class="field-static">
                                <c:out value="${product.productid}"/>
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label label-required" for="productname">
                                Product Name
                            </label>

                            <input
                                    type="text"
                                    id="productname"
                                    name="productname"
                                    class="form-control"
                                    value="${fn:escapeXml(param.productname != null ? param.productname : product.productname)}"
                                    required>

                            <span class="form-hint">
                                A short, descriptive product title.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label label-required" for="price">
                                Price
                            </label>

                            <input
                                    type="number"
                                    id="price"
                                    name="price"
                                    class="form-control"
                                    step="0.01"
                                    value="${fn:escapeXml(param.price != null ? param.price : product.price)}"
                                    required>

                            <span class="form-hint">
                                Must be a number greater than 0.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label" for="description">
                                Description
                            </label>

                            <textarea
                                    id="description"
                                    name="description"
                                    class="form-control"
                                    rows="3">${fn:escapeXml(param.description != null ? param.description : product.description)}</textarea>

                            <span class="form-hint">
                                Optional. Details customers should know about the product.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label" for="images">
                                Image / Image URL
                            </label>

                            <input
                                    type="text"
                                    id="images"
                                    name="images"
                                    class="form-control"
                                    value="${fn:escapeXml(param.images != null ? param.images : product.images)}"
                                    placeholder="Example: phone.jpg">

                            <span class="form-hint">
                                Optional. File name or URL of the product image.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label label-required" for="categoryid">
                                Category
                            </label>

                            <select
                                    id="categoryid"
                                    name="categoryid"
                                    class="form-select"
                                    required>

                                <c:set var="selectedCategoryId"
                                       value="${param.categoryid != null ? fn:escapeXml(param.categoryid) : (product.category != null ? product.category.categoryid : '')}"/>

                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.categoryid}"
                                            ${selectedCategoryId == cat.categoryid ? 'selected' : ''}>
                                        <c:out value="${cat.categoryname}"/>
                                    </option>
                                </c:forEach>

                            </select>

                            <span class="form-hint">
                                The category this product belongs to.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label" for="status">
                                Status
                            </label>

                            <c:set var="selectedStatus"
                                   value="${param.status != null ? fn:escapeXml(param.status) : product.status}"/>

                            <select
                                    id="status"
                                    name="status"
                                    class="form-select">

                                <option value="1"
                                        ${selectedStatus == 1 || selectedStatus == '1' ? 'selected' : ''}>
                                    Active
                                </option>

                                <option value="0"
                                        ${selectedStatus == 0 || selectedStatus == '0' ? 'selected' : ''}>
                                    Inactive
                                </option>

                            </select>

                            <span class="form-hint">
                                Inactive products stay in the system but can be hidden from use.
                            </span>

                        </div>

                        <div class="form-actions">

                            <a href="${pageContext.request.contextPath}/products"
                               class="btn btn-secondary">
                                Back
                            </a>

                            <button
                                    type="submit"
                                    class="btn btn-primary">
                                Update
                            </button>

                        </div>

                    </form>

                </div>

            </div>

        </div>

    </div>

</main>

</body>
</html>
