<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div class="warehouse-account">
    <section class="warehouse-account-head">
        <div>
            <p class="warehouse-eyebrow">TÀI KHOẢN CÁ NHÂN</p>
            <h2>Quản lý tài khoản</h2>
            <p>Cập nhật thông tin làm việc và bảo mật tài khoản của nhân viên kho.</p>
        </div>
        <a class="warehouse-account-back" href="${cp}/manage/warehouse/dashboard">← Tổng quan kho</a>
    </section>

    <c:if test="${not empty sessionScope.errorMsg}">
        <div class="warehouse-alert warehouse-alert-error">${sessionScope.errorMsg}</div>
        <c:remove var="errorMsg" scope="session"/>
    </c:if>

    <div class="warehouse-account-grid">
        <section class="warehouse-account-card warehouse-profile-card">
            <div class="warehouse-card-heading">
                <div class="warehouse-avatar">${fn:substring(accountUser.fullName, 0, 1)}</div>
                <div>
                    <p class="warehouse-eyebrow">THÔNG TIN CÁ NHÂN</p>
                    <h3>${accountUser.fullName}</h3>
                    <span>${accountUser.role.label}</span>
                </div>
            </div>

            <div class="warehouse-account-meta">
                <div><small>Email</small><b>${accountUser.email}</b></div>
                <div><small>Trạng thái</small><b class="account-status">${accountUser.status == 'ACTIVE' ? 'Đang hoạt động' : accountUser.status}</b></div>
            </div>

            <form action="${cp}/manage/warehouse/account" method="post" class="warehouse-account-form">
                <div class="warehouse-form-grid">
                    <label>Họ và tên<input type="text" name="fullName" value="${accountUser.fullName}" maxlength="150" required></label>
                    <label>Số điện thoại<input type="tel" name="phone" value="${accountUser.phone}" maxlength="20" inputmode="tel" placeholder="0901234567"></label>
                    <label>Giới tính<select name="gender"><option value="">Chưa cập nhật</option><option value="MALE" ${accountUser.gender == 'MALE' ? 'selected' : ''}>Nam</option><option value="FEMALE" ${accountUser.gender == 'FEMALE' ? 'selected' : ''}>Nữ</option><option value="OTHER" ${accountUser.gender == 'OTHER' ? 'selected' : ''}>Khác</option></select></label>
                    <label>Ngày sinh<input type="date" name="dateOfBirth" value="${accountUser.dateOfBirth}"></label>
                </div>
                <div class="warehouse-account-actions"><button class="warehouse-primary-btn" type="submit">Lưu thông tin</button></div>
            </form>
        </section>

        <section class="warehouse-account-card warehouse-security-card">
            <div class="warehouse-card-heading compact">
                <div class="warehouse-security-icon">⌁</div>
                <div><p class="warehouse-eyebrow">BẢO MẬT</p><h3>Đổi mật khẩu</h3><span>Thay đổi định kỳ để bảo vệ tài khoản.</span></div>
            </div>

            <form action="${cp}/manage/warehouse/account/password" method="post" class="warehouse-account-form" id="warehousePasswordForm">
                <label>Mật khẩu hiện tại<input type="password" name="currentPassword" autocomplete="current-password" required></label>
                <label>Mật khẩu mới<input type="password" name="newPassword" minlength="6" autocomplete="new-password" required></label>
                <label>Xác nhận mật khẩu mới<input type="password" name="confirmPassword" minlength="6" autocomplete="new-password" required></label>
                <div class="warehouse-password-rules"><b>Yêu cầu</b><span>Tối thiểu 6 ký tự</span><span>Có chữ hoa, chữ thường và chữ số</span><span>Khác mật khẩu hiện tại</span></div>
                <button class="warehouse-dark-btn" type="submit">Cập nhật mật khẩu</button>
            </form>
        </section>
    </div>
</div>

<script>
document.getElementById('warehousePasswordForm').addEventListener('submit', function (event) {
    const newPassword = this.newPassword.value;
    const confirmPassword = this.confirmPassword.value;
    if (newPassword !== confirmPassword) {
        event.preventDefault();
        alert('Xác nhận mật khẩu mới không khớp.');
    }
});
</script>
