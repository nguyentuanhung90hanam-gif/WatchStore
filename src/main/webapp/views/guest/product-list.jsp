<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<main class="page-shell listing-page pro-listing-page">
    <div class="breadcrumbs">
        <a href="${cp}/page/home">Trang chủ</a><span>›</span><b>Sản phẩm</b>
    </div>
    
    <div class="listing-heading">
        <div>
            <p class="eyebrow dark">BỘ SƯU TẬP ĐỒNG HỒ</p>
            <h1>Tất cả sản phẩm</h1>
            <span>Tìm thấy ${productPage.totalItems} sản phẩm</span>
        </div>
        <button class="filter-mobile" data-filter-toggle>Bộ lọc</button>
    </div>

    <c:if test="${not empty filterMessage}">
        <div class="form-alert" style="margin-bottom: 20px;">${filterMessage}</div>
    </c:if>

    <div class="listing-layout">
        <aside class="filter-panel" data-filter>
            <div class="filter-title">
                <b>Bộ lọc sản phẩm</b>
                <button data-filter-toggle aria-label="Đóng bộ lọc">×</button>
            </div>
            
            <form id="filter-form" action="${cp}/page/products" method="get">
                <label>Từ khóa
                    <input name="q" value="${criteria.keyword}" placeholder="Tên hoặc thương hiệu">
                </label>

                <label>Thương hiệu
                    <select name="brand">
                        <option value="">Tất cả thương hiệu</option>
                        <c:forEach items="${brands}" var="b">
                            <option value="${b}" ${criteria.brand == b ? 'selected' : ''}>${b}</option>
                        </c:forEach>
                    </select>
                </label>

                <c:if test="${not empty categories}">
                    <label>Danh mục
                        <select name="category">
                            <option value="">Tất cả danh mục</option>
                            <c:forEach items="${categories}" var="cat">
                                <option value="${cat}" ${criteria.category == cat ? 'selected' : ''}>${cat}</option>
                            </c:forEach>
                        </select>
                    </label>
                </c:if>

                <fieldset style="border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; margin-bottom: 15px;">
                    <legend style="padding: 0 5px; font-weight: bold; font-size: 13px;">Khoảng giá (VNĐ)</legend>
                    <label style="margin-bottom: 8px;">Từ giá
                        <input type="number" name="minPrice" value="${criteria.minPrice}" placeholder="Ví dụ: 1000000" min="0" step="100000">
                    </label>
                    <label>Đến giá
                        <input type="number" name="maxPrice" value="${criteria.maxPrice}" placeholder="Ví dụ: 15000000" min="0" step="100000">
                    </label>
                </fieldset>

                <label class="check" style="margin-bottom: 20px; display: flex; align-items: center; gap: 8px;">
                    <input type="checkbox" name="inStock" value="1" ${criteria.inStockOnly ? 'checked' : ''}>
                    <span>Chỉ hiện sản phẩm còn hàng</span>
                </label>

                <input type="hidden" name="sort" value="${criteria.sort}">
                <button type="submit" class="button button-dark full">Áp dụng bộ lọc</button>
                <a href="${cp}/page/products" class="button button-outline full" style="display: block; text-align: center; margin-top: 10px; text-decoration: none;">Xóa lọc</a>
            </form>
        </aside>

        <section class="listing-results">
            <div class="sort-bar" style="display: flex; justify-content: space-between; align-items: center; gap: 15px; margin-bottom: 20px;">
                <div class="search-box" style="flex: 1;">
                    <input type="text" form="filter-form" name="q" value="${criteria.keyword}" placeholder="Tìm sản phẩm nhanh..." style="width: 100%; padding: 8px 12px; border-radius: 6px; border: 1px solid #ccc;">
                </div>
                <div>
                    <select aria-label="Sắp xếp sản phẩm" onchange="document.querySelector('#filter-form input[name=sort]').value = this.value; document.getElementById('filter-form').submit();" style="padding: 8px 12px; border-radius: 6px; border: 1px solid #ccc;">
                        <option value="featured" ${criteria.sort == 'featured' ? 'selected' : ''}>Sắp xếp: Nổi bật</option>
                        <option value="price_asc" ${criteria.sort == 'price_asc' ? 'selected' : ''}>Giá tăng dần</option>
                        <option value="price_desc" ${criteria.sort == 'price_desc' ? 'selected' : ''}>Giá giảm dần</option>
                        <option value="newest" ${criteria.sort == 'newest' ? 'selected' : ''}>Mới nhất</option>
                    </select>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty products}">
                    <div style="text-align: center; padding: 50px 20px; background: #fff; border-radius: 12px; border: 1px solid #eee;">
                        <p style="font-size: 18px; color: #666; margin-bottom: 15px;">Không tìm thấy sản phẩm phù hợp với bộ lọc hiện tại.</p>
                        <a href="${cp}/page/products" class="button button-gold">Xem tất cả sản phẩm</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="product-grid listing-grid pro-product-grid">
                        <c:forEach items="${products}" var="product">
                            <article class="product-card pro-product-card">
                                <div class="product-media">
                                    <c:if test="${not empty product.badge}"><span class="product-badge">${product.badge}</span></c:if>
                                    <form action="${cp}/wishlist" method="post" style="display:inline;">
                                        <input type="hidden" name="productId" value="${product.id}">
                                        <button type="submit" class="wish-button" aria-label="Thêm ${product.name} vào yêu thích">
                                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"/></svg>
                                        </button>
                                    </form>
                                    <a class="product-image-link" href="${cp}/page/product?id=${product.id}">
                                        <img src="${cp}/assets/images/${product.image}" alt="${product.name}" loading="lazy">
                                    </a>
                                    <form action="${cp}/cart/add" method="post">
                                        <input type="hidden" name="id" value="${product.id}">
                                        <button class="quick-add"><span>+</span> Thêm vào giỏ hàng</button>
                                    </form>
                                </div>
                                <div class="product-info">
                                    <div class="pro-product-brand">
                                        <p>${product.brand}</p>
                                        <span class="stars">★★★★★ <small>(${product.rating})</small></span>
                                    </div>
                                    <h3><a href="${cp}/page/product?id=${product.id}">${product.name}</a></h3>
                                    <small class="pro-product-spec">Nam · Chính hãng · Bảo hành uy tín</small>
                                    <div class="price">
                                        <b><fmt:formatNumber value="${product.price}" pattern="#,##0" />₫</b>
                                        <c:if test="${not empty product.oldPrice && product.oldPrice.signum() > 0}">
                                            <del><fmt:formatNumber value="${product.oldPrice}" pattern="#,##0" />₫</del>
                                        </c:if>
                                        <c:if test="${product.discountPercent > 0}">
                                            <em>-${product.discountPercent}%</em>
                                        </c:if>
                                    </div>
                                    <p class="pro-installment">Trả góp 0% · Miễn phí vận chuyển</p>
                                </div>
                            </article>
                        </c:forEach>
                    </div>

                    <c:if test="${productPage.totalPages > 1}">
                        <nav class="pagination" aria-label="Phân trang" style="margin-top: 30px; display: flex; justify-content: center; gap: 8px;">
                            <c:if test="${productPage.hasPrevious}">
                                <a href="${cp}/page/products?${queryWithoutPage}&page=${productPage.page - 1}">‹</a>
                            </c:if>
                            <c:forEach begin="1" end="${productPage.totalPages}" var="pNum">
                                <a href="${cp}/page/products?${queryWithoutPage}&page=${pNum}" class="${pNum == productPage.page ? 'active' : ''}">${pNum}</a>
                            </c:forEach>
                            <c:if test="${productPage.hasNext}">
                                <a href="${cp}/page/products?${queryWithoutPage}&page=${productPage.page + 1}">›</a>
                            </c:if>
                        </nav>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>
