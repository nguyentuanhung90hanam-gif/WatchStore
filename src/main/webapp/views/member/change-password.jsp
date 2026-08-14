<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
            <div class="form-group" style="margin-bottom:15px;">
                <label for="oldPass" style="display:block; margin-bottom: 5px;">Mật khẩu hiện tại</label>
                <div style="position: relative; display: flex; align-items: center;">
                    <input type="password" id="oldPass" name="oldPassword" required placeholder="Nhập mật khẩu hiện tại" style="width:100%; padding-right:45px;">
                    <button type="button" class="toggle-password-btn" onclick="togglePassword('oldPass', this)" aria-label="Hiện mật khẩu" style="position: absolute; right: 10px; background: none; border: none; cursor: pointer; font-size: 18px; padding: 5px; line-height: 1; user-select: none;">👁</button>
                </div>
            </div>

            <div class="form-group" style="margin-bottom:15px;">
                <label for="newPass" style="display:block; margin-bottom: 5px;">Mật khẩu mới</label>
                <div style="position: relative; display: flex; align-items: center;">
                    <input type="password" id="newPass" name="newPassword" minlength="6" required placeholder="Nhập mật khẩu mới (ít nhất 6 ký tự)" style="width:100%; padding-right:45px;">
                    <button type="button" class="toggle-password-btn" onclick="togglePassword('newPass', this)" aria-label="Hiện mật khẩu" style="position: absolute; right: 10px; background: none; border: none; cursor: pointer; font-size: 18px; padding: 5px; line-height: 1; user-select: none;">👁</button>
                </div>
            </div>

            <div class="form-group" style="margin-bottom:15px;">
                <label for="confirmPass" style="display:block; margin-bottom: 5px;">Xác nhận mật khẩu mới</label>
                <div style="position: relative; display: flex; align-items: center;">
                    <input type="password" id="confirmPass" name="confirmPassword" minlength="6" required placeholder="Nhập lại mật khẩu mới" style="width:100%; padding-right:45px;">
                    <button type="button" class="toggle-password-btn" onclick="togglePassword('confirmPass', this)" aria-label="Hiện mật khẩu" style="position: absolute; right: 10px; background: none; border: none; cursor: pointer; font-size: 18px; padding: 5px; line-height: 1; user-select: none;">👁</button>
                </div>
            </div>

            <div class="password-rules" style="margin-top:15px; margin-bottom:20px;">
                <b>Mật khẩu an toàn nên có:</b>
                <span>✓ Tối thiểu 6 ký tự</span>
                <span>✓ Kết hợp chữ và số</span>
            </div>

            <button type="submit" class="button button-dark">Cập nhật mật khẩu</button>
        </form>
    </section>
</main>
<script src="${cp}/assets/js/password-toggle.js"></script>
<script>
function togglePassword(inputId, button) {
    var input = document.getElementById(inputId);
    if (!input) return;
    if (input.type === "password") {
        input.type = "text";
        button.textContent = "🙈";
    } else {
        input.type = "password";
        button.textContent = "👁";
    }
}
</script>
