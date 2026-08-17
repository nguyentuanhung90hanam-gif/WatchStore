<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<main class="page-shell detail-page"><div class="breadcrumbs"><a href="${cp}/page/home">Trang chủ</a><span>›</span><a href="${cp}/page/products">Sản phẩm</a><span>›</span><b>${product.name}</b></div><div class="detail-grid"><section class="gallery"><div class="main-product-image"><span class="product-badge">${product.badge}</span><img src="${cp}/assets/images/${product.image}" alt="${product.name}"></div><div class="thumbs"><button><img src="${cp}/assets/images/${product.image}" alt=""></button><button><img src="${cp}/assets/images/watch-2.png" alt=""></button><button><img src="${cp}/assets/images/watch-3.png" alt=""></button></div></section><section class="product-summary"><p class="eyebrow dark">${product.brand} · ${product.sku}</p><h1>${product.name}</h1><div class="rating-line"><span class="stars">★★★★★</span><span>${product.rating}/5 · 48 đánh giá · Đã bán 126</span></div><div class="detail-price"><b><fmt:formatNumber value="${product.price}" pattern="#,##0" />₫</b><del><fmt:formatNumber value="${product.oldPrice}" pattern="#,##0" />₫</del><em>-${product.discountPercent}%</em></div><div class="option-block"><b>Màu mặt</b><div class="swatches"><button class="selected">Xanh navy</button><button>Đen</button><button>Bạc</button></div></div><div class="option-block"><b>Dây đeo</b><div class="swatches"><button class="selected">Thép không gỉ</button><button>Dây da</button><button disabled>Mesh · Hết hàng</button></div></div><form class="buy-form" action="${cp}/cart/add" method="post"><input type="hidden" name="id" value="${product.id}"><label>Số lượng<input type="number" name="quantity" min="1" max="${product.stock}" value="1"></label><button class="button button-dark">Thêm vào giỏ</button><button class="button button-gold" formaction="${cp}/cart/add">Mua ngay</button></form><div class="product-benefits"><p>✓ Sản phẩm chính hãng 100%</p><p>◷ Bảo hành 24 tháng</p><p>▱ Miễn phí giao hàng toàn quốc</p><p>↺ Đổi trả trong vòng 7 ngày</p></div></section></div><section class="product-tabs" style="margin-top: 40px;">
    <nav style="display:flex; border-bottom:1px solid #e2e8f0; margin-bottom:20px; gap:10px;">
        <button type="button" class="tab-btn active" data-tab="desc" style="padding:12px 20px; font-weight:600; border:none; background:none; cursor:pointer; font-size:15px; transition: all 0.2s;">Mô tả</button>
        <button type="button" class="tab-btn" data-tab="spec" style="padding:12px 20px; font-weight:600; border:none; background:none; cursor:pointer; font-size:15px; transition: all 0.2s;">Thông số kỹ thuật</button>
        <button type="button" class="tab-btn" data-tab="reviews" style="padding:12px 20px; font-weight:600; border:none; background:none; cursor:pointer; font-size:15px; transition: all 0.2s;">Đánh giá (${reviewsCount != null ? reviewsCount : 0})</button>
    </nav>
    
    <div id="tab-desc" class="tab-content" style="padding:10px 0;">
        <h2 style="font-size:20px; color:#0f172a; margin-bottom:15px;">Tinh thần hiện đại trong từng chi tiết</h2>
        <p style="color:#475569; line-height:1.6; font-size:15px;">${product.name} kết hợp kỹ thuật chế tác chính xác với thiết kế dành riêng cho phong cách nam giới hiện đại. Sản phẩm phù hợp khi đi làm, gặp gỡ đối tác hoặc tham dự sự kiện.</p>
    </div>
    
    <div id="tab-spec" class="tab-content" style="display:none; padding:10px 0;">
        <div class="spec-grid" style="display:grid; grid-template-columns:1fr 1fr; gap:15px;">
            <span style="font-size:14px; color:#475569;"><b style="color:#0f172a; display:block; margin-bottom:4px;">Loại máy</b>Automatic / Quartz cao cấp</span>
            <span style="font-size:14px; color:#475569;"><b style="color:#0f172a; display:block; margin-bottom:4px;">Kính</b>Sapphire chống trầy</span>
            <span style="font-size:14px; color:#475569;"><b style="color:#0f172a; display:block; margin-bottom:4px;">Chống nước</b>10 ATM</span>
            <span style="font-size:14px; color:#475569;"><b style="color:#0f172a; display:block; margin-bottom:4px;">Kích thước</b>40 mm</span>
        </div>
    </div>
    
    <div id="tab-reviews" class="tab-content" style="display:none; padding:10px 0;">
        <c:if test="${hasPurchased}">
            <div style="margin-bottom: 20px; padding: 15px; background: #fffbeb; border: 1px solid #fef3c7; border-radius: 8px; display: flex; justify-content: space-between; align-items: center;">
                <span style="color: #b45309; font-size: 14px; font-weight: 500;">Bạn đã mua sản phẩm này. Hãy chia sẻ cảm nhận của bạn!</span>
                <a href="${cp}/page/reviews?productId=${product.id}" class="button button-gold" style="padding: 8px 16px; font-size: 13px; font-weight: bold; background: #d97706; color: white; border-radius: 6px; text-decoration: none; display: inline-block;">Viết đánh giá</a>
            </div>
        </c:if>
        
        <div style="display:grid; grid-template-columns:1fr 2fr; gap:30px; margin-bottom:30px; padding:20px; background:#f8fafc; border-radius:8px; border:1px solid #e2e8f0; align-items:center;">
            <div style="text-align:center; border-right:1px solid #e2e8f0; padding-right:20px;">
                <div style="font-size:48px; font-weight:bold; color:#0f172a; line-height:1;">${averageRating != null ? averageRating : '5.0'}</div>
                <div style="color:#d97706; font-size:20px; margin:8px 0;">
                    <c:choose>
                        <c:when test="${averageRating >= 4.5}">★★★★★</c:when>
                        <c:when test="${averageRating >= 3.5}">★★★★☆</c:when>
                        <c:when test="${averageRating >= 2.5}">★★★☆☆</c:when>
                        <c:otherwise>★★☆☆☆</c:otherwise>
                    </c:choose>
                </div>
                <div style="color:#64748b; font-size:13px;">${reviewsCount != null ? reviewsCount : 0} nhận xét</div>
            </div>
            
            <div style="display:flex; flex-direction:column; gap:8px;">
                <c:forEach var="star" items="5,4,3,2,1">
                    <div class="star-filter" data-star="${star}" style="display:flex; align-items:center; gap:10px; cursor:pointer; font-size:13px; color:#475569; padding:2px 8px; transition: background 0.2s;">
                        <span style="width:50px; text-align:right;">${star} sao</span>
                        <div style="flex:1; height:8px; background:#e2e8f0; border-radius:4px; overflow:hidden;">
                            <div class="star-bar-${star}" style="height:100%; background:#d97706; width:0%;"></div>
                        </div>
                        <span class="star-count-${star}" style="width:30px; text-align:left;">(0)</span>
                    </div>
                </c:forEach>
            </div>
        </div>
        
        <div id="reviews-list-container">
            <!-- Reviews list will be rendered here dynamically -->
        </div>
    </div>
