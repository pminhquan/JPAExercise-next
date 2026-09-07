<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Home - Newest Products</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>

<body>

<%@ include file="fragments/navbar.jsp" %>

<main class="container py-5">

    <div class="page-header">

        <div class="page-header__text">
            <h1 class="page-header__title">Newest Products</h1>

            <p class="page-header__subtitle">
                <c:set var="count" value="${fn:length(newestProducts)}" />
                <c:choose>
                    <c:when test="${count == 0}">
                        Showing 0 products
                    </c:when>
                    <c:when test="${count == 1}">
                        Showing 1 product
                    </c:when>
                    <c:when test="${count < 10}">
                        Showing <c:out value="${count}"/> products
                    </c:when>
                    <c:otherwise>
                        Showing exactly 10 products
                    </c:otherwise>
                </c:choose>
            </p>
        </div>
    </div>

    <c:choose>

        <c:when test="${empty newestProducts}">

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
                            No products found.
                        </h2>
                        <p class="empty-state__text">
                            Newest products will appear here once they are added. You can also browse the catalog.
                        </p>
                        <a href="${pageContext.request.contextPath}/product"
                           class="btn btn-outline-primary btn-sm mt-3"
                           style="min-height: 44px; display: inline-flex; align-items: center; justify-content: center; padding: 0.5rem 1.25rem;">
                            Browse catalog
                        </a>
                    </div>

                </div>
            </div>

        </c:when>

        <c:otherwise>

            <section class="catalog-toolbar" aria-label="Catalog search and filter toolbar" id="homeCatalogToolbar">
                <div class="catalog-search-wrap">
                    <label for="homeCatalogSearch" class="visually-hidden">Search products</label>
                    <span class="catalog-search__icon" aria-hidden="true">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                    </span>
                    <input type="search"
                           id="homeCatalogSearch"
                           class="form-control catalog-search__input"
                           placeholder="Search products by name or description..."
                           autocomplete="off"
                           aria-label="Search products by name or description"/>
                    <button type="button"
                            id="homeCatalogClear"
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
                    <div class="catalog-pills" role="group" aria-label="Product categories" id="homeCatalogPills">
                        <button type="button"
                                class="catalog-pill catalog-pill--active"
                                data-category-filter="all"
                                aria-pressed="true">
                            All
                        </button>
                    </div>
                </div>
            </section>

            <div class="product-grid">

                <c:forEach var="product" items="${newestProducts}">

                    <div class="product-card"
                         data-product-card
                         data-category="${fn:escapeXml(product.category.categoryname)}"
                         data-name="${fn:escapeXml(product.productname)}"
                         data-description="${fn:escapeXml(product.description)}">

                        <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=home"
                           class="product-card__media"
                           aria-label="View product details">

                            <c:choose>

                                <c:when test="${not empty product.images}">
                                    <c:choose>

                                        <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                            <img src="${fn:escapeXml(product.images)}"
                                                 alt="${fn:escapeXml(product.productname)}"
                                                 onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='inline-block';"/>
                                            <span class="text-empty" style="display:none;">No image</span>
                                        </c:when>

                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                 alt="${fn:escapeXml(product.productname)}"
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

                        </a>

                        <div class="product-card__body">

                            <span class="product-card__category">
                                <c:out value="${product.category.categoryname}"/>
                            </span>

                            <a href="${pageContext.request.contextPath}/products/detail?id=${fn:escapeXml(product.productid)}&amp;from=home"
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
                                    <fmt:formatNumber value="${product.price}" pattern="#,##0"/> ₫
                                </span>

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

                            </div>

                        </div>

                    </div>

                </c:forEach>

            </div>

            <div class="card shadow-sm mt-4 catalog-filtered-empty" id="homeFilteredEmpty" style="display: none;">
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
                        <button type="button" class="btn btn-outline-primary btn-sm mt-3" id="homeResetFilters" style="min-height: 44px; display: inline-flex; align-items: center; justify-content: center; padding: 0.5rem 1.25rem;">
                            Reset search &amp; filters
                        </button>
                    </div>
                </div>
            </div>

        </c:otherwise>

    </c:choose>

</main>

<script>
(function() {
    function initCatalog() {
        var toolbar = document.getElementById('homeCatalogToolbar');
        var searchInput = document.getElementById('homeCatalogSearch');
        var clearBtn = document.getElementById('homeCatalogClear');
        var pillsContainer = document.getElementById('homeCatalogPills');
        var emptyState = document.getElementById('homeFilteredEmpty');
        var resetBtn = document.getElementById('homeResetFilters');
        var cards = Array.from(document.querySelectorAll('.product-grid .product-card[data-product-card]'));

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

<%@ include file="fragments/footer.jsp" %>

</body>
</html>
