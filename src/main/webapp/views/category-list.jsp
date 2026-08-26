<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Category Management</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Category Management</h2>

        <a href="${pageContext.request.contextPath}/categories?action=add"
           class="btn btn-primary">
            + Add Category
        </a>
    </div>

    <c:if test="${param.message == 'add_success'}">
        <div class="alert alert-success">
            Category added successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'update_success'}">
        <div class="alert alert-success">
            Category updated successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'delete_success'}">
        <div class="alert alert-success">
            Category deleted successfully.
        </div>
    </c:if>

    <div class="card shadow-sm">

        <div class="card-body">

            <table class="table table-hover align-middle">

                <thead class="table-dark">
                <tr>
                    <th>ID</th>
                    <th>Category Name</th>
                    <th>Image</th>
                    <th>Status</th>
                    <th width="180">Action</th>
                </tr>
                </thead>

                <tbody>

                <c:forEach var="category" items="${categories}">
                    <tr>

                        <td>
                            ${category.categoryid}
                        </td>

                        <td>
                            ${category.categoryname}
                        </td>

                        <td>
                            <c:choose>

                                <c:when test="${not empty category.images}">
                                    ${category.images}
                                </c:when>

                                <c:otherwise>
                                    No image
                                </c:otherwise>

                            </c:choose>
                        </td>

                        <td>
                            <c:choose>

                                <c:when test="${category.status == 1}">
                                    <span class="badge bg-success">
                                        Active
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="badge bg-secondary">
                                        Inactive
                                    </span>
                                </c:otherwise>

                            </c:choose>
                        </td>

                        <td>

                            <a
                                    href="${pageContext.request.contextPath}/categories?action=edit&id=${category.categoryid}"
                                    class="btn btn-warning btn-sm">
                                Edit
                            </a>

                            <a
                                    href="${pageContext.request.contextPath}/categories?action=delete&id=${category.categoryid}"
                                    class="btn btn-danger btn-sm"
                                    onclick="return confirm('Are you sure you want to delete this category?');">
                                Delete
                            </a>

                        </td>

                    </tr>
                </c:forEach>

                <c:if test="${empty categories}">
                    <tr>
                        <td colspan="5"
                            class="text-center text-muted py-4">

                            No categories found.

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