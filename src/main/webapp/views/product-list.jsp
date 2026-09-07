<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${managementView ? 'Product Management' : 'Products'}</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<c:set var="managementView" value="${not empty managementView ? managementView : (requestScope['jakarta.servlet.forward.servlet_path'] == '/products')}"/>
<c:set var="listPath" value="${managementView ? '/products' : '/product'}"/>

<%@ include file="fragments/navbar.jsp" %>

<main class="container py-5">

    <div class="page-header">

        <div class="page-header__text">
            <h1 class="page-header__title">
                <c:choose>
                    <c:when test="${managementView}">Product Management</c:when>
                    <c:otherwise>Products</c:otherwise>
                </c:choose>
            </h1>
            <p class="page-header__subtitle">
                <c:choose>
                    <c:when test="${totalProducts == 1}">
                        1 product in total
                    </c:when>
                    <c:otherwise>
                        ${totalProducts} products in total
                    </c:otherwise>
                </c:choose>
            </p>
        </div>

        <c:if test="${managementView}">
            <div class="page-header__actions">
                <a href="${pageContext.request.contextPath}/products/add"
                   class="btn btn-primary">
                    + Add Product
                </a>
            </div>
        </c:if>

    </div>

    <c:if test="${param.message == 'add_success'}">
        <div class="alert alert-success" role="status">
            Product added successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'update_success'}">
        <div class="alert alert-success" role="status">
            Product updated successfully.
        </div>
    </c:if>

    <c:if test="${param.message == 'delete_success'}">
        <div class="alert alert-success" role="status">
            Product deleted successfully.
        </div>
    </c:if>

    <c:if test="${param.error == 'invalid_id'}">
        <div class="alert alert-danger" role="alert">
            Invalid product ID provided.
        </div>
    </c:if>

    <c:choose>
        <c:when test="${managementView}">
            <div class="card shadow-sm card--flush">

                <div class="card-body table-wrap table-responsive">

                    <table class="table table-hover table--data align-middle">

                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Product Name</th>
                            <th>Description</th>
                            <th>Price</th>
                            <th>Image</th>
                            <th>Category</th>
                            <th>Status</th>
                            <c:if test="${managementView}">
                                <th class="col-actions">Action</th>
                            </c:if>
                        </tr>
                        </thead>

                        <tbody>

                        <c:forEach var="product" items="${products}">
                            <tr>

                                <td>
                                    <span class="cell-id">
                                        <c:out value="${product.productid}"/>
                                    </span>
                                </td>

                                <td>
                                    <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=${managementView ? 'products' : 'catalog'}"
                                       class="cell-strong"
                                       aria-label="View details for ${fn:escapeXml(product.productname)}">
                                        <c:out value="${product.productname}"/>
                                    </a>
                                </td>

                                <td>
                                    <c:choose>

                                        <c:when test="${not empty product.description}">
                                            <span class="cell-truncate"
                                                  tabindex="0"
                                                  title="${fn:escapeXml(product.description)}">
                                                <c:out value="${product.description}"/>
                                            </span>
                                        </c:when>

                                        <c:otherwise>
                                            <span class="text-empty">
                                                No description
                                            </span>
                                        </c:otherwise>

                                    </c:choose>
                                </td>

                                <td>
                                    <span class="cell-price">
                                        <fmt:formatNumber value="${product.price}" pattern="#,##0"/>
                                        <span class="currency-symbol" aria-label="Vietnamese Dong">₫</span>
                                    </span>
                                </td>

                                <td>
                                    <c:choose>

                                        <c:when test="${not empty product.images}">
                                            <c:choose>

                                                <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                                    <img src="${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="thumb"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                                    <span class="text-empty" style="display:none;">No image</span>
                                                </c:when>

                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="thumb"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                                    <span class="text-empty" style="display:none;">No image</span>
                                                </c:otherwise>

                                            </c:choose>
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
                                        <c:when test="${not empty product.category.categoryname}">
                                            <span class="cell-category">
                                                <c:out value="${product.category.categoryname}"/>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-empty">
                                                Uncategorized
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:choose>

                                        <c:when test="${product.status == 1}">
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

                                <c:if test="${managementView}">
                                    <td>
                                        <div class="table-actions">
                                            <a href="${pageContext.request.contextPath}/products/edit?id=${fn:escapeXml(product.productid)}"
                                               class="btn btn-secondary btn-sm"
                                               aria-label="Edit ${fn:escapeXml(product.productname)}">
                                                Edit
                                            </a>
                                            <form action="${pageContext.request.contextPath}/products/delete"
                                                  method="post"
                                                  class="form-inline"
                                                  onsubmit="return confirm('Are you sure you want to delete this product?');">
                                                <input type="hidden" name="id" value="${fn:escapeXml(product.productid)}"/>
                                                <button type="submit" class="btn btn-danger btn-sm" aria-label="Delete ${fn:escapeXml(product.productname)}">Delete</button>
                                            </form>
                                        </div>
                                    </td>
                                </c:if>

                            </tr>
                        </c:forEach>

                        <c:if test="${empty products}">
                            <tr>
                                <td colspan="${managementView ? 8 : 7}">

                                    <div class="empty-state">
                                        <p class="empty-state__title">
                                            No products found.
                                        </p>
                                        <p class="empty-state__text">
                                            <c:choose>
                                                <c:when test="${managementView}">
                                                    Add your first product to get started.
                                                </c:when>
                                                <c:otherwise>
                                                    Check back later or browse other categories.
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                        <c:if test="${managementView}">
                                            <div class="mt-3">
                                                <a href="${pageContext.request.contextPath}/products/add"
                                                   class="btn btn-primary btn-sm">
                                                    + Add Product
                                                </a>
                                            </div>
                                        </c:if>
                                    </div>

                                </td>
                            </tr>
                        </c:if>

                        </tbody>

                    </table>

                </div>

                <c:if test="${totalPages > 1}">

                    <div class="pagination-bar">

                        <span class="pagination-info">
                            Page ${currentPage} of ${totalPages}
                        </span>

                        <nav aria-label="Product page navigation">
                            <ul class="pagination justify-content-center mb-0">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <c:choose>
                                        <c:when test="${currentPage == 1}">
                                            <span class="page-link" aria-disabled="true">Previous</span>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${currentPage - 1}" aria-label="Previous page">Previous</a>
                                        </c:otherwise>
                                    </c:choose>
                                </li>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item ${currentPage == i ? 'active' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${i}"${currentPage == i ? ' aria-current="page"' : ''} aria-label="Page ${i}">${i}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <c:choose>
                                        <c:when test="${currentPage == totalPages}">
                                            <span class="page-link" aria-disabled="true">Next</span>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${currentPage + 1}" aria-label="Next page">Next</a>
                                        </c:otherwise>
                                    </c:choose>
                                </li>
                            </ul>
                        </nav>

                    </div>

                </c:if>

            </div>
        </c:when>

        <c:otherwise>
            <c:choose>
                <c:when test="${empty products}">
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <div class="empty-state" role="status" aria-live="polite">
                                <div class="empty-state__icon" aria-hidden="true">
                                    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                                        <line x1="3" y1="6" x2="21" y2="6"></line>
                                        <path d="M16 10a4 4 0 0 1-8 0"></path>
                                    </svg>
                                </div>
                                <h2 class="empty-state__title">
                                    No products found in catalog.
                                </h2>
                                <p class="empty-state__text">
                                    There are currently no products available in the catalog. You can return to the homepage or check back later.
                                </p>
                                <a href="${pageContext.request.contextPath}/home"
                                   class="btn btn-outline-primary btn-sm mt-3"
                                   style="min-height: 44px; display: inline-flex; align-items: center; justify-content: center; padding: 0.5rem 1.25rem;">
                                    Back to homepage
                                </a>
                            </div>
                        </div>
                    </div>
                </c:when>

                <c:otherwise>
                    <section class="catalog-toolbar" aria-label="Catalog search and filter toolbar" id="catalogToolbar">
                        <div class="catalog-search-wrap">
                            <label for="catalogSearch" class="visually-hidden">Search products</label>
                            <span class="catalog-search__icon" aria-hidden="true">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <circle cx="11" cy="11" r="8"></circle>
                                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                                </svg>
                            </span>
                            <input type="search"
                                   id="catalogSearch"
                                   class="form-control catalog-search__input"
                                   placeholder="Search products by name or description..."
                                   autocomplete="off"
                                   aria-label="Search products by name or description"/>
                            <button type="button"
                                    id="catalogClear"
                                    class="catalog-search__clear"
                                    aria-label="Clear product search"
                                    title="Clear product search"
                                    style="display: none;">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                    <line x1="18" y1="6" x2="6" y2="18"></line>
                                    <line x1="6" y1="6" x2="18" y2="18"></line>
                                </svg>
                            </button>
                        </div>

                        <div class="catalog-pills-wrap" role="region" aria-label="Filter by category">
                            <span class="catalog-pills-label">Categories:</span>
                            <div class="catalog-pills" role="group" aria-label="Product categories" id="catalogPills">
                                <button type="button"
                                        class="catalog-pill catalog-pill--active"
                                        data-category-filter="all"
                                        aria-pressed="true">
                                    All
                                </button>
                            </div>
                        </div>
                    </section>

                    <div class="product-grid product-grid--catalog">
                        <c:forEach var="product" items="${products}">
                            <div class="product-card"
                                 data-product-card
                                 data-category="${fn:escapeXml(product.category.categoryname)}"
                                 data-name="${fn:escapeXml(product.productname)}"
                                 data-description="${fn:escapeXml(product.description)}">

                                <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=catalog"
                                   class="product-card__media"
                                   aria-label="View details for ${fn:escapeXml(product.productname)}">

                                    <c:choose>
                                        <c:when test="${not empty product.images}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                                    <img src="${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                                    <span class="text-empty" style="display:none;">No image</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                                    <span class="text-empty" style="display:none;">No image</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-empty">
                                                No image
                                            </span>
                                        </c:otherwise>
                                    </c:choose>

                                    <c:choose>
                                        <c:when test="${product.status == 1}">
                                            <span class="product-card__badge badge bg-success badge-status">
                                                Active
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="product-card__badge badge bg-secondary badge-status">
                                                Inactive
                                            </span>
                                        </c:otherwise>
                                    </c:choose>

                                </a>

                                <div class="product-card__body">

                                    <c:if test="${not empty product.category.categoryname}">
                                        <span class="product-card__category">
                                            <c:out value="${product.category.categoryname}"/>
                                        </span>
                                    </c:if>

                                    <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=catalog"
                                       class="product-card__name">
                                        <c:out value="${product.productname}"/>
                                    </a>

                                    <c:if test="${not empty product.description}">
                                        <span class="product-card__desc">
                                            <c:out value="${product.description}"/>
                                        </span>
                                    </c:if>

                                    <div class="product-card__footer">
                                        <span class="product-card__price">
                                            <fmt:formatNumber value="${product.price}" pattern="#,##0"/>
                                            <span class="currency-symbol" aria-label="Vietnamese Dong">₫</span>
                                        </span>

                                        <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=catalog"
                                           class="btn btn-primary btn-sm"
                                           aria-label="View details for ${fn:escapeXml(product.productname)}">
                                            View detail
                                        </a>
                                    </div>

                                </div>

                            </div>
                        </c:forEach>
                    </div>

                    <div class="card shadow-sm mt-4 catalog-filtered-empty" id="catalogFilteredEmpty" style="display: none;">
                        <div class="card-body">
                            <div class="empty-state" role="status" aria-live="polite">
                                <div class="empty-state__icon" aria-hidden="true">
                                    <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="11" cy="11" r="8"></circle>
                                        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                                    </svg>
                                </div>
                                <h2 class="empty-state__title">
                                    No matching products found.
                                </h2>
                                <p class="empty-state__text">
                                    Try adjusting your search query or selecting a different category.
                                </p>
                                <button type="button" class="btn btn-outline-primary btn-sm mt-3" id="catalogResetFilters" style="min-height: 44px; display: inline-flex; align-items: center; justify-content: center; padding: 0.5rem 1.25rem;">
                                    Reset search &amp; filters
                                </button>
                            </div>
                        </div>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="card shadow-sm card--flush mt-4" style="overflow: hidden;">
                            <div class="pagination-bar border-top-0">

                                <span class="pagination-info">
                                    Page ${currentPage} of ${totalPages}
                                </span>

                                <nav aria-label="Product page navigation">
                                    <ul class="pagination justify-content-center mb-0">
                                        <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                            <c:choose>
                                                <c:when test="${currentPage == 1}">
                                                    <span class="page-link" aria-disabled="true">Previous</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${currentPage - 1}" aria-label="Previous page">Previous</a>
                                                </c:otherwise>
                                            </c:choose>
                                        </li>
                                        <c:forEach var="i" begin="1" end="${totalPages}">
                                            <li class="page-item ${currentPage == i ? 'active' : ''}">
                                                <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${i}"${currentPage == i ? ' aria-current="page"' : ''} aria-label="Page ${i}">${i}</a>
                                            </li>
                                        </c:forEach>
                                        <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                            <c:choose>
                                                <c:when test="${currentPage == totalPages}">
                                                    <span class="page-link" aria-disabled="true">Next</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a class="page-link" href="${pageContext.request.contextPath}${listPath}?page=${currentPage + 1}" aria-label="Next page">Next</a>
                                                </c:otherwise>
                                            </c:choose>
                                        </li>
                                    </ul>
                                </nav>

                            </div>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>

</main>

<c:if test="${not managementView}">
<script>
(function() {
    function initCatalog() {
        var toolbar = document.getElementById('catalogToolbar');
        var searchInput = document.getElementById('catalogSearch');
        var clearBtn = document.getElementById('catalogClear');
        var pillsContainer = document.getElementById('catalogPills');
        var emptyState = document.getElementById('catalogFilteredEmpty');
        var resetBtn = document.getElementById('catalogResetFilters');
        var cards = Array.from(document.querySelectorAll('.product-grid--catalog .product-card[data-product-card]'));

        if (!toolbar || !cards.length) return;

        var activeCategory = 'all';
        var searchTerm = '';

        // Collect unique categories
        var categories = [];
        var categoryMap = {};
        cards.forEach(function(card) {
            var cat = (card.getAttribute('data-category') || '').trim();
            if (cat && !categoryMap[cat.toLowerCase()]) {
                categoryMap[cat.toLowerCase()] = true;
                categories.push(cat);
            }
        });
        categories.sort(function(a, b) {
            return a.localeCompare(b, undefined, { sensitivity: 'base' });
        });

        // Add dynamic category pills
        categories.forEach(function(cat) {
            var pill = document.createElement('button');
            pill.type = 'button';
            pill.className = 'catalog-pill';
            pill.setAttribute('data-category-filter', cat);
            pill.setAttribute('aria-pressed', 'false');
            pill.textContent = cat;
            pillsContainer.appendChild(pill);
        });

        var allPills = Array.from(pillsContainer.querySelectorAll('.catalog-pill'));

        function applyFilter() {
            var visibleCount = 0;
            cards.forEach(function(card) {
                var cardCat = (card.getAttribute('data-category') || '').trim().toLowerCase();
                var cardName = (card.getAttribute('data-name') || '').toLowerCase();
                var cardDesc = (card.getAttribute('data-description') || '').toLowerCase();

                var matchCat = (activeCategory === 'all') || (cardCat === activeCategory.toLowerCase());
                var matchSearch = !searchTerm || cardName.indexOf(searchTerm) !== -1 || cardDesc.indexOf(searchTerm) !== -1;

                if (matchCat && matchSearch) {
                    card.classList.remove('catalog-card--hidden');
                    visibleCount++;
                } else {
                    card.classList.add('catalog-card--hidden');
                }
            });

            if (emptyState) {
                emptyState.style.display = visibleCount === 0 ? 'block' : 'none';
            }
        }

        searchInput.addEventListener('input', function() {
            searchTerm = this.value.trim().toLowerCase();
            if (clearBtn) {
                clearBtn.style.display = this.value.length > 0 ? 'inline-flex' : 'none';
            }
            applyFilter();
        });

        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
            } else if (e.key === 'Escape' && this.value) {
                this.value = '';
                searchTerm = '';
                if (clearBtn) clearBtn.style.display = 'none';
                applyFilter();
            }
        });

        if (clearBtn) {
            clearBtn.addEventListener('click', function() {
                searchInput.value = '';
                searchTerm = '';
                clearBtn.style.display = 'none';
                searchInput.focus();
                applyFilter();
            });
        }

        pillsContainer.addEventListener('click', function(e) {
            var pill = e.target.closest('.catalog-pill');
            if (!pill) return;

            var targetCat = pill.getAttribute('data-category-filter') || 'all';
            activeCategory = targetCat;

            allPills.forEach(function(p) {
                var isSelected = (p.getAttribute('data-category-filter') || '').toLowerCase() === activeCategory.toLowerCase();
                p.classList.toggle('catalog-pill--active', isSelected);
                p.setAttribute('aria-pressed', isSelected ? 'true' : 'false');
            });

            applyFilter();
        });

        if (resetBtn) {
            resetBtn.addEventListener('click', function() {
                searchInput.value = '';
                searchTerm = '';
                if (clearBtn) clearBtn.style.display = 'none';
                activeCategory = 'all';

                allPills.forEach(function(p) {
                    var isAll = (p.getAttribute('data-category-filter') || '').toLowerCase() === 'all';
                    p.classList.toggle('catalog-pill--active', isAll);
                    p.setAttribute('aria-pressed', isAll ? 'true' : 'false');
                });

                searchInput.focus();
                applyFilter();
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCatalog);
    } else {
        initCatalog();
    }
})();
</script>
</c:if>

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