</section>

<script>
document.addEventListener("DOMContentLoaded", function() {
    // 1. Tab Switching Logic
    let tabButtons = document.querySelectorAll(".tab-btn");
    let tabContents = document.querySelectorAll(".tab-content");
    
    tabButtons.forEach(btn => {
        btn.addEventListener("click", function() {
            tabButtons.forEach(b => {
                b.classList.remove("active");
                b.style.borderBottom = "2px solid transparent";
                b.style.color = "#64748b";
            });
            tabContents.forEach(c => c.style.display = "none");
            
            this.classList.add("active");
            this.style.borderBottom = "2px solid #d97706";
            this.style.color = "#0f172a";
            
            let tabId = "tab-" + this.getAttribute("data-tab");
            let targetDiv = document.getElementById(tabId);
            if (targetDiv) targetDiv.style.display = "block";
        });
        
        if (btn.classList.contains("active")) {
            btn.style.borderBottom = "2px solid #d97706";
            btn.style.color = "#0f172a";
        } else {
            btn.style.color = "#64748b";
        }
    });

    // 2. Reviews Filtering & Count Logic
    let reviews = [
        <c:forEach var="r" items="${reviewsList}" varStatus="status">
            {
                rating: ${r.rating},
                customerName: "${r.customerName}",
                title: "${r.reviewTitle != null ? r.reviewTitle : ''}",
                content: "${r.reviewContent != null ? r.reviewContent : ''}",
                verified: ${r.verifiedPurchase},
                date: "${r.createdAt}",
                replyContent: "${r.replyContent != null ? r.replyContent : ''}",
                replierName: "${r.replierName != null ? r.replierName : ''}",
                replyDate: "${r.replyCreatedAt != null ? r.replyCreatedAt : ''}"
            }${not status.last ? ',' : ''}
        </c:forEach>
    ];

    let counts = {5:0, 4:0, 3:0, 2:0, 1:0};
    reviews.forEach(r => { 
        if (counts[r.rating] !== undefined) {
            counts[r.rating]++; 
        }
    });
    
    let total = reviews.length;
    for (let i = 1; i <= 5; i++) {
        let pct = total > 0 ? (counts[i] / total) * 100 : 0;
        let bar = document.querySelector(".star-bar-" + i);
        let countLbl = document.querySelector(".star-count-" + i);
        if (bar) bar.style.width = pct + "%";
        if (countLbl) countLbl.textContent = "(" + counts[i] + ")";
    }

    function renderReviews(filteredReviews) {
        let container = document.getElementById("reviews-list-container");
        if (!container) return;
        
        if (filteredReviews.length === 0) {
            container.innerHTML = '<p style="text-align:center; color:#64748b; padding:20px;">Không có đánh giá nào cho mức sao này.</p>';
            return;
        }
        
        let html = "";
        filteredReviews.forEach(r => {
            let starStr = "★".repeat(r.rating) + "☆".repeat(5 - r.rating);
            let badgeHtml = r.verified ? '<span style="background:#d1fae5; color:#065f46; padding:2px 6px; border-radius:4px; font-size:11px; font-weight:600; margin-left:10px; display:inline-flex; align-items:center; gap:2px;">✓ Đã mua hàng</span>' : '';
            
            let replyHtml = "";
            if (r.replyContent) {
                replyHtml = `
                    <div style="margin-top:12px; padding:12px; background:#f1f5f9; border-left:3px solid #d97706; border-radius:4px;">
                        <div style="font-weight:600; color:#334155; font-size:12px;">💬 Phản hồi từ WatchStore (\${r.replierName || 'Quản trị viên'})</div>
                        <div style="color:#475569; font-size:13px; margin-top:5px; line-height:1.4;">\${r.replyContent}</div>
                    </div>
                `;
            }
            
            html += `
                <div style="padding:20px 0; border-bottom:1px solid #e2e8f0;">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <div>
                           <strong style="color:#0f172a; font-size:14px;">\${r.customerName}</strong>
                           \${badgeHtml}
                        </div>
                        <span style="color:#64748b; font-size:12px;">\${r.date.substring(0, 10)}</span>
                    </div>
                    <div style="color:#d97706; font-size:14px; margin:5px 0;">\${starStr}</div>
                    <h4 style="color:#1e293b; margin:5px 0; font-size:15px;">\${r.title || 'Đánh giá sản phẩm'}</h4>
                    <p style="color:#475569; font-size:14px; margin:5px 0; line-height:1.5;">\${r.content}</p>
                    \${replyHtml}
                </div>
            `;
        });
        container.innerHTML = html;
    }

    // Filter by stars click
    let activeFilter = null;
    let filterRows = document.querySelectorAll(".star-filter");
    filterRows.forEach(row => {
        row.addEventListener("click", function() {
            let starValue = parseInt(this.getAttribute("data-star"));
            
            // Toggle highlight
            filterRows.forEach(r => r.style.background = "transparent");
            
            if (activeFilter === starValue) {
                activeFilter = null;
                renderReviews(reviews);
            } else {
                activeFilter = starValue;
                this.style.background = "#f1f5f9";
                this.style.borderRadius = "4px";
                let filtered = reviews.filter(r => r.rating === starValue);
                renderReviews(filtered);
            }
        });
    });

    // Initial render
    renderReviews(reviews);
});
</script></main>
