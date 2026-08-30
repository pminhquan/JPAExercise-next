<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Category Management</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<main class="container py-5">

    <div class="page-header">

        <div class="page-header__text">
            <h1 class="page-header__title">Category Management</h1>
            <p class="page-header__subtitle">
                ${fn:length(categories)} categories in total
            </p>
        </div>

        <div class="page-header__actions">
            <a href="${pageContext.request.contextPath}/categories?action=add"
               class="btn btn-primary">
                + Add Category
            </a>
            <c:if test="${not empty sessionScope.authenticatedUserId}">
                <a href="${pageContext.request.contextPath}/logout"
                   class="btn btn-ghost">
                    Logout
                </a>
            </c:if>
        </div>

    </div>

    <c:if test="${param.message == 'add_success'}">
        <div class="alert alert-success" role="status">
            Category added successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'update_success'}">
        <div class="alert alert-success" role="status">
            Category updated successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'delete_success'}">
        <div class="alert alert-success" role="status">
            Category deleted successfully.
        </div>
    </c:if>

    <c:if test="${param.error == 'invalid_id'}">
        <div class="alert alert-danger" role="alert">
            Invalid category ID provided.
        </div>
    </c:if>

    <c:if test="${param.error == 'not_found'}">
        <div class="alert alert-danger" role="alert">
            Category does not exist.
        </div>
    </c:if>

    <c:if test="${param.error == 'in_use'}">
        <div class="alert alert-danger" role="alert">
            Cannot delete category because it contains active products.
        </div>
    </c:if>

    <c:if test="${param.error == 'delete_failed'}">
        <div class="alert alert-danger" role="alert">
            Failed to delete the category due to a database error.
        </div>
    </c:if>

    <div class="card shadow-sm card--flush">

        <div class="card-body table-wrap">

            <table class="table table-hover table--data align-middle">

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Category Name</th>
                    <th>Image</th>
                    <th>Status</th>
                    <th class="col-actions">Action</th>
                </tr>
                </thead>

                <tbody>

                <c:forEach var="category" items="${categories}">
                    <tr>

                        <td>
                            <span class="cell-id">
                                <c:out value="${category.categoryid}"/>
                            </span>
                        </td>

                        <td>
                            <span class="cell-strong">
                                <c:out value="${category.categoryname}"/>
                            </span>
                        </td>

                        <td>
                            <c:choose>

                                <c:when test="${not empty category.images}">
                                    <span class="cell-truncate"
                                          tabindex="0"
                                          title="${fn:escapeXml(category.images)}">
                                        <c:out value="${category.images}"/>
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="text-empty">
                                        No image
                                    </span>
                                </c:otherwise>

                            </c:choose>
                        </td>

                        <td>
                            <c:choose>

                                <c:when test="${category.status == 1}">
                                    <span class="badge bg-success badge-status">
                                        Active
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="badge bg-secondary badge-status">
                                        Inactive
                                    </span>
                                </c:otherwise>

                            </c:choose>
                        </td>

                        <td>

                            <div class="table-actions">

                                <a
                                        href="${pageContext.request.contextPath}/categories?action=edit&amp;id=${fn:escapeXml(category.categoryid)}"
                                        class="btn btn-secondary btn-sm">
                                    Edit
                                </a>

                                <form action="${pageContext.request.contextPath}/categories" method="post" class="form-inline" onsubmit="return confirm('Are you sure you want to delete this category?');">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${fn:escapeXml(category.categoryid)}">
                                    <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                </form>

                            </div>

                        </td>

                    </tr>
                </c:forEach>

                <c:if test="${empty categories}">
                    <tr>
                        <td colspan="5">

                            <div class="empty-state">
                                <p class="empty-state__title">
                                    No categories found.
                                </p>
                                <p class="empty-state__text">
                                    Create your first category to get started.
                                </p>
                            </div>

                        </td>
                    </tr>
                </c:if>

                </tbody>

            </table>

        </div>

    </div>

</main>

</body>
</html>
