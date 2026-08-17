<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<main class="page-shell account-page">
    <jsp:include page="/views/shared/account-nav.jsp" />
    <section class="account-content">
        <div class="account-heading">
            <div>
                <p class="eyebrow dark">CHIA SẺ TRẢI NGHIỆM</p>
                <h1>Đánh giá sản phẩm</h1>
            </div>
        </div>
        
        <c:choose>
            <c:when test="${not empty reviewProduct}">
                <article class="review-form-card" style="display:flex; gap:25px; background:white; padding:20px; border-radius:8px; border:1px solid #e2e8f0;">
                    <img src="${cp}/assets/images/${reviewProduct.image}" alt="${reviewProduct.name}" style="width:120px; height:120px; border-radius:8px; object-fit:cover; border:1px solid #f1f5f9;">
                    <div style="flex:1;">
                        <small style="color:#64748b;">Mã SP: ${reviewProduct.sku}</small>
                        <h3 style="margin:5px 0 15px 0; color:#0f172a; font-size:18px;">${reviewProduct.name}</h3>
                        
                        <form action="${cp}/page/reviews/submit" method="post" id="reviewForm">
                            <input type="hidden" name="productId" value="${reviewProduct.id}">
                            <input type="hidden" name="rating" id="ratingInput" value="5">
                            
                            <div class="form-group" style="margin-bottom: 15px;">
                                <label style="font-weight:600; font-size:13px; color:#475569; display:block; margin-bottom:5px;">Chất lượng sản phẩm <span style="color:red;">*</span></label>
                                <div class="star-input" style="display:flex; gap:8px; font-size:26px; cursor:pointer; color:#d97706;">
                                    <span class="star-btn" data-star="1">★</span>
                                    <span class="star-btn" data-star="2">★</span>
                                    <span class="star-btn" data-star="3">★</span>
                                    <span class="star-btn" data-star="4">★</span>
                                    <span class="star-btn" data-star="5">★</span>
                                </div>
                            </div>
                            
                            <div class="form-group" style="margin-bottom: 15px;">
                                <label style="font-weight:600; font-size:13px; color:#475569; display:block; margin-bottom:5px;">Tiêu đề đánh giá</label>
                                <input type="text" name="title" placeholder="Nhập tiêu đề đánh giá (ví dụ: Rất tốt, Đẹp...)" style="width:100%; height:40px; padding:0 12px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none; box-sizing:border-box;">
                            </div>
                            
                            <div class="form-group" style="margin-bottom: 15px;">
                                <label style="font-weight:600; font-size:13px; color:#475569; display:block; margin-bottom:5px;">Nội dung bình luận <span style="color:red;">*</span></label>
                                <textarea name="content" placeholder="Sản phẩm có đúng mô tả? Trải nghiệm của bạn như thế nào?" style="width:100%; height:100px; padding:10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none; resize:none; font-family:inherit; box-sizing:border-box;" required></textarea>
                            </div>
                            
                            <button type="submit" class="button button-gold" style="width:100%; height:42px; border:none; border-radius:6px; background:#d97706; color:white; font-weight:bold; cursor:pointer; font-size:15px;">Gửi đánh giá</button>
                        </form>
                        <p class="form-note" style="margin-top:10px; font-size:12px; color:#64748b;">Sau khi gửi, đánh giá sẽ được kiểm duyệt và bạn không thể tự sửa hoặc xóa.</p>
                    </div>
                </article>
            </c:when>
            <c:otherwise>
                <div style="padding:40px; background:#f8fafc; border:1px dashed #cbd5e1; border-radius:8px; text-align:center;">
                    <p style="color:#64748b; font-size:15px; margin-bottom:20px;">Vui lòng chọn sản phẩm cần đánh giá từ danh sách đơn hàng đã hoàn thành.</p>
                    <a href="${cp}/orders/list" class="button button-gold" style="display:inline-block; padding:10px 20px; background:#d97706; color:white; font-weight:bold; border-radius:6px; text-decoration:none; font-size:14px;">Xem đơn hàng của tôi</a>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<script>
document.addEventListener("DOMContentLoaded", function() {
    let stars = document.querySelectorAll(".star-btn");
    let ratingInput = document.getElementById("ratingInput");
    if (!stars || stars.length === 0) return;
    
    stars.forEach(star => {
        star.addEventListener("click", function() {
            let r = parseInt(this.getAttribute("data-star"));
            ratingInput.value = r;
            stars.forEach(s => {
                let sr = parseInt(s.getAttribute("data-star"));
                if (sr <= r) {
                    s.style.color = "#d97706";
                } else {
                    s.style.color = "#cbd5e1";
                }
            });
        });
    });
});
</script>
