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

<%@ include file="fragments/navbar.jsp" %>

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
                        &larr; Back to Categories
                    </a>
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
                                value="${fn:escapeXml(param.categoryid != null ? param.categoryid : category.categoryid)}">

                        <div class="form-field">

                            <label class="form-label">
                                Category ID
                            </label>

                            <div>
                                <span class="field-static">
                                    <c:out value="${param.categoryid != null ? param.categoryid : category.categoryid}"/>
                                </span>
                            </div>

                            <span class="form-hint">
                                System-generated unique identifier (read-only).
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
                                    placeholder="e.g. Laptops, Smartphones, Audio"
                                    maxlength="100"
                                    required>

                            <span class="form-hint">
                                A clear, unique name shown in product forms and catalog filters.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label" for="images">
                                Image / Image URL
                            </label>

                            <c:set var="currentImage"
                                   value="${param.images != null ? param.images : category.images}"/>

                            <c:if test="${not empty currentImage}">
                                <div class="d-flex align-items-center gap-3 mb-2 p-2 rounded border" style="background-color: var(--bg-app);">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(currentImage, 'http://') or fn:startsWith(currentImage, 'https://')}">
                                            <img src="${fn:escapeXml(currentImage)}"
                                                 alt="Current category image"
                                                 class="thumb"
                                                 onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                            <span class="text-empty small" style="display:none;">(Preview unavailable)</span>
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(currentImage)}"
                                                 alt="Current category image"
                                                 class="thumb"
                                                 onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                            <span class="text-empty small" style="display:none;">(Preview unavailable)</span>
                                        </c:otherwise>
                                    </c:choose>
                                    <div>
                                        <div class="small text-muted">Current image reference:</div>
                                        <code class="small"><c:out value="${currentImage}"/></code>
                                    </div>
                                </div>
                            </c:if>

                            <input
                                    type="text"
                                    id="images"
                                    name="images"
                                    class="form-control"
                                    value="${fn:escapeXml(currentImage)}"
                                    placeholder="e.g. laptop.jpg or https://images.unsplash.com/...">

                            <span class="form-hint">
                                Optional. File name located in /uploads/ (presets: laptop.jpg, smartphone.jpg, headphone.jpg, tablet.jpg, mouse.jpg, smartwatch.jpg) or a full web image URL (https://...). Leave as-is to keep the current image.
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
                                    Active - Visible in product forms and catalog
                                </option>

                                <option
                                        value="0"
                                        ${selectedStatus == 0 || selectedStatus == '0' ? 'selected' : ''}>
                                    Inactive - Hidden from active product creation
                                </option>

                            </select>

                            <span class="form-hint">
                                Inactive categories stay in the system but cannot be selected for new products.
                            </span>

                        </div>

                        <div class="form-actions">

                            <a
                                    href="${pageContext.request.contextPath}/categories"
                                    class="btn btn-secondary">
                                Cancel
                            </a>

                            <button
                                    type="submit"
                                    class="btn btn-primary">
                                Update Category
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
