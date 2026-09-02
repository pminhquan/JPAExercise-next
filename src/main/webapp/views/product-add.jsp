<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add Product</title>

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
                    <h1 class="page-header__title">Add Product</h1>
                    <p class="page-header__subtitle">
                        Create a new product for the catalog.
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

                    <form action="${pageContext.request.contextPath}/products/add"
                          method="post">

                        <div class="form-field">

                            <label class="form-label label-required" for="productname">
                                Product Name
                            </label>

                            <input
                                    type="text"
                                    id="productname"
                                    name="productname"
                                    class="form-control"
                                    value="${fn:escapeXml(param.productname)}"
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
                                    step="1"
                                    value="${fn:escapeXml(param.price)}"
                                    required>

                            <span class="form-hint">
                                Must be a whole number greater than 0.
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
                                    rows="3">${fn:escapeXml(param.description)}</textarea>

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
                                    value="${fn:escapeXml(param.images)}"
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

                                <option value="">-- Select Category --</option>

                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.categoryid}"
                                            ${param.categoryid == cat.categoryid ? 'selected' : ''}>
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

                            <select
                                    id="status"
                                    name="status"
                                    class="form-select">

                                <option value="1"
                                        ${param.status == '1' || empty param.status ? 'selected' : ''}>
                                    Active
                                </option>

                                <option value="0"
                                        ${param.status == '0' ? 'selected' : ''}>
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
                                Save
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
