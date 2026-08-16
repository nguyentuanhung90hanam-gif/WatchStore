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

            <c:if test="${not empty error}">
                <div class="form-alert" style="background:#fde8e8;color:#9b1c1c;margin-bottom:15px;border-left:3px solid #e02424;padding:10px 14px;border-radius:4px;">
                    ${error}
                </div>
            </c:if>
            <c:if test="${not empty sessionScope.flash}">
                <div class="form-alert success" style="margin-bottom:15px;">
                    ${sessionScope.flash}
                </div>
                <c:remove var="flash" scope="session"/>
            </c:if>

            <div class="form-grid">
                <label>Họ và tên
                    <input name="fullName" required placeholder="Nguyễn Văn An"
                           value="${param.fullName}">
                </label>
                <label>Email
                    <input type="email" name="email" required placeholder="email@example.com"
                           value="${param.email}">
                </label>
                <label>Số điện thoại
                    <input name="phone" placeholder="09xx xxx xxx"
                           value="${param.phone}">
                </label>
                <label>Mật khẩu
                    <span class="label-action" data-toggle-password="reg-password" style="cursor:pointer;">Hiện</span>
                    <input type="password" id="reg-password" name="password" required minlength="6"
                           placeholder="Tối thiểu 6 ký tự">
                </label>
                <label>Xác nhận mật khẩu
                    <span class="label-action" data-toggle-password="reg-confirm" style="cursor:pointer;">Hiện</span>
                    <input type="password" id="reg-confirm" name="confirmPassword" required
                           placeholder="Nhập lại mật khẩu">
                </label>
            </div>

            <label class="check">
                <input type="checkbox" required>
                Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật.
            </label>

            <button type="submit" class="button button-gold full">Tạo tài khoản</button>

            <p class="auth-switch">Đã có tài khoản? <a href="${cp}/auth/login">Đăng nhập</a></p>
        </form>
    </section>
</main>
<script>
(function () {
    document.querySelectorAll('[data-toggle-password]').forEach(function (btn) {
        var targetId = btn.getAttribute('data-toggle-password');
        btn.addEventListener('click', function () {
            var inp = document.getElementById(targetId);
            if (!inp) return;
            if (inp.type === 'password') {
                inp.type = 'text';
                btn.textContent = 'Ẩn';
            } else {
                inp.type = 'password';
                btn.textContent = 'Hiện';
            }
        });
    });
})();
</script>
