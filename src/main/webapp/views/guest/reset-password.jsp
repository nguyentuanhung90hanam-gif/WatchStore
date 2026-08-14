<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<main class="narrow-page">
    <div class="breadcrumbs">
        <a href="${cp}/auth/login">Đăng nhập</a>
        <span>›</span>
        <b>Đặt lại mật khẩu</b>
    </div>
    <section class="simple-card centered">
        <span class="step-number">03</span>
        <p class="eyebrow dark">TẠO MẬT KHẨU MỚI</p>
        <h1>Đặt lại mật khẩu</h1>
        <p>Nhập mật khẩu mới cho tài khoản: <strong style="color:#d4af37;">${sessionScope.forgotEmail}</strong></p>

        <c:if test="${not empty error}">
            <div class="form-alert">${error}</div>
        </c:if>

        <form action="${cp}/auth/reset-password" method="post" class="auth-form" style="text-align: left; margin-top: 20px;">
            <div class="form-group" style="margin-bottom: 15px;">
                <label for="resetPassword" style="display:block; margin-bottom: 5px;">Mật khẩu mới</label>
                <div style="position: relative; display: flex; align-items: center;">
                    <input type="password" id="resetPassword" name="password" required minlength="6" placeholder="Tối thiểu 6 ký tự" style="width: 100%; padding-right: 45px;">
                    <button type="button" class="toggle-password-btn" onclick="togglePassword('resetPassword', this)" aria-label="Hiện mật khẩu" style="position: absolute; right: 10px; background: none; border: none; cursor: pointer; font-size: 18px; padding: 5px; line-height: 1; user-select: none;">👁</button>
                </div>
            </div>

            <div class="form-group" style="margin-bottom: 20px;">
                <label for="resetConfirmPassword" style="display:block; margin-bottom: 5px;">Xác nhận mật khẩu mới</label>
                <div style="position: relative; display: flex; align-items: center;">
                    <input type="password" id="resetConfirmPassword" name="confirmPassword" required placeholder="Nhập lại mật khẩu mới" style="width: 100%; padding-right: 45px;">
                    <button type="button" class="toggle-password-btn" onclick="togglePassword('resetConfirmPassword', this)" aria-label="Hiện mật khẩu" style="position: absolute; right: 10px; background: none; border: none; cursor: pointer; font-size: 18px; padding: 5px; line-height: 1; user-select: none;">👁</button>
                </div>
            </div>

            <button type="submit" class="button button-gold full">Hoàn tất đặt lại mật khẩu</button>
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
