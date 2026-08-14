<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<main class="narrow-page">
    <div class="breadcrumbs">
        <a href="${cp}/auth/login">Đăng nhập</a>
        <span>›</span>
        <b>Quên mật khẩu</b>
    </div>
    <section class="simple-card centered">
        <span class="step-number">01</span>
        <p class="eyebrow dark">KHÔI PHỤC TÀI KHOẢN</p>
        <h1>Quên mật khẩu?</h1>
        <p>Nhập email đã đăng ký. Hệ thống sẽ gửi mã OTP để bạn đặt lại mật khẩu mới.</p>

        <c:if test="${not empty error}"><div class="form-alert">${error}</div></c:if>

        <form action="${cp}/auth/forgot-password" method="post" style="text-align: left; margin-top: 15px;">
            <label style="display:block; margin-bottom: 15px;">Email
                <input type="email" name="email" required placeholder="email@example.com" style="width:100%; margin-top:5px;">
            </label>
            <button type="submit" class="button button-gold full">Gửi mã OTP qua Gmail</button>
        </form>
        <small style="display:block; margin-top:15px; color:#888;">Quy trình khôi phục: Email → Nhận OTP Gmail → Đặt mật khẩu mới</small>
    </section>
</main>
