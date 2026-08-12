<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Quản lý vận chuyển</title>

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <style>

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f5f6fa;
            color: #333;
        }

        .container {
            padding: 30px;
        }

        .header {
            margin-bottom: 25px;
        }

        .header h1 {
            margin: 0;
            font-size: 28px;
        }

        .header p {
            margin-top: 8px;
            color: #777;
        }

        /* ================= FILTER ================= */

        .filter-box {
            background: white;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 20px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
        }

        .filter-form {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        .filter-form input,
        .filter-form select {
            height: 42px;
            padding: 0 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
        }

        .filter-form input {
            width: 250px;
        }

        .filter-form select {
            width: 180px;
        }

        .btn {
            height: 42px;
            padding: 0 18px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
        }

        .btn-search {
            background: #2563eb;
            color: white;
        }

        .btn-reset {
            background: #e5e7eb;
            color: #333;
        }

        /* ================= TABLE ================= */

        .table-box {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
            overflow-x: auto;
        }

        .table-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 18px;
        }

        .table-header h2 {
            margin: 0;
            font-size: 20px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 900px;
        }

        th,
        td {
            padding: 14px 12px;
            border-bottom: 1px solid #eee;
            text-align: left;
        }

        th {
            background: #fafafa;
            color: #666;
            font-size: 14px;
        }

        td {
            font-size: 14px;
        }

        tr:hover {
            background: #fafafa;
        }

        .order-id {
            color: #2563eb;
            font-weight: bold;
            text-decoration: none;
        }

        /* ================= STATUS ================= */

        .status {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 12px;
            white-space: nowrap;
        }

        .processing {
            background: #fff3cd;
            color: #856404;
        }

        .shipping {
            background: #cfe2ff;
            color: #084298;
        }

        .completed {
            background: #d1e7dd;
            color: #0f5132;
        }

        .cancelled {
            background: #f8d7da;
            color: #842029;
        }

        /* ================= ACTION ================= */

        .action {
            color: #2563eb;
            text-decoration: none;
            font-weight: 500;
        }

        .action:hover {
            text-decoration: underline;
        }

        .empty {
            text-align: center;
            padding: 40px;
            color: #777;
        }

        /* ================= RESPONSIVE ================= */

        @media (max-width: 700px) {

            .container {
                padding: 15px;
            }

            .filter-form input,
            .filter-form select,
            .btn {
                width: 100%;
            }

        }

    </style>

</head>

<body>

