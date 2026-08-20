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
        input, select, textarea {
            width: 100%; padding: 10px 12px; border: 1px solid #ddd;
            border-radius: 6px; font-size: 14px; outline: none; transition: border-color .2s;
            height: 42px;
        }
        input:focus, select:focus, textarea:focus { border-color: #2563eb; }
        .readonly { background: #f3f4f6; }
        .required-mark { color: #e53e3e; }
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
            <h1>️ Chỉnh sửa đơn hàng #${order.id}</h1>
        </div>
        <a class="back" href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}">← Quay lại chi tiết</a>
    </div>

    <div class="box">
        <h2>Thông tin đơn đặt hàng</h2>

        <form method="post" action="${pageContext.request.contextPath}/manage/sales/order-edit">
            <input type="hidden" name="id" value="${order.id}">

            <div class="form-grid">

                <div class="form-group">
                    <label>Mã đơn hàng</label>
                    <input type="text" value="${order.code}" class="readonly" readonly>
                </div>

                <div class="form-group">
                    <label>Ngày đặt</label>
                    <input type="text" value="${order.orderDate}" class="readonly" readonly>
                </div>

                <div class="form-group">
                    <label for="customerName">Tên khách hàng <span class="required-mark">*</span></label>
                    <input type="text" id="customerName" name="customerName" value="${order.customerName}" placeholder="Nhập tên người nhận" required>
                </div>

                <div class="form-group">
                    <label for="phone">Số điện thoại <span class="required-mark">*</span></label>
                    <input type="tel" id="phone" name="phone" value="${order.phone}" placeholder="Ví dụ: 0988666888" required oninput="this.value = this.value.replace(/[^0-9]/g, '')" pattern="[0-9]{9,11}" title="Số điện thoại phải từ 9 đến 11 chữ số và chỉ gồm số">
                </div>

                <div class="form-group full">
                    <label for="shippingAddress">Địa chỉ giao hàng <span class="required-mark">*</span></label>
                    <input type="text" id="shippingAddress" name="shippingAddress" value="${order.shippingAddress}" placeholder="Số nhà, đường, quận/huyện, tỉnh/thành phố..." required>
                </div>

                <div class="form-group">
                    <label for="totalPrice">Tổng tiền đơn hàng (đ) <span class="required-mark">*</span></label>
                    <input type="number" id="totalPrice" name="totalPrice" value="${order.totalAsDouble}" placeholder="Ví dụ: 6500000" min="0" required>
                </div>

                <div class="form-group">
                    <label for="status">Trạng thái đơn hàng <span class="required-mark">*</span></label>
                    <select id="status" name="status" required>
                        <option value="PENDING" ${order.statusCode == 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                        <option value="CONFIRMED" ${order.statusCode == 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
                        <option value="PACKING" ${order.statusCode == 'PACKING' ? 'selected' : ''}>Đang đóng gói</option>
                        <option value="SHIPPING" ${order.statusCode == 'SHIPPING' ? 'selected' : ''}>Đang giao hàng</option>
                        <option value="DELIVERED" ${order.statusCode == 'DELIVERED' ? 'selected' : ''}>Đã giao hàng</option>
                        <option value="COMPLETED" ${order.statusCode == 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
                        <option value="CANCELLED" ${order.statusCode == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                        <option value="RETURNED" ${order.statusCode == 'RETURNED' ? 'selected' : ''}>Đã đổi trả</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="paymentStatus">Trạng thái thanh toán <span class="required-mark">*</span></label>
                    <select id="paymentStatus" name="paymentStatus" required>
                        <option value="UNPAID" ${order.paymentStatus == 'UNPAID' ? 'selected' : ''}>Chưa thanh toán</option>
                        <option value="PAID" ${order.paymentStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
                    </select>
                </div>

            </div>

            <div class="buttons">
                <button type="submit" class="btn btn-save"> Lưu thay đổi</button>
                <a href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}" class="btn btn-back">Hủy</a>
            </div>

        </form>
    </div>

</div>

