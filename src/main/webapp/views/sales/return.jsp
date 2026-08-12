<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Yêu cầu đổi trả</title>

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
            width: 280px;
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

        .return-id {
            color: #2563eb;
            font-weight: bold;
        }

        /* ================= STATUS ================= */

        .status {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 12px;
            white-space: nowrap;
        }

        .pending {
            background: #fff3cd;
            color: #856404;
        }

        .approved {
            background: #d1e7dd;
            color: #0f5132;
        }

        .rejected {
            background: #f8d7da;
            color: #842029;
        }

        .completed {
            background: #cfe2ff;
            color: #084298;
        }

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
        <h1>Yêu cầu đổi trả</h1>
        <p>Quản lý và xử lý các yêu cầu đổi trả của khách hàng.</p>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#d1fae5; border:1px solid #6ee7b7; color:#065f46; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
            ✅ ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session"/>
    </c:if>


    <!-- ================= FILTER ================= -->

    <div class="filter-box">

        <form method="get"
              action="${pageContext.request.contextPath}/manage/sales/returns"
              class="filter-form">

            <input type="text"
                   name="keyword"
                   value="${keyword}"
                   placeholder="Nhập mã đơn hoặc tên khách hàng...">


            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="Chờ xử lý" ${status == 'Chờ xử lý' ? 'selected' : ''}>Chờ xử lý</option>
                <option value="Đã duyệt" ${status == 'Đã duyệt' ? 'selected' : ''}>Đã duyệt</option>
                <option value="Đã nhận hàng" ${status == 'Đã nhận hàng' ? 'selected' : ''}>Đã nhận hàng</option>
                <option value="Đã đổi hàng" ${status == 'Đã đổi hàng' ? 'selected' : ''}>Đã đổi hàng</option>
                <option value="Đã hoàn tiền" ${status == 'Đã hoàn tiền' ? 'selected' : ''}>Đã hoàn tiền</option>
                <option value="Từ chối" ${status == 'Từ chối' ? 'selected' : ''}>Từ chối</option>
            </select>


            <button type="submit"
                    class="btn btn-search">

                Tìm kiếm

            </button>


            <a href="${pageContext.request.contextPath}/manage/sales/returns"
               class="btn btn-reset">

                Đặt lại

            </a>

        </form>

    </div>


    <!-- ================= TABLE ================= -->

    <div class="table-box">

        <div class="table-header">

            <h2>
                Danh sách yêu cầu
            </h2>

            <span>

                Tổng:

                <strong>
                    ${returns.size()}
                </strong>

                yêu cầu

            </span>

        </div>


        <table>

            <thead>

            <tr>

                <th>Mã yêu cầu</th>

                <th>Mã đơn</th>

                <th>Khách hàng</th>

                <th>Lý do</th>

                <th>Ngày yêu cầu</th>

                <th>Trạng thái</th>

                <th>Thao tác</th>

            </tr>

            </thead>


            <tbody>

            <c:choose>

                <c:when test="${not empty returns}">

                    <c:forEach var="item"
                               items="${returns}">

                        <tr>

                            <!-- MÃ YÊU CẦU -->

                            <td>

                                <span class="return-id">

                                    #${item.id}

                                </span>

                            </td>


                            <!-- MÃ ĐƠN -->

                            <td>

                                #${item.orderId}

                            </td>


                            <!-- KHÁCH HÀNG -->

                            <td>

                                ${item.customerName}

                            </td>


                            <!-- LÝ DO -->

                            <td>

                                ${item.reason}

                            </td>


                            <!-- NGÀY -->

                            <td>

                                ${item.requestDate}

                            </td>


                            <!-- TRẠNG THÁI -->

                            <td>

                                <c:choose>

                                    <c:when test="${item.status == 'Chờ xử lý' or item.status == 'PENDING'}">
                                        <span class="status pending" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#fef3c7;color:#92400e;">Chờ xử lý</span>
                                    </c:when>

                                    <c:when test="${item.status == 'Đã duyệt' or item.status == 'APPROVED'}">
                                        <span class="status approved" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#cfe2ff;color:#084298;">Đã duyệt</span>
                                    </c:when>

                                    <c:when test="${item.status == 'Đã nhận hàng' or item.status == 'RECEIVED'}">
                                        <span class="status received" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#e0f2fe;color:#0369a1;">Đã nhận hàng</span>
                                    </c:when>

                                    <c:when test="${item.status == 'Đã đổi hàng' or item.status == 'REPLACED'}">
                                        <span class="status completed" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#d1fae5;color:#065f46;">Đã đổi hàng</span>
                                    </c:when>

                                    <c:when test="${item.status == 'Đã hoàn tiền' or item.status == 'REFUNDED'}">
                                        <span class="status refunded" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#ecfdf5;color:#047857;">Đã hoàn tiền</span>
                                    </c:when>

                                    <c:when test="${item.status == 'Từ chối' or item.status == 'Đã từ chối' or item.status == 'REJECTED'}">
                                        <span class="status rejected" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#fee2e2;color:#991b1b;">Từ chối</span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="status" style="display:inline-block;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#f1f5f9;color:#64748b;">${item.status}</span>
                                    </c:otherwise>

                                </c:choose>

                            </td>


                            <!-- THAO TÁC -->
                            <td>
                                <div style="display:flex; gap:8px; align-items:center;">
                                    <button type="button" class="btn" style="background:#f1f5f9; color:#334155; padding:6px 12px; font-size:13px; font-weight:600; cursor:pointer; border:1px solid #cbd5e1; border-radius:6px; display:inline-flex; align-items:center; height:32px;"
                                            data-id="${item.id}"
                                            data-order-id="${item.orderId}"
                                            data-order-code="${item.orderCode}"
                                            data-customer="${item.customerName}"
                                            data-phone="${item.customerPhone}"
                                            data-email="${item.customerEmail}"
                                            data-reason="${item.reason}"
                                            data-date="${item.requestDate}"
                                            data-status="${item.status}"
                                            data-product="${item.productName}"
                                            data-quantity="${item.quantity}"
                                            data-evidence="${item.evidenceImg}"
                                            data-order-date="${item.orderDate}"
                                            data-total-price="${item.totalPrice}"
                                            data-within-period="${item.isWithinPeriod}"
                                            data-correct-condition="${item.isCorrectCondition}"
                                            data-store-error="${item.isStoreError}"
                                            onclick="openDetailModal(event)">
                                        Xem chi tiết
                                    </button>
                                    
                                    <c:choose>
                                        <c:when test="${item.status == 'Chờ xử lý' or item.status == 'PENDING'}">
                                            <button type="button" class="btn" style="background:#059669; color:white; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    onclick="submitReturnAction(${item.id}, 'Đã duyệt')">
                                                Duyệt
                                            </button>
                                            <button type="button" class="btn" style="background:#dc2626; color:white; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    onclick="submitReturnAction(${item.id}, 'Từ chối')">
                                                Từ chối
                                            </button>
                                        </c:when>
                                        <c:when test="${item.status == 'Đã duyệt' or item.status == 'APPROVED'}">
                                            <button type="button" class="btn" style="background:#0284c7; color:white; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    onclick="submitReturnAction(${item.id}, 'Đã nhận hàng')">
                                                Xác nhận đã nhận hàng
                                            </button>
                                        </c:when>
                                        <c:when test="${item.status == 'Đã nhận hàng' or item.status == 'RECEIVED'}">
                                            <button type="button" class="btn" style="background:#16a34a; color:white; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    onclick="submitReturnAction(${item.id}, 'Đã đổi hàng')">
                                                Xác nhận đổi hàng
                                            </button>
                                            <button type="button" class="btn" style="background:#0d9488; color:white; padding:0 12px; font-size:13px; font-weight:600; cursor:pointer; border:none; border-radius:6px; height:32px;"
                                                    onclick="submitReturnAction(${item.id}, 'Đã hoàn tiền')">
                                                Hoàn tiền
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

                            Chưa có yêu cầu đổi trả.

                        </td>

                    </tr>

                </c:otherwise>

            </c:choose>

            </tbody>

        </table>

    </div>

    <!-- ================= MODAL XEM CHI TIẾT YÊU CẦU ĐỔI TRẢ ================= -->
    <div id="detail-modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center; padding:15px;">
        <div style="background:white; border-radius:12px; max-width:600px; width:100%; box-shadow:0 10px 25px rgba(0,0,0,0.25); padding:25px; position:relative; max-height:90vh; overflow-y:auto;">
            <span style="position:absolute; top:15px; right:15px; font-size:24px; color:#aaa; cursor:pointer; font-weight:bold;" onclick="closeModal()">&times;</span>
            <h2 style="margin-top:0; margin-bottom:20px; font-size:20px; color:#1e293b; border-bottom:1px solid #e2e8f0; padding-bottom:12px;">Chi tiết yêu cầu đổi trả <span id="modal-id-text" style="color:#2563eb;"></span></h2>
            
            <div style="display:grid; grid-template-columns:1fr; gap:16px; margin-bottom:20px; font-size:14px; text-align:left;">
                
                <!-- KHÁCH HÀNG & ĐƠN HÀNG -->
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:15px; background:#f8fafc; padding:15px; border-radius:8px; border:1px solid #e2e8f0;">
                    <div>
                        <strong style="color:#0f172a; display:block; margin-bottom:6px; font-size:13px; text-transform:uppercase;">👤 Khách hàng</strong>
                        <span id="modal-customer" style="font-weight:bold; color:#1e293b;"></span><br>
                        SĐT: <span id="modal-phone"></span><br>
                        Email: <span id="modal-email" style="font-size:13px;"></span>
                    </div>
                    <div>
                        <strong style="color:#0f172a; display:block; margin-bottom:6px; font-size:13px; text-transform:uppercase;">📦 Đơn hàng liên quan</strong>
                        Mã đơn: <span id="modal-order-code" style="font-weight:bold; color:#2563eb;"></span><br>
                        Ngày mua: <span id="modal-order-date"></span><br>
                        Tổng tiền: <span id="modal-total-price" style="font-weight:bold;"></span> ₫
                    </div>
                </div>
                
                <!-- SẢN PHẨM CẦN ĐỔI TRẢ -->
                <div>
                    <strong style="color:#0f172a; display:block; margin-bottom:6px; font-size:13px; text-transform:uppercase;">🛍️ Sản phẩm yêu cầu đổi/trả</strong>
                    <table style="width:100%; border-collapse:collapse; font-size:13px;">
                        <thead>
                            <tr style="background:#f1f5f9;">
                                <th style="padding:8px; border:1px solid #e2e8f0; text-align:left;">Tên sản phẩm</th>
                                <th style="padding:8px; border:1px solid #e2e8f0; text-align:center; width:80px;">Số lượng</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td id="modal-product" style="padding:8px; border:1px solid #e2e8f0; font-weight:bold; color:#1e293b;"></td>
                                <td id="modal-quantity" style="padding:8px; border:1px solid #e2e8f0; text-align:center; font-weight:bold;"></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- LÝ DO & BẰNG CHỨNG -->
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:15px;">
                    <div>
                        <strong style="color:#0f172a; display:block; margin-bottom:4px; font-size:13px; text-transform:uppercase;">💬 Lý do đổi trả</strong>
                        <p id="modal-reason" style="margin:0; background:#fffbeb; padding:10px; border-radius:6px; border:1px solid #fef3c7; color:#92400e; font-style:italic; line-height:1.4; min-height:60px;"></p>
                    </div>
                    <div>
                        <strong style="color:#0f172a; display:block; margin-bottom:4px; font-size:13px; text-transform:uppercase;">📸 Hình ảnh / Bằng chứng</strong>
                        <p id="modal-evidence" style="margin:0; background:#f1f5f9; padding:10px; border-radius:6px; border:1px solid #e2e8f0; color:#475569; font-style:italic; line-height:1.4; min-height:60px;"></p>
                    </div>
                </div>

                <!-- ĐIỀU KIỆN ĐỔI TRẢ -->
                <div style="background:#f0fdf4; padding:15px; border-radius:8px; border:1px solid #bbf7d0;">
                    <strong style="color:#14532d; display:block; margin-bottom:8px; font-size:13px; text-transform:uppercase;">📝 Kiểm tra điều kiện đổi trả</strong>
                    <div style="display:grid; grid-template-columns:1fr; gap:6px; font-size:13px;">
                        <div>⏱️ <strong>Thời hạn đổi trả:</strong> <span id="modal-within-period" style="font-weight:bold; color:#15803d;"></span></div>
                        <div>📦 <strong>Trạng thái sản phẩm:</strong> <span id="modal-correct-condition" style="font-weight:bold; color:#15803d;"></span></div>
                        <div>⚠️ <strong>Lỗi từ phía cửa hàng/sản phẩm:</strong> <span id="modal-store-error" style="font-weight:bold; color:#15803d;"></span></div>
                    </div>
                </div>

            </div>
            
            <div style="display:flex; justify-content:flex-end; gap:10px; border-top:1px solid #e2e8f0; padding-top:15px;">
                <button type="button" class="btn" style="background:#e2e8f0; color:#334155; height:36px; padding:0 18px; cursor:pointer; border:none; border-radius:6px; font-weight:bold;" onclick="closeModal()">Đóng</button>
            </div>
        </div>
    </div>

    <!-- HIDDEN FORM FOR ACTIONS -->
    <form id="action-form" method="post" action="${pageContext.request.contextPath}/manage/sales/returns" style="display:none;">
        <input type="hidden" name="id" id="action-id">
        <input type="hidden" name="status" id="action-status">
    </form>

    <script>
        function submitReturnAction(id, status) {
            let message = 'Bạn có chắc chắn muốn chuyển trạng thái yêu cầu này thành "' + status + '"?';
            if (confirm(message)) {
                document.getElementById('action-id').value = id;
                document.getElementById('action-status').value = status;
                document.getElementById('action-form').submit();
            }
        }
        
        function openDetailModal(event) {
            var btn = event.currentTarget;
            var id = btn.getAttribute('data-id');
            var orderId = btn.getAttribute('data-order-id');
            var orderCode = btn.getAttribute('data-order-code');
            var customer = btn.getAttribute('data-customer');
            var phone = btn.getAttribute('data-phone');
            var email = btn.getAttribute('data-email');
            var reason = btn.getAttribute('data-reason');
            var date = btn.getAttribute('data-date');
            var status = btn.getAttribute('data-status');
            var product = btn.getAttribute('data-product');
            var quantity = btn.getAttribute('data-quantity');
            var evidence = btn.getAttribute('data-evidence');
            var orderDate = btn.getAttribute('data-order-date');
            var totalPrice = btn.getAttribute('data-total-price');
            var withinPeriod = btn.getAttribute('data-within-period');
            var correctCondition = btn.getAttribute('data-correct-condition');
            var storeError = btn.getAttribute('data-store-error');
            
            document.getElementById('modal-id-text').innerText = '#' + id;
            document.getElementById('modal-customer').innerText = customer;
            document.getElementById('modal-phone').innerText = phone || 'Chưa rõ';
            document.getElementById('modal-email').innerText = email || 'Chưa rõ';
            document.getElementById('modal-order-code').innerText = orderCode ? '#' + orderCode : '#' + orderId;
            document.getElementById('modal-order-date').innerText = orderDate || 'Chưa rõ';
            document.getElementById('modal-total-price').innerText = totalPrice || '0';
            document.getElementById('modal-product').innerText = product || 'Sản phẩm khác';
            document.getElementById('modal-quantity').innerText = quantity || '1';
            document.getElementById('modal-reason').innerText = reason;
            document.getElementById('modal-evidence').innerText = evidence || 'Không có bằng chứng đính kèm';
            document.getElementById('modal-within-period').innerText = withinPeriod || 'Chưa xác định';
            document.getElementById('modal-correct-condition').innerText = correctCondition || 'Chưa xác định';
            document.getElementById('modal-store-error').innerText = storeError || 'Không';
            
            document.getElementById('detail-modal').style.display = 'flex';
        }
        
        function closeModal() {
            document.getElementById('detail-modal').style.display = 'none';
        }
    </script>

</div>

</body>

</html>