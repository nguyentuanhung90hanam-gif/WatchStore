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

            <c:if test="${not empty error}">
                <div class="form-alert" style="background:#fde8e8; color:#9b1c1c; margin-bottom:15px; border-left: 3px solid #e02424;">
                    ${error}
                </div>
            </c:if>
            <c:if test="${param.required == 1}">
                <div class="form-alert" style="margin-bottom:15px;">
                    Vui lòng đăng nhập để tiếp tục.
                </div>
            </c:if>

            <label>Email
                <input type="email" name="email" value="${param.email}" required placeholder="customer@watchstore.vn">
            </label>

            <label>Mật khẩu <span class="label-action" data-toggle-password="login-password" style="cursor:pointer;">Hiện</span>
                <input id="login-password" type="password" name="password" required placeholder="Nhập mật khẩu">
            </label>

            <div class="form-row">
                <label class="check">
                    <input type="checkbox" name="remember"> Ghi nhớ đăng nhập
                </label>
                <a href="${cp}/auth/forgot-password">Quên mật khẩu?</a>
            </div>

            <button type="submit" class="button button-gold full">Đăng nhập</button>

            <div class="demo-accounts">
                <b>Tài khoản demo — mật khẩu: 123456</b>
                <span>admin@watchstore.vn · sales@watchstore.vn</span>
                <span>warehouse@watchstore.vn · customer@watchstore.vn</span>
            </div>

            <p class="auth-switch">Chưa có tài khoản? <a href="${cp}/auth/register">Đăng ký ngay</a></p>
        </form>
    </section>
</main>
