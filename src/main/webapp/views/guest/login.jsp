<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<main class="auth-page">
    <section class="auth-visual">
        <div>
            <p class="eyebrow">WATCHSTORE MEMBER</p>
            <h1>Phong cách bắt đầu từ những lựa chọn tinh tế.</h1>
            <p>Đăng nhập để lưu sản phẩm yêu thích, theo dõi đơn hàng và nhận đặc quyền dành riêng.</p>
        </div>
    </section>
    <section class="auth-form-wrap">
        <form class="auth-form" action="${cp}/auth/login" method="post">
            <a class="logo" href="${cp}/page/home"><span>W</span>WATCHSTORE</a>
            <p class="eyebrow dark">CHÀO MỪNG TRỞ LẠI</p>
            <h1>Đăng nhập</h1>
            <p class="auth-sub">Sử dụng email để truy cập tài khoản của bạn.</p>

            <c:if test="${param.required == 1}"><div class="form-alert">Vui lòng đăng nhập để tiếp tục.</div></c:if>
            <c:if test="${not empty error}"><div class="form-alert">${error}</div></c:if>
            <c:if test="${not empty sessionScope.flash}">
                <div class="form-alert success">${sessionScope.flash}</div>
                <c:remove var="flash" scope="session" />
            </c:if>

            <label style="display:block; margin-bottom: 15px;">Email
                <input type="email" name="email" required placeholder="customer@watchstore.vn">
            </label>

            <div class="form-group" style="margin-bottom: 15px;">
                <label for="loginPassword" style="display:block; margin-bottom: 5px;">Mật khẩu</label>
                <div style="position: relative; display: flex; align-items: center;">
                    <input type="password" id="loginPassword" name="password" required placeholder="Nhập mật khẩu" style="width: 100%; padding-right: 45px;">
                    <button type="button" class="toggle-password-btn" onclick="togglePassword('loginPassword', this)" aria-label="Hiện mật khẩu" style="position: absolute; right: 10px; background: none; border: none; cursor: pointer; font-size: 18px; padding: 5px; line-height: 1; user-select: none;">👁</button>
                </div>
            </div>

            <div class="form-row">
                <label class="check"><input type="checkbox"> Ghi nhớ đăng nhập</label>
                <a href="${cp}/auth/forgot-password">Quên mật khẩu?</a>
            </div>

            <button type="submit" class="button button-gold full">Đăng nhập</button>
            <p class="auth-switch">Chưa có tài khoản? <a href="${cp}/auth/register">Đăng ký ngay</a></p>
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
