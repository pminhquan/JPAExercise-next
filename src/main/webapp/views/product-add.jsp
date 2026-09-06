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

<%@ include file="fragments/navbar.jsp" %>

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
                    <a href="${pageContext.request.contextPath}/home"
                       class="btn btn-ghost">
                        Home
                    </a>
                    <a href="${pageContext.request.contextPath}/products"
                       class="btn btn-ghost">
                        &larr; Back to Products
                    </a>
                    <a href="${pageContext.request.contextPath}/categories"
                       class="btn btn-ghost">
                        Manage Categories
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
                          method="post"
                          enctype="multipart/form-data">

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
                                    placeholder="e.g. Dell XPS 15 9530, iPhone 15 Pro Max"
                                    maxlength="255"
                                    required>

                            <span class="form-hint">
                                A clear, recognizable title shown in product listings and search results.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label label-required" for="price">
                                Price
                            </label>

                            <div class="input-group">
                                <input
                                        type="number"
                                        id="price"
                                        name="price"
                                        class="form-control"
                                        min="1"
                                        step="1"
                                        value="${fn:escapeXml(param.price)}"
                                        placeholder="e.g. 15000000"
                                        required>
                                <span class="input-group-text">₫ (VND)</span>
                            </div>

                            <span class="form-hint">
                                Positive whole number in Vietnamese Dong (VND), without commas or decimals.
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

                                <option value="">-- Choose a category --</option>

                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.categoryid}"
                                            ${param.categoryid == cat.categoryid ? 'selected' : ''}>
                                        <c:out value="${cat.categoryname}"/>
                                    </option>
                                </c:forEach>

                            </select>

                            <span class="form-hint">
                                The catalog category this product will be classified under.
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
                                    rows="4"
                                    placeholder="Provide specifications, features, warranty, or key details...">${fn:escapeXml(param.description)}</textarea>

                            <span class="form-hint">
                                Optional. Clear product details displayed on the product overview page.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label" for="images">
                                Product Image
                            </label>

                            <input
                                    type="file"
                                    id="images"
                                    name="images"
                                    class="form-control"
                                    accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp">

                            <span class="form-hint">
                                Optional. Supported formats: JPG, JPEG, PNG, WEBP (maximum size 5 MB).
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
                                        ${empty param.status or param.status == '1' ? 'selected' : ''}>
                                    Active - Visible to shoppers in catalog
                                </option>

                                <option value="0"
                                        ${param.status == '0' ? 'selected' : ''}>
                                    Inactive - Hidden from store
                                </option>

                            </select>

                            <span class="form-hint">
                                Inactive products remain in inventory but are hidden from the public catalog.
                            </span>

                        </div>

                        <div class="form-actions">

                            <a href="${pageContext.request.contextPath}/products"
                               class="btn btn-secondary">
                                Cancel
                            </a>

                            <button
                                    type="submit"
                                    class="btn btn-primary">
                                Save Product
                            </button>

                        </div>

                    </form>

                </div>

            </div>

        </div>

    </div>

</main>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
