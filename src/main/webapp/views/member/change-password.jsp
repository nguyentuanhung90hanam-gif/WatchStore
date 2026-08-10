<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<main class="page-shell account-page">
    <jsp:include page="/views/shared/account-nav.jsp" />
    <section class="account-content">
        <div class="account-heading">
            <div>
                <p class="eyebrow dark">BẢO MẬT</p>
                <h1>Đổi mật khẩu</h1>
            </div>
        </div>

        <c:if test="${not empty sessionScope.flash}">
            <c:choose>
                <c:when test="${fn:startsWith(sessionScope.flash, 'Lỗi')}">
                    <div style="background:#fee2e2; border:1px solid #fca5a5; color:#991b1b; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
                        ⚠ ${sessionScope.flash}
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="background:#d1fae5; border:1px solid #6ee7b7; color:#065f46; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
                        ✅ ${sessionScope.flash}
                    </div>
                </c:otherwise>
            </c:choose>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <form class="simple-form" action="${cp}/page/change-password" method="post">
            <label>Mật khẩu hiện tại
                <input type="password" name="oldPassword" required placeholder="Nhập mật khẩu hiện tại">
            </label>
            <label>Mật khẩu mới
                <input type="password" name="newPassword" minlength="6" required placeholder="Nhập mật khẩu mới (ít nhất 6 ký tự)">
            </label>
            <label>Xác nhận mật khẩu mới
                <input type="password" name="confirmPassword" minlength="6" required placeholder="Nhập lại mật khẩu mới">
            </label>

            <div class="password-rules" style="margin-top:15px; margin-bottom:20px;">
                <b>Mật khẩu an toàn nên có:</b>
                <span>✓ Tối thiểu 6 ký tự</span>
                <span>✓ Kết hợp chữ và số</span>
            </div>

            <button type="submit" class="button button-dark">Cập nhật mật khẩu</button>
        </form>
    </section>
</main>
