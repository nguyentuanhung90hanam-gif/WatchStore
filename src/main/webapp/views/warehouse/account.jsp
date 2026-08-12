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
                <div>
                    <small>Email</small>
                    <b>${accountUser.email}</b>
                </div>
                <div>
                    <small>Trạng thái</small>
                    <b class="account-status">
                        ${accountUser.status == 'ACTIVE' ? 'Đang hoạt động' : accountUser.status}
                    </b>
                </div>
            </div>

            <form action="${cp}/manage/warehouse/account"
                  method="post"
                  class="warehouse-account-form">

                <div class="warehouse-form-grid">
                    <label>
                        Họ và tên
                        <input type="text"
                               name="fullName"
                               value="${accountUser.fullName}"
                               maxlength="150"
                               required>
                    </label>

                    <label>
                        Số điện thoại
                        <input type="tel"
                               name="phone"
                               value="${accountUser.phone}"
                               maxlength="20"
                               inputmode="tel"
                               placeholder="0901234567">
                    </label>

                    <label>
                        Giới tính
                        <select name="gender">
                            <option value="">Chưa cập nhật</option>
                            <option value="MALE"
                                ${accountUser.gender == 'MALE' ? 'selected' : ''}>
                                Nam
                            </option>
                            <option value="FEMALE"
                                ${accountUser.gender == 'FEMALE' ? 'selected' : ''}>
                                Nữ
                            </option>
                            <option value="OTHER"
                                ${accountUser.gender == 'OTHER' ? 'selected' : ''}>
                                Khác
                            </option>
                        </select>
                    </label>

                    <label>
                        Ngày sinh
                        <input type="date"
                               name="dateOfBirth"
                               value="${accountUser.dateOfBirth}">
                    </label>
                </div>

                <div class="warehouse-account-actions">
                    <button class="warehouse-primary-btn" type="submit">
                        Lưu thông tin
                    </button>
                </div>
            </form>
        </section>

        <section class="warehouse-account-card warehouse-security-card">
            <div class="warehouse-card-heading compact">
                <div class="warehouse-security-icon">⌁</div>

                <div>
                    <p class="warehouse-eyebrow">BẢO MẬT</p>
                    <h3>Đổi mật khẩu</h3>
                    <span>Thay đổi định kỳ để bảo vệ tài khoản.</span>
                </div>
            </div>

            <form action="${cp}/manage/warehouse/account/password"
                  method="post"
                  class="warehouse-account-form"
                  id="warehousePasswordForm">

                <label>
                    Mật khẩu hiện tại
                    <input type="password"
                           name="currentPassword"
                           autocomplete="current-password"
                           required>
                </label>

                <label>
                    Mật khẩu mới
                    <input type="password"
                           name="newPassword"
                           minlength="6"
                           autocomplete="new-password"
                           required>
                </label>

                <label>
                    Xác nhận mật khẩu mới
                    <input type="password"
                           name="confirmPassword"
                           minlength="6"
                           autocomplete="new-password"
                           required>
                </label>

                <div class="warehouse-password-rules">
                    <b>Yêu cầu</b>
                    <span>Tối thiểu 6 ký tự</span>
                    <span>Có chữ hoa, chữ thường và chữ số</span>
                    <span>Khác mật khẩu hiện tại</span>
                </div>

                <button class="warehouse-dark-btn" type="submit">
                    Cập nhật mật khẩu
                </button>
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

<style>
/* =========================================================
   WAREHOUSE ACCOUNT
   ========================================================= */

.warehouse-account {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    color: #222;
    font-family: inherit;
}


/* =========================================================
   HEADER
   ========================================================= */

.warehouse-account-head {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 28px;
    width: 100%;
    margin-bottom: 24px;
    padding: 0;
    box-sizing: border-box;
}

.warehouse-account-head > div {
    min-width: 0;
}

.warehouse-eyebrow {
    margin: 0 0 8px;
    color: #b48820;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 2.2px;
    line-height: 1.4;
    text-transform: uppercase;
}

.warehouse-account-head h2 {
    margin: 0;
    color: #171717;
    font-family: Georgia, "Times New Roman", serif;
    font-size: 31px;
    font-weight: 500;
    line-height: 1.2;
}

