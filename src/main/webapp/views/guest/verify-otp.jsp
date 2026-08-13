<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<main class="narrow-page">
    <div class="breadcrumbs">
        <a href="${cp}/auth/login">Đăng nhập</a>
        <span>›</span>
        <b>Xác thực mã OTP</b>
    </div>
    <section class="simple-card centered">
        <span class="step-number">02</span>
        <p class="eyebrow dark">XÁC THỰC BẢO MẬT</p>
        <h1>Xác nhận mã OTP</h1>
        <p>Mã xác thực 6 chữ số đã được gửi tới email: 
            <strong style="color:#d4af37;">
                <c:choose>
                    <c:when test="${otpPurpose == 'FORGOT_PASSWORD'}">${sessionScope.forgotEmail}</c:when>
                    <c:otherwise>${sessionScope.pendingReg.email}</c:otherwise>
                </c:choose>
            </strong>
        </p>

        <c:if test="${not empty error}">
            <div class="form-alert">${error}</div>
        </c:if>
        <c:if test="${not empty sessionScope.flash}">
            <div class="form-alert success">${sessionScope.flash}</div>
            <c:remove var="flash" scope="session" />
        </c:if>

        <form action="${cp}/auth/verify-otp" method="post" class="otp-form" style="margin-top: 20px;">
            <input type="hidden" name="purpose" value="${otpPurpose}">
            <div class="form-group" style="margin-bottom: 20px;">
                <label for="otpCode" style="display:block; margin-bottom: 8px; font-weight: bold;">Mã OTP (6 chữ số)</label>
                <input type="text" id="otpCode" name="otpCode" required maxlength="6" pattern="[0-9]{6}"
                       placeholder="123456" autocomplete="one-time-code"
                       style="font-size: 24px; letter-spacing: 8px; text-align: center; font-weight: bold; width: 100%; max-width: 280px; padding: 10px; margin: 0 auto; display: block; border: 2px solid #ddd; border-radius: 6px;">
            </div>

            <div class="otp-timer-box" style="margin-bottom: 20px; font-size: 14px; color: #666;">
                Thời gian hiệu lực mã OTP: <span id="otp-timer" style="font-weight: bold; color: #e53935;">05:00</span>
            </div>

            <button type="submit" class="button button-gold full">Xác nhận OTP</button>
        </form>

        <div style="margin-top: 25px; padding-top: 15px; border-top: 1px solid #eee;">
            <p style="font-size: 14px; color: #666; margin-bottom: 10px;">Chưa nhận được mã OTP?</p>
            <form action="${cp}/auth/resend-otp" method="post">
                <input type="hidden" name="purpose" value="${otpPurpose}">
                <button type="submit" id="btn-resend-otp" class="button button-dark full" disabled style="opacity: 0.6;">
                    Gửi lại mã OTP (<span id="resend-cooldown">60</span>s)
                </button>
            </form>
        </div>
    </section>
</main>

<script>
(function() {
    // 5-minute countdown timer for OTP expiration
    var expireSeconds = 300;
    var timerEl = document.getElementById("otp-timer");
    var expireInterval = setInterval(function() {
        expireSeconds--;
        if (expireSeconds <= 0) {
            clearInterval(expireInterval);
            if (timerEl) {
                timerEl.textContent = "Hết hạn";
                timerEl.style.color = "#d32f2f";
            }
        } else {
            var m = Math.floor(expireSeconds / 60);
            var s = expireSeconds % 60;
            if (timerEl) {
                timerEl.textContent = (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
            }
        }
    }, 1000);

    // 60-second cooldown timer for resend button
    var resendSeconds = 60;
    var cooldownEl = document.getElementById("resend-cooldown");
    var resendBtn = document.getElementById("btn-resend-otp");
    var resendInterval = setInterval(function() {
        resendSeconds--;
        if (resendSeconds <= 0) {
            clearInterval(resendInterval);
            if (resendBtn) {
                resendBtn.disabled = false;
                resendBtn.style.opacity = "1";
                resendBtn.innerHTML = "Gửi lại mã OTP";
            }
        } else {
            if (cooldownEl) cooldownEl.textContent = resendSeconds;
        }
    }, 1000);
})();
</script>
