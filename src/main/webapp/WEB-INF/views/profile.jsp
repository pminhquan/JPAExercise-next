<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
    <title>My Profile</title>
</head>

<main class="container py-4 py-md-5">

    <div class="row justify-content-center">
        <div class="col-12 col-lg-10 col-xl-9">

            <!-- Breadcrumb Navigation -->
            <nav class="detail-breadcrumb-nav mb-3" aria-label="Breadcrumb">
                <ol class="detail-breadcrumb">
                    <li class="breadcrumb-item">
                        <a href="${pageContext.request.contextPath}/home">Home</a>
                    </li>
                    <li class="breadcrumb-separator" aria-hidden="true">&rsaquo;</li>
                    <li class="breadcrumb-item active" aria-current="page">My Profile</li>
                </ol>
            </nav>

            <div class="page-header mb-4">
                <div class="page-header__text">
                    <h1 class="page-header__title">My Profile</h1>
                    <p class="page-header__subtitle">
                        Manage your personal information, contact details, and profile photo.
                    </p>
                </div>
                <div class="page-header__actions">
                    <a href="${pageContext.request.contextPath}/home" class="btn btn-ghost btn-sm">
                        &larr; Back to Home
                    </a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                    <svg class="flex-shrink-0" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <circle cx="12" cy="12" r="10" />
                        <line x1="12" y1="8" x2="12" y2="12" />
                        <line x1="12" y1="16" x2="12.01" y2="16" />
                    </svg>
                    <div>
                        <c:out value="${error}" />
                    </div>
                </div>
            </c:if>

            <div class="card profile-card">
                <div class="card-body p-3 p-sm-4 p-md-5">

                    <div class="row g-4 g-lg-5 align-items-start">

                        <!-- Left Column: Avatar & User Summary -->
                        <div class="col-12 col-md-4 text-center">
                            <div class="profile-side">
                                <div class="profile-avatar mb-3" id="profileAvatarContainer">
                                    <c:choose>
                                        <c:when test="${not empty user.images}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(user.images, 'http://') or fn:startsWith(user.images, 'https://')}">
                                                    <c:set var="avatarImgSrc" value="${user.images}" />
                                                </c:when>
                                                <c:when test="${fn:startsWith(user.images, '/')}">
                                                    <c:set var="avatarImgSrc" value="${pageContext.request.contextPath}${user.images}" />
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="avatarImgSrc" value="${pageContext.request.contextPath}/uploads/${user.images}" />
                                                </c:otherwise>
                                            </c:choose>
                                            <img
                                                    id="profileAvatarImg"
                                                    src="${fn:escapeXml(avatarImgSrc)}"
                                                    alt="Profile photo of ${fn:escapeXml(not empty user.fullname ? user.fullname : user.username)}"
                                                    class="profile-avatar__img"
                                                    loading="lazy"
                                                    onerror="this.style.display='none'; var fb=document.getElementById('profileAvatarFallback'); if(fb) fb.style.display='flex';"
                                            />
                                            <div id="profileAvatarFallback" class="profile-avatar__fallback" style="display:none;" aria-hidden="true">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                                                    <circle cx="12" cy="7" r="4" />
                                                </svg>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <img
                                                    id="profileAvatarImg"
                                                    src=""
                                                    alt="Profile photo"
                                                    class="profile-avatar__img"
                                                    style="display:none;"
                                            />
                                            <div id="profileAvatarFallback" class="profile-avatar__fallback" aria-hidden="true">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                                                    <circle cx="12" cy="7" r="4" />
                                                </svg>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="profile-meta mb-3">
                                    <h2 class="h5 fw-bold text-truncate text-dark mb-1">
                                        <c:choose>
                                            <c:when test="${not empty user.fullname}">${fn:escapeXml(user.fullname)}</c:when>
                                            <c:otherwise>${fn:escapeXml(user.username)}</c:otherwise>
                                        </c:choose>
                                    </h2>
                                    <div class="text-muted small text-truncate mb-2">
                                        @${fn:escapeXml(user.username)}
                                    </div>
                                    <c:if test="${user.active}">
                                        <span class="badge bg-success badge-status mb-2">
                                            Active Account
                                        </span>
                                    </c:if>
                                    <div class="text-muted small text-truncate">
                                        ${fn:escapeXml(user.email)}
                                    </div>
                                </div>

                                <div class="profile-upload-hint p-2 rounded bg-light border w-100">
                                    <span class="d-block fw-medium text-secondary">Photo Requirements</span>
                                    <span>JPG, JPEG, PNG or WEBP. Max 5 MB.</span>
                                </div>
                            </div>
                        </div>

                        <!-- Right Column: Profile Form -->
                        <div class="col-12 col-md-8">

                            <form
                                    method="post"
                                    action="${pageContext.request.contextPath}/profile"
                                    enctype="multipart/form-data">

                                <!-- Section 1: Account Information (Read-only) -->
                                <div class="profile-section mb-4">
                                    <h3 class="profile-section-heading">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                                            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                                        </svg>
                                        Account Credentials (Read-only)
                                    </h3>

                                    <div class="row g-3">
                                        <div class="col-12 col-sm-6">
                                            <div class="form-field mb-0">
                                                <label for="username" class="form-label">
                                                    Username
                                                </label>
                                                <input
                                                        id="username"
                                                        type="text"
                                                        class="form-control"
                                                        value="${fn:escapeXml(user.username)}"
                                                        readonly
                                                        aria-readonly="true" />
                                                <span class="form-hint">Unique system identifier.</span>
                                            </div>
                                        </div>

                                        <div class="col-12 col-sm-6">
                                            <div class="form-field mb-0">
                                                <label for="email" class="form-label">
                                                    Email address
                                                </label>
                                                <input
                                                        id="email"
                                                        type="email"
                                                        class="form-control"
                                                        value="${fn:escapeXml(user.email)}"
                                                        readonly
                                                        aria-readonly="true" />
                                                <span class="form-hint">Registered primary email.</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Section 2: Personal Details (Editable) -->
                                <div class="profile-section mb-4">
                                    <h3 class="profile-section-heading">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                                            <circle cx="12" cy="7" r="4" />
                                        </svg>
                                        Personal Information
                                    </h3>

                                    <div class="row g-3">
                                        <div class="col-12 col-sm-6">
                                            <div class="form-field mb-0">
                                                <label for="fullname" class="form-label">
                                                    Full name
                                                </label>
                                                <input
                                                        id="fullname"
                                                        name="fullname"
                                                        type="text"
                                                        class="form-control"
                                                        maxlength="100"
                                                        placeholder="Enter your full name"
                                                        autocomplete="name"
                                                        value="${fn:escapeXml(user.fullname)}" />
                                                <span class="form-hint">Max 100 characters.</span>
                                            </div>
                                        </div>

                                        <div class="col-12 col-sm-6">
                                            <div class="form-field mb-0">
                                                <label for="phone" class="form-label">
                                                    Phone number
                                                </label>
                                                <input
                                                        id="phone"
                                                        name="phone"
                                                        type="text"
                                                        class="form-control"
                                                        maxlength="30"
                                                        placeholder="e.g. +84 901 234 567"
                                                        autocomplete="tel"
                                                        value="${fn:escapeXml(user.phone)}" />
                                                <span class="form-hint">Optional contact number (3-30 chars).</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Section 3: Profile Photo Upload Area -->
                                <div class="profile-section mb-4">
                                    <h3 class="profile-section-heading">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                            <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
                                            <circle cx="12" cy="13" r="4" />
                                        </svg>
                                        Profile Photo
                                    </h3>

                                    <div class="profile-upload-zone">
                                        <div class="form-field mb-0">
                                            <label for="images" class="form-label fw-semibold">
                                                Upload new photo
                                            </label>

                                            <input
                                                    id="images"
                                                    name="images"
                                                    type="file"
                                                    class="form-control"
                                                    accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" />

                                            <span class="form-hint mt-2">
                                                <c:choose>
                                                    <c:when test="${not empty user.images}">
                                                        Leave empty to keep your current photo. Selecting a new file will replace it upon saving.
                                                    </c:when>
                                                    <c:otherwise>
                                                        Upload a photo to personalize your profile.
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                            <div id="profileImageSelectedName" class="small text-primary fw-medium mt-2" style="display:none;"></div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Actions: Primary save hierarchy -->
                                <div class="form-actions pt-3 border-top">
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

<script>
(function() {
    var fileInput = document.getElementById('images');
    var selectedName = document.getElementById('profileImageSelectedName');
    var avatarImg = document.getElementById('profileAvatarImg');
    var avatarFallback = document.getElementById('profileAvatarFallback');

    if (!fileInput) return;

    fileInput.addEventListener('change', function(e) {
        var file = e.target.files && e.target.files[0];
        if (file) {
            if (selectedName) {
                selectedName.textContent = 'Selected: ' + file.name + ' (' + (file.size / 1024).toFixed(1) + ' KB)';
                selectedName.style.display = 'block';
            }
            if (avatarImg && file.type && file.type.indexOf('image/') === 0) {
                var reader = new FileReader();
                reader.onload = function(evt) {
                    avatarImg.src = evt.target.result;
                    avatarImg.style.display = 'block';
                    if (avatarFallback) {
                        avatarFallback.style.display = 'none';
                    }
                };
                reader.readAsDataURL(file);
            }
        } else {
            if (selectedName) {
                selectedName.style.display = 'none';
                selectedName.textContent = '';
            }
        }
    });
})();
</script>
