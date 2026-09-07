<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<main class="container py-5">

    <div class="page-header">
        <div class="page-header__text">
            <h1 class="page-header__title">My Profile</h1>
            <p class="page-header__subtitle">
                Manage your personal information and profile image.
            </p>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">
            <c:out value="${error}" />
        </div>
    </c:if>

    <div class="card">
        <div class="card-body">

            <div class="row g-4 align-items-start">

                <!-- Profile image -->
                <div class="col-md-4 text-center">

                    <c:choose>
                        <c:when test="${not empty user.images}">
                            <img
                                src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(user.images)}"
                                alt="Profile image"
                                class="img-fluid rounded-circle mb-3"
                                style="width: 160px; height: 160px; object-fit: cover;"
                                onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"
                            />

                            <div
                                class="align-items-center justify-content-center rounded-circle bg-light border mx-auto mb-3"
                                style="width:160px;height:160px;display:none;"
                            >
                                No image
                            </div>
                        </c:when>

                        <c:otherwise>
                            <div
                                class="d-flex align-items-center justify-content-center rounded-circle bg-light border mx-auto mb-3"
                                style="width:160px;height:160px;"
                            >
                                No image
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <p class="text-muted small mb-0">
                        JPG, JPEG, PNG or WEBP. Maximum 5 MB.
                    </p>
                </div>

                <!-- Profile form -->
                <div class="col-md-8">

                    <form
                        method="post"
                        action="${pageContext.request.contextPath}/profile"
                        enctype="multipart/form-data"
                    >

                        <div class="form-field">
                            <label class="form-label">Username</label>
                            <input
                                type="text"
                                class="form-control"
                                value="${fn:escapeXml(user.username)}"
                                readonly
                            />
                        </div>

                        <div class="form-field">
                            <label class="form-label">Email</label>
                            <input
                                type="email"
                                class="form-control"
                                value="${fn:escapeXml(user.email)}"
                                readonly
                            />
                        </div>

                        <div class="form-field">
                            <label for="fullname" class="form-label">
                                Full name
                            </label>

                            <input
                                id="fullname"
                                name="fullname"
                                type="text"
                                class="form-control"
                                maxlength="100"
                                value="${fn:escapeXml(user.fullname)}"
                            />
                        </div>

                        <div class="form-field">
                            <label for="phone" class="form-label">
                                Phone
                            </label>

                            <input
                                id="phone"
                                name="phone"
                                type="text"
                                class="form-control"
                                maxlength="30"
                                value="${fn:escapeXml(user.phone)}"
                            />
                        </div>

                        <div class="form-field">
                            <label for="images" class="form-label">
                                Profile image
                            </label>

                            <input
                                id="images"
                                name="images"
                                type="file"
                                class="form-control"
                                accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                            />

                            <span class="form-hint">
                                Leave this empty to keep your current image.
                            </span>
                        </div>

                        <div class="form-actions">
                            <a
                                href="${pageContext.request.contextPath}/home"
                                class="btn btn-secondary"
                            >
                                Cancel
                            </a>

                            <button type="submit" class="btn btn-primary">
                                Save Profile
                            </button>
                        </div>

                    </form>

                </div>
            </div>

        </div>
    </div>

</main>