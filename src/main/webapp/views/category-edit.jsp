<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Edit Category</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container py-5">

    <div class="row justify-content-center">

        <div class="col-md-7">

            <div class="card shadow-sm">

                <div class="card-header">
                    <h4 class="mb-0">
                        Edit Category
                    </h4>
                </div>

                <div class="card-body">

                    <% if (request.getAttribute("error") != null) { %>

                    <div class="alert alert-danger">
                        <%= request.getAttribute("error") %>
                    </div>

                    <% } %>

                    <form
                            action="${pageContext.request.contextPath}/categories?action=update"
                            method="post">

                        <input
                                type="hidden"
                                name="categoryid"
                                value="${category.categoryid}">

                        <div class="mb-3">

                            <label class="form-label">
                                Category ID
                            </label>

                            <input
                                    type="text"
                                    class="form-control"
                                    value="${category.categoryid}"
                                    disabled>

                        </div>

                        <div class="mb-3">

                            <label class="form-label">
                                Category Name
                            </label>

                            <input
                                    type="text"
                                    name="categoryname"
                                    class="form-control"
                                    value="${category.categoryname}"
                                    required>

                        </div>

                        <div class="mb-3">

                            <label class="form-label">
                                Image / Image URL
                            </label>

                            <input
                                    type="text"
                                    name="images"
                                    class="form-control"
                                    value="${category.images}">

                        </div>

                        <div class="mb-3">

                            <label class="form-label">
                                Status
                            </label>

                            <select
                                    name="status"
                                    class="form-select">

                                <option
                                        value="1"
                                        ${category.status == 1 ? 'selected' : ''}>
                                    Active
                                </option>

                                <option
                                        value="0"
                                        ${category.status == 0 ? 'selected' : ''}>
                                    Inactive
                                </option>

                            </select>

                        </div>

                        <button
                                type="submit"
                                class="btn btn-primary">
                            Update
                        </button>

                        <a
                                href="${pageContext.request.contextPath}/categories"
                                class="btn btn-secondary">
                            Back
                        </a>

                    </form>

                </div>

            </div>

        </div>

    </div>

</div>

</body>
</html>