<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Báo cáo bán hàng</title>

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
            align-items: end;
        }

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 7px;
        }

        .form-group label {
            font-size: 13px;
            color: #666;
        }

        .filter-form input,
        .filter-form select {
            height: 42px;
            padding: 0 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
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

        /* ================= CARDS ================= */

        .cards {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 18px;
            margin-bottom: 20px;
        }

        .card {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
        }

        .card-title {
            color: #777;
            font-size: 14px;
            margin-bottom: 10px;
        }

        .card-value {
            font-size: 25px;
            font-weight: bold;
        }

        /* ================= REPORT ================= */

        .report-box {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
            margin-bottom: 20px;
            overflow-x: auto;
        }

        .report-box h2 {
            margin-top: 0;
            font-size: 20px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 700px;
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

        .money {
            font-weight: bold;
        }

        .empty {
            text-align: center;
            padding: 40px;
            color: #777;
        }

        /* ================= RESPONSIVE ================= */

        @media (max-width: 1000px) {

            .cards {
                grid-template-columns: repeat(2, 1fr);
            }

        }

        @media (max-width: 700px) {

            .container {
                padding: 15px;
            }

            .cards {
                grid-template-columns: 1fr;
            }

            .filter-form {
                display: block;
            }

            .form-group {
                margin-bottom: 10px;
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
            Báo cáo bán hàng
        </h1>

        <p>
            Theo dõi tình hình doanh thu và đơn hàng.
        </p>

    </div>


    <!-- ================= FILTER ================= -->

    <div class="filter-box">

        <form method="get"
              action="${pageContext.request.contextPath}/manage/sales/report"
              class="filter-form">


            <div class="form-group">

                <label>
                    Từ ngày
                </label>

                <input type="date"
                       name="fromDate"
                       value="${fromDate}">

            </div>


            <div class="form-group">

                <label>
                    Đến ngày
                </label>

                <input type="date"
                       name="toDate"
                       value="${toDate}">

            </div>


            <div class="form-group">

                <label>
                    Trạng thái đơn
                </label>

            <select name="status">
                <option value="">Tất cả</option>
                <option value="COMPLETED" ${param.status == 'COMPLETED' or status == 'COMPLETED' or status == 'Hoàn thành' ? 'selected' : ''}>Hoàn thành</option>
                <option value="SHIPPING" ${param.status == 'SHIPPING' or status == 'SHIPPING' or status == 'Đang giao' ? 'selected' : ''}>Đang giao</option>
                <option value="PENDING" ${param.status == 'PENDING' or status == 'PENDING' or status == 'Đang xử lý' ? 'selected' : ''}>Đang xử lý</option>
                <option value="CANCELLED" ${param.status == 'CANCELLED' or status == 'CANCELLED' or status == 'Đã hủy' ? 'selected' : ''}>Đã hủy</option>
            </select>

            </div>


            <button type="submit"
                    class="btn btn-search">

                Xem báo cáo

            </button>


            <a href="${pageContext.request.contextPath}/manage/sales/report"
               class="btn btn-reset">

                Đặt lại

            </a>

        </form>

    </div>


    <!-- ================= SUMMARY ================= -->

    <div class="cards">


        <div class="card">

            <div class="card-title">
                Tổng đơn hàng
            </div>

            <div class="card-value">
                ${totalOrders}
            </div>

        </div>


        <div class="card">

            <div class="card-title">
                Đơn hoàn thành
            </div>

            <div class="card-value">
                ${completedOrders}
            </div>

        </div>


        <div class="card">

            <div class="card-title">
                Đơn đang giao
            </div>

            <div class="card-value">
                ${shippingOrders}
            </div>

        </div>


        <div class="card">

            <div class="card-title">
                Doanh thu
            </div>

            <div class="card-value">

                ${totalRevenue} ₫

            </div>

        </div>

    </div>


    <!-- ================= REPORT TABLE ================= -->

    <div class="report-box">

        <h2>
            Chi tiết báo cáo
        </h2>


        <table>

            <thead>

            <tr>

                <th>Mã đơn</th>

                <th>Khách hàng</th>

                <th>Ngày đặt</th>

                <th>Trạng thái</th>

                <th>Tổng tiền</th>

            </tr>

            </thead>


            <tbody>

            <c:choose>

                <c:when test="${not empty reportOrders}">

                    <c:forEach var="order"
                               items="${reportOrders}">

                        <tr>

                            <td>
                                #${order.id}
                            </td>

                            <td>
                                ${order.customerName}
                            </td>

                            <td>
                                ${order.orderDate}
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${order.status == 'COMPLETED' or order.status == 'Hoàn thành'}">
                                        <span style="color:#16a34a;font-weight:600;">Hoàn thành</span>
                                    </c:when>
                                    <c:when test="${order.status == 'SHIPPING' or order.status == 'DELIVERED' or order.status == 'Đang giao'}">
                                        <span style="color:#2563eb;font-weight:600;">Đang giao</span>
                                    </c:when>
                                    <c:when test="${order.status == 'PENDING' or order.status == 'CONFIRMED' or order.status == 'PACKING' or order.status == 'Đang xử lý'}">
                                        <span style="color:#d97706;font-weight:600;">Đang xử lý</span>
                                    </c:when>
                                    <c:when test="${order.status == 'CANCELLED' or order.status == 'Đã hủy'}">
                                        <span style="color:#dc2626;font-weight:600;">Đã hủy</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span>${order.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td class="money">
                                ${order.total} ₫
                            </td>

                        </tr>

                    </c:forEach>

                </c:when>


                <c:otherwise>

                    <tr>

                        <td colspan="5"
                            class="empty">

                            Không có dữ liệu báo cáo.

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