.warehouse-account-head > div > p:last-child {
    margin: 8px 0 0;
    color: #777;
    font-size: 13px;
    line-height: 1.6;
}

.warehouse-account-back {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    min-height: 38px;
    padding: 0 15px;
    border: 1px solid #d7d1c4;
    background: #fff;
    color: #5d4a23;
    text-decoration: none;
    font-size: 12px;
    font-weight: 600;
    box-sizing: border-box;
    transition:
        background-color .15s ease,
        border-color .15s ease,
        color .15s ease;
}

.warehouse-account-back:hover {
    border-color: #b88a24;
    background: #faf7ef;
    color: #8c691c;
}


/* =========================================================
   ALERT
   ========================================================= */

.warehouse-alert {
    width: 100%;
    min-height: 42px;
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    padding: 10px 14px;
    box-sizing: border-box;
    border: 1px solid transparent;
    font-size: 13px;
    line-height: 1.5;
}

.warehouse-alert-error {
    border-color: #efcaca;
    background: #fff7f7;
    color: #9d2c2c;
}


/* =========================================================
   MAIN GRID
   ========================================================= */

.warehouse-account-grid {
    display: grid;
    grid-template-columns: minmax(0, 1.35fr) minmax(340px, .9fr);
    align-items: start;
    gap: 16px;
    width: 100%;
    box-sizing: border-box;
}


/* =========================================================
   CARD
   ========================================================= */

.warehouse-account-card {
    width: 100%;
    min-width: 0;
    padding: 22px;
    border: 1px solid #ded9cf;
    background: #fff;
    box-sizing: border-box;
}

.warehouse-profile-card {
    min-height: 100%;
}

.warehouse-security-card {
    min-height: 100%;
}


/* =========================================================
   CARD HEADING
   ========================================================= */

.warehouse-card-heading {
    display: flex;
    align-items: center;
    gap: 14px;
    min-width: 0;
    padding-bottom: 18px;
    border-bottom: 1px solid #ece9e2;
}

.warehouse-card-heading.compact {
    align-items: center;
}

.warehouse-card-heading > div:last-child {
    min-width: 0;
}

.warehouse-card-heading h3 {
    margin: 0;
    color: #202020;
    font-family: Georgia, "Times New Roman", serif;
    font-size: 21px;
    font-weight: 500;
    line-height: 1.3;
}

.warehouse-card-heading span {
    display: block;
    margin-top: 5px;
    color: #777;
    font-size: 12px;
    line-height: 1.5;
}


/* =========================================================
   AVATAR
   ========================================================= */

.warehouse-avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 52px;
    width: 52px;
    height: 52px;
    border: 1px solid #b68b2c;
    border-radius: 50%;
    background: #f8f3e7;
    color: #9b741f;
    font-family: Georgia, "Times New Roman", serif;
    font-size: 22px;
    font-weight: 500;
    box-sizing: border-box;
}


/* =========================================================
   SECURITY ICON
   ========================================================= */

.warehouse-security-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 46px;
    width: 46px;
    height: 46px;
    border: 1px solid #ded5c1;
    background: #faf7ef;
    color: #9c741e;
    font-size: 25px;
    line-height: 1;
    box-sizing: border-box;
}


/* =========================================================
   ACCOUNT META
   ========================================================= */

.warehouse-account-meta {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    gap: 12px;
    margin: 18px 0 20px;
}

.warehouse-account-meta > div {
    min-width: 0;
    padding: 12px 13px;
    border: 1px solid #e6e2da;
    background: #faf9f6;
    box-sizing: border-box;
}

.warehouse-account-meta small {
    display: block;
    margin-bottom: 5px;
    color: #888;
    font-size: 10px;
    font-weight: 600;
    letter-spacing: .7px;
    text-transform: uppercase;
}

.warehouse-account-meta b {
    display: block;
    overflow: hidden;
    color: #333;
    font-size: 13px;
    font-weight: 600;
    line-height: 1.4;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.warehouse-account-meta .account-status {
    color: #39734a;
}


/* =========================================================
   FORM
   ========================================================= */

.warehouse-account-form {
    width: 100%;
    box-sizing: border-box;
}

.warehouse-form-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    gap: 15px;
}

