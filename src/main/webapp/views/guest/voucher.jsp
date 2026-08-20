<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<main class="page-shell content-page">
    <div class="page-title">
        <p class="eyebrow dark">ĐẶC QUYỀN WATCHSTORE</p>
        <h1>Kho voucher</h1>
        <p>Lưu mã ưu đãi và sử dụng khi thanh toán để nhận đặc quyền tốt nhất.</p>
    </div>
    
    <div class="voucher-grid">
        <c:forEach items="${vouchers}" var="v">
            <article class="voucher-card">
                <div>
                    <b>${v.voucherCode}</b>
                    <span>
                        <c:choose>
                            <c:when test="${v.discountType == 'PERCENT'}">
                                Giảm <fmt:formatNumber value="${v.discountValue}" pattern="#,##0.##"/>%
                            </c:when>
                            <c:when test="${v.discountType == 'FREESHIP'}">
                                Miễn phí vận chuyển
                            </c:when>
                            <c:otherwise>
                                Giảm <fmt:formatNumber value="${v.discountValue}" pattern="#,##0"/>₫
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
                
                <p>
                    <c:if test="${v.discountType == 'PERCENT' and not empty v.maximumDiscount and v.maximumDiscount > 0}">
                        Tối đa <fmt:formatNumber value="${v.maximumDiscount}" pattern="#,##0"/>₫ · 
                    </c:if>
                    <c:choose>
                        <c:when test="${not empty v.minimumOrderValue and v.minimumOrderValue > 0}">
                            Đơn tối thiểu <fmt:formatNumber value="${v.minimumOrderValue}" pattern="#,##0"/>₫
                        </c:when>
                        <c:otherwise>
                            Không giới hạn giá trị đơn
                        </c:otherwise>
                    </c:choose>
                </p>
                
                <c:if test="${not empty v.description}">
                    <small style="display:block; color:#716e67; margin-top:-4px; margin-bottom:4px;">${v.description}</small>
                </c:if>
                
                <small>
                    <c:choose>
                        <c:when test="${not empty v.endAt}">
                            Hết hạn: ${v.endAt.dayOfMonth}/${v.endAt.monthValue}/${v.endAt.year}
                        </c:when>
                        <c:otherwise>
                            Hiệu lực dài lâu
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${not empty v.usageLimit and v.usageLimit > 0}">
                        · Còn lại: ${v.usageLimit - v.usedCount > 0 ? (v.usageLimit - v.usedCount) : 0} lượt
                    </c:if>
                </small>
                
                <button type="button" data-copy="${v.voucherCode}">Sao chép mã</button>
            </article>
        </c:forEach>
        
        <c:if test="${empty vouchers}">
            <div style="grid-column: 1 / -1; text-align:center; padding: 48px 16px; background:#fffdf8; border:1px dashed #d5a940; border-radius:12px;">
                <p style="font-size:16px; color:#555; margin-bottom:8px;">Hiện tại chưa có mã ưu đãi công khai nào khả dụng.</p>
                <small style="color:#888;">Quý khách vui lòng quay lại sau để nhận các ưu đãi hấp dẫn từ WatchStore.</small>
            </div>
        </c:if>
    </div>
</main>
