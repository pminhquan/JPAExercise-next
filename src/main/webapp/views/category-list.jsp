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

<%@ include file="fragments/navbar.jsp" %>

<main class="container py-5">

    <div class="page-header">

        <div class="page-header__text">
            <h1 class="page-header__title">Category Management</h1>
            <p class="page-header__subtitle">
                <c:set var="catCount" value="${fn:length(categories)}" />
                <c:choose>
                    <c:when test="${catCount == 1}">
                        1 category in total
                    </c:when>
                    <c:otherwise>
                        ${catCount} categories in total
                    </c:otherwise>
                </c:choose>
            </p>
        </div>

        <div class="page-header__actions">
            <a href="${pageContext.request.contextPath}/categories?action=add"
               class="btn btn-primary">
                + Add Category
            </a>
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

    <div class="card shadow-sm border-0 card--flush">

        <div class="card-body table-wrap table-responsive p-0">

            <table class="table table-hover table--data align-middle mb-0">

                <thead class="table-light">
                <tr>
                    <th scope="col" style="width: 70px;">ID</th>
                    <th scope="col">Category Name</th>
                    <th scope="col" style="width: 80px;">Image</th>
                    <th scope="col" style="width: 110px;">Status</th>
                    <th scope="col" class="col-actions text-end" style="width: 140px;">Action</th>
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
                                    <c:choose>

                                        <c:when test="${fn:startsWith(category.images, 'http://') or fn:startsWith(category.images, 'https://')}">
                                            <img src="${fn:escapeXml(category.images)}"
                                                 alt="${fn:escapeXml(category.categoryname)}"
                                                 class="thumb"
                                                 width="48"
                                                 height="48"
                                                 loading="lazy"
                                                 onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-flex';"/>
                                            <div class="thumb align-items-center justify-content-center text-muted" style="display:none;" aria-label="No image available" role="img">
                                                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                    <polyline points="21 15 16 10 5 21"></polyline>
                                                </svg>
                                            </div>
                                        </c:when>

                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(category.images)}"
                                                 alt="${fn:escapeXml(category.categoryname)}"
                                                 class="thumb"
                                                 width="48"
                                                 height="48"
                                                 loading="lazy"
                                                 onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-flex';"/>
                                            <div class="thumb align-items-center justify-content-center text-muted" style="display:none;" aria-label="No image available" role="img">
                                                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                    <polyline points="21 15 16 10 5 21"></polyline>
                                                </svg>
                                            </div>
                                        </c:otherwise>

                                    </c:choose>
                                </c:when>

                                <c:otherwise>
                                    <div class="thumb d-inline-flex align-items-center justify-content-center text-muted" aria-label="No image available" role="img">
                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                            <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                            <polyline points="21 15 16 10 5 21"></polyline>
                                        </svg>
                                    </div>
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

                            <div class="table-actions d-flex align-items-center justify-content-end gap-1">

                                <a
                                        href="${pageContext.request.contextPath}/categories?action=edit&amp;id=${fn:escapeXml(category.categoryid)}"
                                        class="btn btn-secondary btn-sm">
                                    Edit
                                </a>

                                <form action="${pageContext.request.contextPath}/categories" method="post" class="form-inline d-inline" onsubmit="return confirm('Are you sure you want to delete this category?');">
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

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