.warehouse-account-form label {
    display: flex;
    flex-direction: column;
    gap: 7px;
    min-width: 0;
    color: #444;
    font-size: 12px;
    font-weight: 600;
    line-height: 1.4;
}

.warehouse-account-form input,
.warehouse-account-form select {
    width: 100%;
    height: 40px;
    min-width: 0;
    padding: 0 11px;
    border: 1px solid #d5d1c9;
    border-radius: 0;
    outline: none;
    background: #fff;
    color: #333;
    font-family: inherit;
    font-size: 13px;
    font-weight: 400;
    line-height: 40px;
    box-sizing: border-box;
    transition:
        border-color .15s ease,
        box-shadow .15s ease,
        background-color .15s ease;
}

.warehouse-account-form input::placeholder {
    color: #aaa;
}

.warehouse-account-form input:hover,
.warehouse-account-form select:hover {
    border-color: #c2bba9;
}

.warehouse-account-form input:focus,
.warehouse-account-form select:focus {
    border-color: #b78b27;
    box-shadow: 0 0 0 3px rgba(183, 139, 39, .10);
    background: #fff;
}

.warehouse-account-form input[type="date"] {
    color: #444;
}


/* =========================================================
   ACCOUNT ACTION
   ========================================================= */

.warehouse-account-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 18px;
    padding-top: 17px;
    border-top: 1px solid #ece9e2;
}


/* =========================================================
   BUTTONS
   ========================================================= */

.warehouse-primary-btn,
.warehouse-dark-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 40px;
    padding: 0 18px;
    border: 1px solid transparent;
    border-radius: 0;
    font-family: inherit;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
    cursor: pointer;
    box-sizing: border-box;
    transition:
        background-color .15s ease,
        border-color .15s ease,
        color .15s ease;
}

.warehouse-primary-btn {
    border-color: #b88a24;
    background: #b88a24;
    color: #fff;
}

.warehouse-primary-btn:hover {
    border-color: #9f761c;
    background: #9f761c;
}

.warehouse-dark-btn {
    width: 100%;
    margin-top: 18px;
    border-color: #222;
    background: #222;
    color: #fff;
}

.warehouse-dark-btn:hover {
    border-color: #111;
    background: #111;
}


/* =========================================================
   SECURITY FORM
   ========================================================= */

.warehouse-security-card .warehouse-account-form {
    padding-top: 20px;
}

.warehouse-security-card .warehouse-account-form label + label {
    margin-top: 14px;
}


/* =========================================================
   PASSWORD RULES
   ========================================================= */

.warehouse-password-rules {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-top: 16px;
    padding: 12px 13px;
    border: 1px solid #e7e1d5;
    background: #faf8f3;
    color: #777;
    font-size: 11px;
    line-height: 1.5;
    box-sizing: border-box;
}

.warehouse-password-rules b {
    margin-bottom: 1px;
    color: #76591f;
    font-size: 11px;
    font-weight: 700;
}

.warehouse-password-rules span {
    position: relative;
    padding-left: 12px;
}

.warehouse-password-rules span::before {
    content: "•";
    position: absolute;
    left: 0;
    color: #b58a2b;
}


/* =========================================================
   RESPONSIVE
   ========================================================= */

@media (max-width: 1100px) {

    .warehouse-account-grid {
        grid-template-columns: 1fr;
    }

    .warehouse-security-card {
        min-height: auto;
    }
}


@media (max-width: 800px) {

    .warehouse-account-head {
        align-items: flex-start;
        flex-direction: column;
        gap: 14px;
    }

    .warehouse-account-back {
        align-self: flex-start;
    }

    .warehouse-form-grid {
        grid-template-columns: 1fr;
    }

    .warehouse-account-meta {
        grid-template-columns: 1fr;
    }
}


@media (max-width: 520px) {

    .warehouse-account-head h2 {
        font-size: 26px;
    }

    .warehouse-account-card {
        padding: 17px;
    }

    .warehouse-card-heading {
        align-items: flex-start;
    }

    .warehouse-avatar,
    .warehouse-security-icon {
        flex-basis: 44px;
        width: 44px;
        height: 44px;
    }

    .warehouse-card-heading h3 {
        font-size: 19px;
    }

    .warehouse-account-actions {
        justify-content: stretch;
    }

    .warehouse-primary-btn {
        width: 100%;
    }
}
</style>