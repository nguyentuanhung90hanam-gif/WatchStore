<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<main class="auth-page">
    <section class="auth-visual register-visual">
        <div>
            <p class="eyebrow">WATCHSTORE PRIVILEGE</p>
            <h1>Gia nhập cộng đồng quý ông hiện đại.</h1>
            <p>Tận hưởng ưu đãi thành viên và dịch vụ chăm sóc riêng biệt.</p>
        </div>
    </section>
    <section class="auth-form-wrap">
        <form class="auth-form" action="${cp}/auth/register" method="post">
            <a class="logo" href="${cp}/page/home"><span>W</span>WATCHSTORE</a>
            <p class="eyebrow dark">TẠO TÀI KHOẢN</p>
            <h1>Đăng ký</h1>

            <c:if test="${not empty error}"><div class="form-alert">${error}</div></c:if>

            <div class="form-grid">
                <label>Họ và tên
                    <input name="fullName" required placeholder="Nguyễn Văn An">
                </label>
                <label>Email
                    <input type="email" name="email" required placeholder="email@example.com">
                </label>
                <label>Số điện thoại
                    <input name="phone" inputmode="numeric" pattern="[0-9]{9,11}" maxlength="11" placeholder="09xxxxxxxx">
                </label>

                <div class="form-group">
                    <label for="regPassword" style="display:block; margin-bottom: 5px;">Mật khẩu</label>
                    <div style="position: relative; display: flex; align-items: center;">
                        <input type="password" id="regPassword" name="password" required minlength="6" placeholder="Tối thiểu 6 ký tự" style="width: 100%; padding-right: 45px;">
                        <button type="button" class="toggle-password-btn" onclick="togglePassword('regPassword', this)" aria-label="Hiện mật khẩu" style="position: absolute; right: 10px; background: none; border: none; cursor: pointer; font-size: 18px; padding: 5px; line-height: 1; user-select: none;">👁</button>
                    </div>
                </div>

                <div class="form-group">
                    <label for="regConfirmPassword" style="display:block; margin-bottom: 5px;">Xác nhận mật khẩu</label>
                    <div style="position: relative; display: flex; align-items: center;">
                        <input type="password" id="regConfirmPassword" name="confirmPassword" required placeholder="Nhập lại mật khẩu" style="width: 100%; padding-right: 45px;">
                        <button type="button" class="toggle-password-btn" onclick="togglePassword('regConfirmPassword', this)" aria-label="Hiện mật khẩu" style="position: absolute; right: 10px; background: none; border: none; cursor: pointer; font-size: 18px; padding: 5px; line-height: 1; user-select: none;">👁</button>
                    </div>
                </div>
            </div>

            <label class="check">
                <input type="checkbox" required> Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật.
            </label>
            <button type="submit" class="button button-gold full">Tạo tài khoản & Nhận mã OTP</button>
            <p class="auth-switch">Đã có tài khoản? <a href="${cp}/auth/login">Đăng nhập</a></p>
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
