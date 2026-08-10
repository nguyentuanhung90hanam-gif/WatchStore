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
                   placeholder="Nhập mã đơn hoặc tên khách hàng...">


            <select name="status">

                <option value="">
                    -- Tất cả --
                </option>

                <option value="Đang xử lý"
                    ${status == 'Đang xử lý' ? 'selected' : ''}>
                    Đang xử lý
                </option>

                <option value="Đang giao"
                    ${status == 'Đang giao' ? 'selected' : ''}>
                    Đang giao
                </option>

                <option value="Hoàn thành"
                    ${status == 'Hoàn thành' ? 'selected' : ''}>
                    Hoàn thành
                </option>

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

                                    <c:when test="${order.status == 'Đang xử lý'}">

                                        <span class="status processing">

                                            ${order.status}

                                        </span>

                                    </c:when>


                                    <c:when test="${order.status == 'Đang giao'}">

                                        <span class="status shipping">

                                            ${order.status}

                                        </span>

                                    </c:when>


                                    <c:when test="${order.status == 'Hoàn thành'}">

                                        <span class="status completed">

                                            ${order.status}

                                        </span>

                                    </c:when>


                                    <c:when test="${order.status == 'Đã hủy'}">

                                        <span class="status cancelled">

                                            ${order.status}

                                        </span>

                                    </c:when>


                                    <c:otherwise>

                                        <span class="status">

                                            ${order.status}

                                        </span>

                                    </c:otherwise>

                                </c:choose>

                            </td>


                            <!-- THAO TÁC -->

                            <td>

                                <a class="action"
                                   href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}">

                                    Xem đơn hàng

                                </a>

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

</div>

</body>

</html>