<div class="container">

    <!-- ================= HEADER ================= -->

    <div class="header">

        <h1>
            Quản lý vận chuyển
        </h1>

        <p>
            Theo dõi các đơn hàng đang được giao đến khách hàng.
        </p>

    </div>


    <!-- ================= FILTER ================= -->

    <div class="filter-box">

        <form method="get"
              action="${pageContext.request.contextPath}/manage/sales/delivery"
              class="filter-form">

            <input type="text"
                   name="keyword"
                   value="${keyword}"
                   placeholder="Nhập mã đơn, tên KH, SĐT, địa chỉ...">


            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="Chờ giao" ${status == 'Chờ giao' ? 'selected' : ''}>Chờ giao</option>
                <option value="Đang giao" ${status == 'Đang giao' ? 'selected' : ''}>Đang giao</option>
                <option value="Giao thành công" ${status == 'Giao thành công' ? 'selected' : ''}>Giao thành công</option>
                <option value="Giao thất bại" ${status == 'Giao thất bại' ? 'selected' : ''}>Giao thất bại</option>
            </select>


            <button type="submit"
                    class="btn btn-search">

                Tìm kiếm

            </button>


            <a href="${pageContext.request.contextPath}/manage/sales/delivery"
               class="btn btn-reset">

                Đặt lại

            </a>

        </form>

    </div>


    <!-- ================= DANH SÁCH ================= -->

    <div class="table-box">

        <div class="table-header">

            <h2>
                Danh sách vận chuyển
            </h2>

            <span>

                Tổng:

                <strong>
                    ${orders.size()}
                </strong>

                đơn

            </span>

        </div>


        <table>

            <thead>

            <tr>

                <th>Mã đơn</th>

                <th>Khách hàng</th>

                <th>Số điện thoại</th>

                <th>Địa chỉ</th>

                <th>Tổng tiền</th>

                <th>Trạng thái</th>

                <th>Thao tác</th>

            </tr>

            </thead>


            <tbody>

            <c:choose>

                <c:when test="${not empty orders}">

                    <c:forEach var="order"
                               items="${orders}">

                        <tr>

                            <!-- MÃ ĐƠN -->

                            <td>

                                <a class="order-id"
                                   href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}">

                                    #${order.id}

                                </a>

                            </td>


                            <!-- KHÁCH HÀNG -->

                            <td>

                                ${order.customerName}

                            </td>


                            <!-- SỐ ĐIỆN THOẠI -->

                            <td>

                                ${order.customerPhone}

                            </td>


                            <!-- ĐỊA CHỈ -->

                            <td>

                                ${order.shippingAddress}

                            </td>


                            <!-- TỔNG TIỀN -->

                            <td>

                                <strong>

                                    ${order.total} ₫

                                </strong>

                            </td>


                            <!-- TRẠNG THÁI -->

                            <td>

                                <c:choose>

                                    <c:when test="${order.statusCode == 'CONFIRMED' or order.statusCode == 'Chờ giao'}">
                                        <span class="status pending" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#fef3c7;color:#92400e;">Chờ giao</span>
                                    </c:when>

                                    <c:when test="${order.statusCode == 'SHIPPING' or order.statusCode == 'Đang giao'}">
                                        <span class="status shipping" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#dbeafe;color:#1e40af;">Đang giao</span>
                                    </c:when>

                                    <c:when test="${order.statusCode == 'COMPLETED' or order.statusCode == 'Giao thành công' or order.statusCode == 'Hoàn thành'}">
                                        <span class="status completed" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#d1fae5;color:#065f46;">Giao thành công</span>
                                    </c:when>

                                    <c:when test="${order.statusCode == 'CANCELLED' or order.statusCode == 'Giao thất bại' or order.statusCode == 'Đã hủy'}">
                                        <span class="status cancelled" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#fee2e2;color:#991b1b;">Giao thất bại</span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="status" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#f1f5f9;color:#64748b;">${order.statusCode}</span>
                                    </c:otherwise>

                                </c:choose>

                            </td>


                            <!-- THAO TÁC -->

                            <td>

                                <div style="display:flex; gap:8px; align-items:center;">
                                    <a class="btn" style="background:#f1f5f9; color:#334155; padding:0 12px; font-size:13px; font-weight:600; text-decoration:none; border-radius:6px; border:1px solid #cbd5e1; display:inline-flex; align-items:center; height:32px;"
                                       href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}">
                                        Xem đơn
                                    </a>
                                    
                                    <c:choose>
                                        <c:when test="${order.statusCode == 'CONFIRMED' or order.statusCode == 'Chờ giao'}">
                                            <button type="button" class="btn" style="background:#e2e8f0; color:#1e293b; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    data-id="${order.id}"
                                                    data-code="${order.code}"
                                                    data-customer="${order.customerName}"
                                                    data-phone="${order.customerPhone}"
                                                    data-address="${order.shippingAddress}"
                                                    data-status="${order.statusCode}"
                                                    data-total="${order.total}"
                                                    onclick="openUpdateModal(event)">
                                                Cập nhật
                                            </button>
                                            <button type="button" class="btn" style="background:#2563eb; color:white; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    onclick="quickUpdateStatus(${order.id}, 'SHIPPING')">
                                                Bắt đầu giao
                                            </button>
                                        </c:when>
                                        <c:when test="${order.statusCode == 'SHIPPING' or order.statusCode == 'Đang giao'}">
                                            <button type="button" class="btn" style="background:#e2e8f0; color:#1e293b; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    data-id="${order.id}"
                                                    data-code="${order.code}"
                                                    data-customer="${order.customerName}"
                                                    data-phone="${order.customerPhone}"
                                                    data-address="${order.shippingAddress}"
                                                    data-status="${order.statusCode}"
                                                    data-total="${order.total}"
                                                    onclick="openUpdateModal(event)">
                                                Cập nhật
                                            </button>
                                            <button type="button" class="btn" style="background:#16a34a; color:white; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    onclick="quickUpdateStatus(${order.id}, 'COMPLETED')">
                                                Xác nhận giao thành công
                                            </button>
                                        </c:when>
                                    </c:choose>
                                </div>

                            </td>

                        </tr>

                    </c:forEach>

                </c:when>


                <c:otherwise>

                    <tr>

                        <td colspan="7"
                            class="empty">

                            Không có đơn hàng vận chuyển.

                        </td>

                    </tr>

                </c:otherwise>

            </c:choose>

            </tbody>

        </table>

    </div>

    <!-- ================= MODAL CẬP NHẬT GIAO HÀNG ================= -->
    <div id="update-delivery-modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center; padding:15px;">
        <div style="background:white; border-radius:12px; max-width:500px; width:100%; box-shadow:0 10px 25px rgba(0,0,0,0.25); padding:25px; position:relative;">
            <span style="position:absolute; top:15px; right:15px; font-size:24px; color:#aaa; cursor:pointer; font-weight:bold;" onclick="closeUpdateModal()">&times;</span>
            <h2 style="margin-top:0; margin-bottom:20px; font-size:20px; color:#1e293b; border-bottom:1px solid #e2e8f0; padding-bottom:12px;">Cập nhật vận chuyển <span id="modal-order-code" style="color:#2563eb;"></span></h2>
            
            <form method="post" action="${pageContext.request.contextPath}/manage/sales/order-update-shipping" style="text-align:left;">
                <input type="hidden" name="id" id="modal-order-id">
                <input type="hidden" name="redirect" value="delivery">
                
                <div style="margin-bottom:12px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Người nhận:</label>
                    <input type="text" name="customerName" id="modal-customer-input" required style="width:100%; height:38px; padding:0 10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none;">
                </div>
                
                <div style="margin-bottom:12px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Số điện thoại:</label>
                    <input type="text" name="phone" id="modal-phone-input" required style="width:100%; height:38px; padding:0 10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none;">
                </div>
                
                <div style="margin-bottom:12px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Địa chỉ:</label>
                    <input type="text" name="shippingAddress" id="modal-address-input" required style="width:100%; height:38px; padding:0 10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none;">
                </div>
                
                <div style="margin-bottom:20px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Trạng thái vận chuyển:</label>
                    <select name="status" id="modal-status-input" style="width:100%; height:38px; padding:0 10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none; background:white;">
                        <option value="CONFIRMED">Chờ giao</option>
                        <option value="SHIPPING">Đang giao</option>
                        <option value="COMPLETED">Giao thành công</option>
                        <option value="CANCELLED">Giao thất bại</option>
                    </select>
                </div>
                
                <div style="display:flex; justify-content:flex-end; gap:10px; border-top:1px solid #e2e8f0; padding-top:15px;">
                    <button type="submit" class="btn" style="background:#2563eb; color:white; height:36px; padding:0 18px; font-weight:bold; cursor:pointer; border:none; border-radius:6px;">💾 Lưu thay đổi</button>
                    <button type="button" class="btn" style="background:#e2e8f0; color:#334155; height:36px; padding:0 18px; cursor:pointer; border:none; border-radius:6px;" onclick="closeUpdateModal()">Đóng</button>
                </div>
            </form>
        </div>
    </div>

    <!-- HIDDEN FORM FOR QUICK STATE UPDATE -->
    <form id="quick-update-form" method="post" action="${pageContext.request.contextPath}/manage/sales/order-update-shipping" style="display:none;">
        <input type="hidden" name="id" id="quick-order-id">
        <input type="hidden" name="status" id="quick-order-status">
        <input type="hidden" name="redirect" value="delivery">
    </form>

    <script>
        function openUpdateModal(event) {
            var btn = event.currentTarget;
            var id = btn.getAttribute('data-id');
            var code = btn.getAttribute('data-code');
            var customer = btn.getAttribute('data-customer');
            var phone = btn.getAttribute('data-phone');
            var address = btn.getAttribute('data-address');
            var status = btn.getAttribute('data-status');
            
            document.getElementById('modal-order-id').value = id;
            document.getElementById('modal-order-code').innerText = code ? '#' + code : '#' + id;
            document.getElementById('modal-customer-input').value = customer || '';
            document.getElementById('modal-phone-input').value = phone || '';
            document.getElementById('modal-address-input').value = address || '';
            document.getElementById('modal-status-input').value = status || 'CONFIRMED';
            
            document.getElementById('update-delivery-modal').style.display = 'flex';
        }
        
        function closeUpdateModal() {
            document.getElementById('update-delivery-modal').style.display = 'none';
        }
        
        function quickUpdateStatus(id, newStatus) {
            let msg = '';
            if (newStatus === 'SHIPPING') {
                msg = 'Bắt đầu giao đơn hàng này?';
            } else if (newStatus === 'COMPLETED') {
                msg = 'Xác nhận đơn hàng đã được giao thành công?';
            }
            
            if (confirm(msg)) {
                document.getElementById('quick-order-id').value = id;
                document.getElementById('quick-order-status').value = newStatus;
                document.getElementById('quick-update-form').submit();
            }
        }
    </script>

</div>

</body>

</html>