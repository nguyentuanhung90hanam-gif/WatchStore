<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<main class="page-shell listing-page pro-listing-page">
    <div class="breadcrumbs"><a href="${cp}/page/home">Trang chủ</a><span>›</span><b>Sản phẩm</b></div>
    <div class="listing-heading"><div><p class="eyebrow dark">BỘ SƯU TẬP NAM</p><h1>Đồng hồ thời trang</h1><span>${products.size()} sản phẩm được tìm thấy</span></div><button class="filter-mobile" data-filter-toggle>Bộ lọc</button></div>
    <div class="listing-layout">
        <aside class="filter-panel" data-filter>
            <div class="filter-title"><b>Bộ lọc sản phẩm</b><button data-filter-toggle aria-label="Đóng bộ lọc">×</button></div>
            <label>Từ khóa<input form="search-products" name="q" value="${param.q}" placeholder="Tên hoặc thương hiệu"></label>
            <label>Khoảng giá<select><option>Tất cả mức giá</option><option>Dưới 5 triệu</option><option>5–10 triệu</option><option>Trên 10 triệu</option></select></label>
            <fieldset><legend>Thương hiệu</legend><label><input type="checkbox"> Casio</label><label><input type="checkbox"> Orient</label><label><input type="checkbox"> Seiko</label><label><input type="checkbox"> Tissot</label></fieldset>
            <fieldset><legend>Loại máy</legend><label><input type="checkbox"> Automatic</label><label><input type="checkbox"> Quartz</label><label><input type="checkbox"> Solar</label></fieldset>
            <button class="button button-dark" form="search-products">Áp dụng bộ lọc</button>
        </aside>
        <section class="listing-results">
            <div class="sort-bar"><form id="search-products" action="${cp}/page/products"><input name="q" value="${param.q}" placeholder="Tìm sản phẩm"><button>Tìm kiếm</button></form><select aria-label="Sắp xếp sản phẩm"><option>Sắp xếp: Nổi bật</option><option>Giá tăng dần</option><option>Giá giảm dần</option><option>Mới nhất</option></select></div>
            <div class="product-grid listing-grid pro-product-grid">
                <c:forEach items="${products}" var="product">
                    <article class="product-card pro-product-card">
                        <div class="product-media">
                            <c:if test="${not empty product.badge}"><span class="product-badge">${product.badge}</span></c:if>
                            <a class="wish-button" href="${cp}/page/wishlist" aria-label="Thêm ${product.name} vào yêu thích"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"/></svg></a>
                            <a class="product-image-link" href="${cp}/page/product?id=${product.id}"><img src="${cp}/assets/images/${product.image}" alt="${product.name}" loading="lazy"></a>
                            <form action="${cp}/cart/add" method="post"><input type="hidden" name="id" value="${product.id}"><button class="quick-add"><span>+</span> Thêm vào giỏ hàng</button></form>
                        </div>
                        <div class="product-info"><div class="pro-product-brand"><p>${product.brand}</p><span class="stars">★★★★★ <small>(${product.rating})</small></span></div><h3><a href="${cp}/page/product?id=${product.id}">${product.name}</a></h3><small class="pro-product-spec">Nam · Chính hãng · Bảo hành uy tín</small><div class="price"><b><fmt:formatNumber value="${product.price}" pattern="#,##0" />₫</b><del><fmt:formatNumber value="${product.oldPrice}" pattern="#,##0" />₫</del><c:if test="${product.discountPercent > 0}"><em>-${product.discountPercent}%</em></c:if></div><p class="pro-installment">Trả góp 0% · Miễn phí vận chuyển</p></div>
                    </article>
                </c:forEach>
            </div>
            <nav class="pagination" aria-label="Phân trang"><a class="active">1</a><a>2</a><a>3</a><a aria-label="Trang tiếp">›</a></nav>
        </section>
    </div>
</main>
