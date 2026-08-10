<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">

<head>
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
</head>

<body>

<div class="container">

    <!-- HEADER -->
    <div class="header">
        <h1>Quản lý đơn hàng</h1>
        <p>Xem, tìm kiếm và theo dõi các đơn hàng của khách hàng.</p>
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
                                <c:choose>
                                    <c:when test="${order.status == 'PENDING'}">
                                        <span class="status processing">Chờ xử lý</span>
                                    </c:when>
                                    <c:when test="${order.status == 'CONFIRMED'}">
                                        <span class="status processing">Đã xác nhận</span>
                                    </c:when>
                                    <c:when test="${order.status == 'PACKING'}">
                                        <span class="status processing">Đang đóng gói</span>
                                    </c:when>
                                    <c:when test="${order.status == 'SHIPPING'}">
                                        <span class="status shipping">Đang giao</span>
                                    </c:when>
                                    <c:when test="${order.status == 'DELIVERED'}">
                                        <span class="status shipping">Đã giao</span>
                                    </c:when>
                                    <c:when test="${order.status == 'COMPLETED'}">
                                        <span class="status completed">Hoàn thành</span>
                                    </c:when>
                                    <c:when test="${order.status == 'CANCELLED'}">
                                        <span class="status cancelled">Đã hủy</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status">${order.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <!-- NGÀY -->
                            <td>${order.orderDate}</td>

                            <!-- THAO TÁC -->
                            <td>
                                <a class="action" href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}">
                                    Xem chi tiết
                                </a>
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

</body>
</html>