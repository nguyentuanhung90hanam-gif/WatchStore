<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>





    <meta charset="UTF-8">
    <title>Quản lý đơn hàng</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

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

        /* SEARCH */
        .search-box {
            background: white;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 20px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
        }

        .search-form {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        .search-form input,
        .search-form select {
            height: 42px;
            padding: 0 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
        }

        .search-form input {
            width: 280px;
        }

        .search-form select {
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

        /* TABLE */
        .table-box {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
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
            min-width: 800px;
        }

        th, td {
            padding: 14px 12px;
            border-bottom: 1px solid #eee;
            text-align: left;
        }

        th {
            color: #666;
            font-size: 14px;
            background: #fafafa;
        }

        td {
            font-size: 14px;
        }

        tr:hover {
            background: #fafafa;
        }

        .order-id {
            font-weight: bold;
            color: #2563eb;
            text-decoration: none;
        }

        /* STATUS */
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

        /* RESPONSIVE */
        @media (max-width: 700px) {
            .container {
                padding: 15px;
            }

            .search-form input,
            .search-form select {
                width: 100%;
            }

            .btn {
                width: 100%;
            }
        }
    </style>




<div class="container">

    <!-- HEADER -->
    <div class="header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px;">
        <div>
            <h1 style="margin: 0;">Quản lý đơn hàng</h1>
            <p style="margin: 8px 0 0 0; color: #777;">Xem, tìm kiếm và theo dõi các đơn hàng của khách hàng.</p>
        </div>
        <c:if test="${sessionScope.user.role == 'ADMIN' || (not empty sessionScope.userPermissions && (sessionScope.userPermissions.contains('ORDER_CREATE') || sessionScope.userPermissions.contains('SALES_ORDER')))}">
            <a href="${pageContext.request.contextPath}/manage/sales/order-add" class="btn btn-search" style="text-decoration: none; font-weight: bold; background: #2563eb; color: white;">
                ➕ Thêm đơn hàng
            </a>
        </c:if>
    </div>

    <!-- STATS -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 15px; margin-bottom: 25px;">
        <div style="background: white; padding: 18px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.06); border-left: 4px solid #2563eb;">
            <span style="color: #666; font-size: 13px; font-weight: 500;">Tổng đơn hàng</span>
            <div style="font-size: 24px; font-weight: bold; margin-top: 8px; color: #2563eb;">${totalCount}</div>
        </div>
        <div style="background: white; padding: 18px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.06); border-left: 4px solid #d97706;">
            <span style="color: #666; font-size: 13px; font-weight: 500;">Chờ xác nhận</span>
            <div style="font-size: 24px; font-weight: bold; margin-top: 8px; color: #d97706;">${pendingCount}</div>
        </div>
        <div style="background: white; padding: 18px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.06); border-left: 4px solid #3b82f6;">
            <span style="color: #666; font-size: 13px; font-weight: 500;">Đang giao</span>
            <div style="font-size: 24px; font-weight: bold; margin-top: 8px; color: #3b82f6;">${shippingCount}</div>
        </div>
        <div style="background: white; padding: 18px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.06); border-left: 4px solid #059669;">
            <span style="color: #666; font-size: 13px; font-weight: 500;">Hoàn thành</span>
            <div style="font-size: 24px; font-weight: bold; margin-top: 8px; color: #059669;">${completedCount}</div>
        </div>
        <div style="background: white; padding: 18px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.06); border-left: 4px solid #dc2626;">
            <span style="color: #666; font-size: 13px; font-weight: 500;">Đã hủy</span>
            <div style="font-size: 24px; font-weight: bold; margin-top: 8px; color: #dc2626;">${cancelledCount}</div>
        </div>
    </div>

    <!-- SEARCH -->
    <div class="search-box">
        <form method="get"
              action="${pageContext.request.contextPath}/manage/sales/orders"
              class="search-form">

            <!-- Tìm theo khách hàng -->
            <input type="text"
                   name="keyword"
                   value="${param.keyword}"
                   placeholder="Nhập tên KH, mã đơn, SĐT...">

            <!-- Lọc trạng thái -->
            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                <option value="CONFIRMED" ${param.status == 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
                <option value="PACKING" ${param.status == 'PACKING' ? 'selected' : ''}>Đang đóng gói</option>
                <option value="SHIPPING" ${param.status == 'SHIPPING' ? 'selected' : ''}>Đang giao</option>
                <option value="DELIVERED" ${param.status == 'DELIVERED' ? 'selected' : ''}>Đã giao</option>
                <option value="COMPLETED" ${param.status == 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
                <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
            </select>

            <!-- Lọc theo ngày -->
            <div style="display: flex; gap: 8px; align-items: center; background: white; border: 1px solid #ddd; border-radius: 6px; padding: 0 10px; height: 42px;">
                <span style="font-size: 13px; color: #666;">Từ:</span>
                <input type="date" name="fromDate" value="${fromDate}" style="border: none; padding: 0; height: auto; width: 125px; font-size: 13px;">
                <span style="font-size: 13px; color: #666;">Đến:</span>
                <input type="date" name="toDate" value="${toDate}" style="border: none; padding: 0; height: auto; width: 125px; font-size: 13px;">
            </div>

            <!-- Nút tìm kiếm & Đặt lại -->
            <button type="submit" class="btn btn-search">Tìm kiếm</button>
            <a href="${pageContext.request.contextPath}/manage/sales/orders" class="btn btn-reset">Đặt lại</a>
        </form>
    </div>

    <!-- TABLE -->
    <div class="table-box">
        <div class="table-header">
            <h2>Danh sách đơn hàng</h2>
            <span>Tổng: <strong>${not empty orders ? orders.size() : 0}</strong> đơn hàng</span>
        </div>

        <table>
            <thead>
            <tr>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th>Số điện thoại</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th>Ngày đặt</th>
                <th>Thao tác</th>
            </tr>
            </thead>

            <tbody>
            <c:choose>
                <c:when test="${not empty orders}">
                    <c:forEach var="order" items="${orders}">
                        <tr>
                            <!-- MÃ ĐƠN -->
                            <td>
                                <a class="order-id" href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}">
                                    #${order.id}
                                </a>
                            </td>

                            <!-- KHÁCH HÀNG -->
                            <td>${order.customerName}</td>

                            <!-- SỐ ĐIỆN THOẠI -->
                            <td>${order.customerPhone}</td>

                            <!-- TỔNG TIỀN -->
                            <td>
                                <strong>
                                    <fmt:formatNumber value="${order.total}" pattern="#,##0" /> ₫
                                </strong>
                            </td>

                            <!-- TRẠNG THÁI -->
                            <td>
                                <div>
                                    <c:choose>
                                        <c:when test="${order.statusCode == 'PENDING'}">
                                            <span class="status processing">Chờ xử lý</span>
                                        </c:when>
                                        <c:when test="${order.statusCode == 'CONFIRMED'}">
                                            <span class="status processing">Đã xác nhận</span>
                                        </c:when>
                                        <c:when test="${order.statusCode == 'PACKING'}">
                                            <span class="status processing">Đang đóng gói</span>
                                        </c:when>
                                        <c:when test="${order.statusCode == 'SHIPPING'}">
                                            <span class="status shipping">Đang giao</span>
                                        </c:when>
                                        <c:when test="${order.statusCode == 'DELIVERED'}">
                                            <span class="status shipping">Đã giao</span>
                                        </c:when>
                                        <c:when test="${order.statusCode == 'COMPLETED'}">
                                            <span class="status completed">Hoàn thành</span>
                                        </c:when>
                                        <c:when test="${order.statusCode == 'CANCELLED'}">
                                            <span class="status cancelled">Đã hủy</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status">${order.statusCode}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div style="margin-top: 5px;">
                                    <c:choose>
                                        <c:when test="${order.paymentStatus == 'PAID'}">
                                            <span style="background: #d1fae5; color: #065f46; padding: 3px 8px; border-radius: 10px; font-size: 11px; font-weight: 500; display: inline-block;">Đã thanh toán</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="background: #fee2e2; color: #991b1b; padding: 3px 8px; border-radius: 10px; font-size: 11px; font-weight: 500; display: inline-block;">Chưa thanh toán</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </td>

                            <!-- NGÀY -->
                            <td>${order.orderDate}</td>

                            <!-- THAO TÁC -->
                            <td>
                                <div style="display: flex; gap: 8px; align-items: center; flex-wrap: nowrap;">
                                    <a class="action" href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}" style="color: #2563eb;">
                                        Chi tiết
                                    </a>
                                    <c:if test="${order.statusCode == 'PENDING' && (sessionScope.user.role == 'ADMIN' || (not empty sessionScope.userPermissions && (sessionScope.userPermissions.contains('ORDER_APPROVE') || sessionScope.userPermissions.contains('SALES_ORDER'))))}">
                                        <a class="action" href="javascript:void(0);" onclick="confirmAction(${order.id}, 'confirm')" style="color: #059669; font-weight: bold;">
                                            | Xác nhận
                                        </a>
                                    </c:if>
                                    <c:if test="${order.statusCode != 'COMPLETED' && (sessionScope.user.role == 'ADMIN' || (not empty sessionScope.userPermissions && (sessionScope.userPermissions.contains('ORDER_EDIT') || sessionScope.userPermissions.contains('SALES_ORDER'))))}">
                                        <a class="action" href="${pageContext.request.contextPath}/manage/sales/order-edit?id=${order.id}" style="color: #d97706;">
                                            | Sửa
                                        </a>
                                    </c:if>
                                    <c:if test="${order.statusCode != 'COMPLETED' && order.statusCode != 'CANCELLED' && (sessionScope.user.role == 'ADMIN' || (not empty sessionScope.userPermissions && (sessionScope.userPermissions.contains('ORDER_APPROVE') || sessionScope.userPermissions.contains('SALES_ORDER'))))}">
                                        <a class="action" href="javascript:void(0);" onclick="confirmAction(${order.id}, 'cancel')" style="color: #dc2626;">
                                            | Hủy
                                        </a>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <tr>
                        <td colspan="7" class="empty">Không tìm thấy đơn hàng.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>

</div>

<c:if test="${not empty sessionScope.flash}">
    <div style="position: fixed; bottom: 24px; right: 24px; background: #16a34a; color: #fff; padding: 14px 22px; border-radius: 8px; font-size: 14px; box-shadow: 0 4px 14px rgba(0,0,0,.15); z-index: 9999;">
        ${sessionScope.flash}
    </div>
    <c:remove var="flash" scope="session"/>
</c:if>

<script>
    function confirmAction(id, action) {
        let message = '';
        let url = '';
        if (action === 'delete') {
            message = 'Bạn có chắc chắn muốn xóa đơn hàng này? Thao tác này sẽ xóa toàn bộ chi tiết sản phẩm liên quan.';
            url = '${pageContext.request.contextPath}/manage/sales/order-delete';
        } else if (action === 'confirm') {
            message = 'Bạn có chắc chắn muốn xác nhận đơn hàng này?';
            url = '${pageContext.request.contextPath}/manage/sales/order-confirm';
        } else if (action === 'cancel') {
            message = 'Bạn có chắc chắn muốn hủy đơn hàng này?';
            url = '${pageContext.request.contextPath}/manage/sales/order-cancel';
        }
        
        if (confirm(message)) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = url;
            
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'id';
            input.value = id;
            form.appendChild(input);
            
            document.body.appendChild(form);
            form.submit();
        }
    }
</script>
