<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: Arial, sans-serif; background: #f5f6fa; color: #333; }
        .container { max-width: 750px; margin: auto; padding: 30px; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
        .header h1 { margin: 0; font-size: 26px; }
        .back { color: #2563eb; text-decoration: none; font-size: 14px; }
        .box { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
        .box h2 { margin-top: 0; font-size: 20px; margin-bottom: 22px; }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .form-group { margin-bottom: 5px; }
        .form-group.full { grid-column: 1 / -1; }
        label { display: block; margin-bottom: 8px; font-weight: 500; font-size: 14px; }
        input, textarea {
            width: 100%; padding: 10px 12px; border: 1px solid #ddd;
            border-radius: 6px; font-size: 14px; outline: none; transition: border-color .2s;
        }
        input:focus, textarea:focus { border-color: #2563eb; }
        textarea { height: 80px; resize: vertical; }
        .required-mark { color: #e53e3e; }
        .hint { font-size: 12px; color: #888; margin-top: 4px; }
        .buttons { display: flex; gap: 12px; margin-top: 28px; }
        .btn {
            height: 44px; padding: 0 24px; border: none; border-radius: 6px;
            cursor: pointer; font-size: 14px; font-weight: 500; text-decoration: none;
            display: inline-flex; align-items: center; justify-content: center;
        }
        .btn-save { background: #2563eb; color: white; }
        .btn-save:hover { background: #1d4ed8; }
        .btn-back { background: #e5e7eb; color: #333; }
        .btn-back:hover { background: #d1d5db; }
        .flash-error {
            background: #fef2f2; border: 1px solid #fca5a5; color: #b91c1c;
            padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px;
        }
        @media (max-width: 600px) {
            .form-grid { grid-template-columns: 1fr; }
            .buttons { flex-direction: column; }
        }
    </style>

<div class="container">

    <div class="header">
        <div>
            <h1>➕ Thêm khách hàng mới</h1>
        </div>
        <a class="back" href="${pageContext.request.contextPath}/manage/sales/customers">← Quay lại danh sách</a>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="flash-error">${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <div class="box">
        <h2>Thông tin khách hàng</h2>

        <form method="post" action="${pageContext.request.contextPath}/manage/sales/customer-add">

            <div class="form-grid">

                <div class="form-group">
                    <label for="fullName">Họ và tên <span class="required-mark">*</span></label>
                    <input type="text" id="fullName" name="fullName" placeholder="Nhập họ và tên" required>
                </div>

                <div class="form-group">
                    <label for="phone">Số điện thoại <span class="required-mark">*</span></label>
                    <input type="tel" id="phone" name="phone" placeholder="Ví dụ: 0901234567" required>
                </div>

                <div class="form-group">
                    <label for="email">Email <span class="required-mark">*</span></label>
                    <input type="email" id="email" name="email" placeholder="Ví dụ: khachhang@gmail.com" required>
                    <div class="hint">Email sẽ được dùng để đăng nhập. Mật khẩu mặc định: <strong>123456</strong></div>
                </div>

                <div class="form-group">
                    <label for="address">Địa chỉ</label>
                    <input type="text" id="address" name="address" placeholder="Số nhà, phố, quận/huyện...">
                </div>

            </div>

            <div class="buttons">
                <button type="submit" class="btn btn-save">💾 Lưu khách hàng</button>
                <a href="${pageContext.request.contextPath}/manage/sales/customers" class="btn btn-back">Hủy</a>
            </div>

        </form>
    </div>

</div>

