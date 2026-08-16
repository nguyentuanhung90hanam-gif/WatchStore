<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<main class="page-shell account-page">
    <jsp:include page="/views/shared/account-nav.jsp" />
    <section class="account-content">
        <div class="account-heading">
            <div>
                <p class="eyebrow dark">TÀI KHOẢN CỦA TÔI</p>
                <h1>Thông tin cá nhân</h1>
            </div>
            <span class="status-badge success">Đã xác minh</span>
        </div>

        <c:if test="${not empty sessionScope.flash}">
            <div style="background:#d1fae5; border:1px solid #6ee7b7; color:#065f46; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
                ✅ ${sessionScope.flash}
            </div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <form class="profile-form" action="${cp}/page/profile" method="post">
            <div class="profile-avatar">
                <span>${empty sessionScope.user ? 'U' : fn:substring(sessionScope.user.fullName, 0, 1)}</span>
                <button type="button">Thay ảnh đại diện</button>
            </div>
            <div class="form-grid two">
                <label>Họ và tên
                    <input name="fullName" value="${sessionScope.user.fullName}" required>
                </label>
                <label>Email
                    <input type="email" value="${sessionScope.user.email}" disabled>
                    <small>Email dùng để đăng nhập và không thể tự thay đổi.</small>
                </label>
                <label>Số điện thoại
                    <input name="phone" value="${sessionScope.user.phone}">
                </label>
                <label>Giới tính
                    <select name="gender">
                        <option value="">-- Chọn giới tính --</option>
                        <option value="MALE" ${sessionScope.user.gender == 'MALE' ? 'selected' : ''}>Nam</option>
                        <option value="FEMALE" ${sessionScope.user.gender == 'FEMALE' ? 'selected' : ''}>Nữ</option>
                        <option value="OTHER" ${sessionScope.user.gender == 'OTHER' ? 'selected' : ''}>Khác</option>
                    </select>
                </label>
                <label>Ngày sinh
                    <input type="date" name="dateOfBirth" value="${sessionScope.user.dateOfBirth}">
                </label>
                <label>Địa chỉ
                    <input name="address" value="${sessionScope.user.address}" placeholder="Nhập địa chỉ của bạn">
                </label>
                <label>Vai trò hệ thống
                    <input value="${sessionScope.user.roleLabel}" disabled>
                </label>
                <label>Trạng thái tài khoản
                    <input value="${empty sessionScope.user.status ? 'ACTIVE' : sessionScope.user.status}" disabled>
                </label>
            </div>
            <button type="submit" class="button button-dark" style="margin-top:20px;">Lưu thay đổi</button>
        </form>
    </section>
</main>
