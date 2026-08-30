<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit Category</title>

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
                    <h1 class="page-header__title">Edit Category</h1>
                    <p class="page-header__subtitle">
                        Update the details of an existing category.
                    </p>
                </div>

                <div class="page-header__actions">
                    <a href="${pageContext.request.contextPath}/categories"
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

                    <form
                            action="${pageContext.request.contextPath}/categories?action=update"
                            method="post">

                        <input
                                type="hidden"
                                name="categoryid"
                                value="${fn:escapeXml(category.categoryid)}">

                        <div class="form-field">

                            <label class="form-label">
                                Category ID
                            </label>

                            <span class="field-static">
                                <c:out value="${category.categoryid}"/>
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label label-required" for="categoryname">
                                Category Name
                            </label>

                            <input
                                    type="text"
                                    id="categoryname"
                                    name="categoryname"
                                    class="form-control"
                                    value="${fn:escapeXml(param.categoryname != null ? param.categoryname : category.categoryname)}"
                                    required>

                            <span class="form-hint">
                                A short, unique name shown in product forms.
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
                                    value="${fn:escapeXml(param.images != null ? param.images : category.images)}">

                            <span class="form-hint">
                                Optional. File name or URL of the category image.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label" for="status">
                                Status
                            </label>

                            <c:set var="selectedStatus"
                                   value="${param.status != null ? param.status : category.status}"/>

                            <select
                                    id="status"
                                    name="status"
                                    class="form-select">

                                <option
                                        value="1"
                                        ${selectedStatus == 1 || selectedStatus == '1' ? 'selected' : ''}>
                                    Active
                                </option>

                                <option
                                        value="0"
                                        ${selectedStatus == 0 || selectedStatus == '0' ? 'selected' : ''}>
                                    Inactive
                                </option>

                            </select>

                            <span class="form-hint">
                                Inactive categories stay in the system but can be hidden from use.
                            </span>

                        </div>

                        <div class="form-actions">

                            <a
                                    href="${pageContext.request.contextPath}/categories"
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
