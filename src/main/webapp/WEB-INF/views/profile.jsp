<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
    <title>My Profile</title>
</head>

<main class="container py-5">

    <div class="row justify-content-center">
        <div class="col-12 col-lg-10 col-xl-9">

            <div class="page-header">
                <div class="page-header__text">
                    <h1 class="page-header__title">My Profile</h1>
                    <p class="page-header__subtitle">
                        Manage your personal information and profile image.
                    </p>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">
                    <c:out value="${error}" />
                </div>
            </c:if>

            <div class="card profile-card">
                <div class="card-body p-4 p-md-5">

                    <div class="row g-4 g-lg-5 align-items-start">

                        <div class="col-md-4 text-center">
                            <div class="profile-side">
                                <div class="profile-avatar mb-3">
                                    <c:choose>
                                        <c:when test="${not empty user.images}">
                                            <img
                                                    src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(user.images)}"
                                                    alt="Profile image"
                                                    class="profile-avatar__img"
                                                    onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"
                                            />
                                            <div class="profile-avatar__fallback" style="display:none;" aria-hidden="true">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                                                    <circle cx="12" cy="7" r="4" />
                                                </svg>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="profile-avatar__fallback" aria-hidden="true">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                                                    <circle cx="12" cy="7" r="4" />
                                                </svg>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="profile-meta mb-3">
                                    <div class="fw-semibold text-truncate text-dark">
                                        <c:choose>
                                            <c:when test="${not empty user.fullname}">${fn:escapeXml(user.fullname)}</c:when>
                                            <c:otherwise>${fn:escapeXml(user.username)}</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="text-muted small text-truncate">
                                        ${fn:escapeXml(user.email)}
                                    </div>
                                </div>

                                <p class="text-muted small mb-0 profile-upload-hint">
                                    JPG, JPEG, PNG or WEBP. Maximum 5 MB.
                                </p>
                            </div>
                        </div>

                        <div class="col-md-8">

                            <form
                                    method="post"
                                    action="${pageContext.request.contextPath}/profile"
                                    enctype="multipart/form-data">

                                <div class="row g-3 mb-3">

                                    <div class="col-sm-6">
                                        <div class="form-field mb-0">
                                            <label for="username" class="form-label">
                                                Username
                                            </label>
                                            <input
                                                    id="username"
                                                    type="text"
                                                    class="form-control"
                                                    value="${fn:escapeXml(user.username)}"
                                                    readonly />
                                        </div>
                                    </div>

                                    <div class="col-sm-6">
                                        <div class="form-field mb-0">
                                            <label for="email" class="form-label">
                                                Email
                                            </label>
                                            <input
                                                    id="email"
                                                    type="email"
                                                    class="form-control"
                                                    value="${fn:escapeXml(user.email)}"
                                                    readonly />
                                        </div>
                                    </div>

                                </div>

                                <div class="row g-3 mb-3">

                                    <div class="col-sm-6">
                                        <div class="form-field mb-0">
                                            <label for="fullname"
                                                   class="form-label">
                                                Full name
                                            </label>
                                            <input
                                                    id="fullname"
                                                    name="fullname"
                                                    type="text"
                                                    class="form-control"
                                                    maxlength="100"
                                                    value="${fn:escapeXml(user.fullname)}" />
                                        </div>
                                    </div>

                                    <div class="col-sm-6">
                                        <div class="form-field mb-0">
                                            <label for="phone"
                                                   class="form-label">
                                                Phone
                                            </label>
                                            <input
                                                    id="phone"
                                                    name="phone"
                                                    type="text"
                                                    class="form-control"
                                                    maxlength="30"
                                                    value="${fn:escapeXml(user.phone)}" />
                                        </div>
                                    </div>

                                </div>

                                <div class="form-field">

                                    <label for="images"
                                           class="form-label">
                                        Profile image
                                    </label>

                                    <input
                                            id="images"
                                            name="images"
                                            type="file"
                                            class="form-control"
                                            accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" />

                                    <span class="form-hint">
                                        Leave this empty to keep your current image.
                                    </span>

                                </div>

                                <div class="form-actions">

                                    <a
                                            href="${pageContext.request.contextPath}/home"
                                            class="btn btn-secondary">
                                        Cancel
                                    </a>

                                    <button
                                            type="submit"
                                            class="btn btn-primary">
                                        Save Profile
                                    </button>

                                </div>

                            </form>

                        </div>

                    </div>

                </div>
            </div>

        </div>
    </div>

</main>