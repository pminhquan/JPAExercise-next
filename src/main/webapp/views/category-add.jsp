<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Add Category</title>

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
                        Add Category
                    </h4>
                </div>

                <div class="card-body">

                    <% if (request.getAttribute("error") != null) { %>

                    <div class="alert alert-danger">
                        <%= request.getAttribute("error") %>
                    </div>

                    <% } %>

                    <form
                            action="${pageContext.request.contextPath}/categories?action=insert"
                            method="post">

                        <div class="mb-3">

                            <label class="form-label">
                                Category Name
                            </label>

                            <input
                                    type="text"
                                    name="categoryname"
                                    class="form-control"
                                    value="${param.categoryname}"
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
                                    value="${param.images}"
                                    placeholder="Example: laptop.jpg">

                        </div>

                        <div class="mb-3">

                            <label class="form-label">
                                Status
                            </label>

                            <select
                                    name="status"
                                    class="form-select">

                                <option value="1">
                                    Active
                                </option>

                                <option value="0">
                                    Inactive
                                </option>

                            </select>

                        </div>

                        <button
                                type="submit"
                                class="btn btn-primary">
                            Save
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