<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Chi tiết khách hàng</title>

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
            max-width: 1000px;
            margin: auto;
            padding: 30px;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }

        .header h1 {
            margin: 0;
            font-size: 28px;
        }

        .back {
            color: #2563eb;
            text-decoration: none;
        }

        .box {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
            margin-bottom: 20px;
        }

        .box h2 {
            margin-top: 0;
            margin-bottom: 22px;
            font-size: 20px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .form-group {
            margin-bottom: 5px;
        }

        .form-group.full {
            grid-column: 1 / -1;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
        }

        input {
            width: 100%;
            height: 42px;
            padding: 0 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
        }

        input:focus {
            outline: none;
            border-color: #2563eb;
        }

        .readonly {
            background: #f3f4f6;
        }

        .buttons {
            display: flex;
            gap: 10px;
            margin-top: 25px;
        }

        .btn {
            height: 42px;
            padding: 0 20px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
        }

        .btn-save {
            background: #2563eb;
            color: white;
        }

        .btn-save:hover {
            background: #1d4ed8;
        }

        .btn-back {
            background: #e5e7eb;
            color: #333;
        }

        .info {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }

        .info-item {
            padding: 15px;
            background: #f8f9fa;
            border-radius: 8px;
        }

        .info-label {
            color: #777;
            font-size: 13px;
            margin-bottom: 5px;
        }

        .info-value {
            font-weight: 500;
        }

        @media (max-width: 700px) {

            .container {
                padding: 15px;
            }

            .header {
                display: block;
            }

            .back {
                display: inline-block;
                margin-top: 12px;
            }

            .form-grid,
            .info {
                grid-template-columns: 1fr;
            }

        }

    </style>

</head>

<body>

<div class="container">

    <!-- ================= HEADER ================= -->

    <div class="header">

        <div>

            <h1>
                Chi tiết khách hàng
            </h1>

        </div>

        <a class="back"
           href="${pageContext.request.contextPath}/manage/sales/customers">

            ← Quay lại danh sách

        </a>

    </div>


    <!-- ================= THÔNG TIN HIỆN TẠI ================= -->

    <div class="box">

        <h2>
            Thông tin khách hàng
        </h2>

        <div class="info">

            <div class="info-item">

                <div class="info-label">
                    Mã khách hàng
                </div>

                <div class="info-value">
                    #${customer.id}
                </div>

            </div>


            <div class="info-item">

                <div class="info-label">
                    Tài khoản
                </div>

                <div class="info-value">
                    ${customer.username}
                </div>

            </div>


            <div class="info-item">

                <div class="info-label">
                    Email
                </div>

                <div class="info-value">
                    ${customer.email}
                </div>

            </div>


            <div class="info-item">

                <div class="info-label">
                    Số điện thoại
                </div>

                <div class="info-value">
                    ${customer.phone}
                </div>

            </div>

            <!-- Tổng số đơn -->
            <div class="info-item" style="background: #eff6ff; border-left: 4px solid #3b82f6;">
                <div class="info-label" style="color: #1e3a8a; font-weight: bold;">Tổng số đơn hàng</div>
                <div class="info-value" style="font-size: 18px; font-weight: bold; color: #1d4ed8; margin-top: 5px;">
                    ${totalOrdersCount} đơn
                </div>
            </div>

            <!-- Tổng chi tiêu -->
            <div class="info-item" style="background: #ecfdf5; border-left: 4px solid #10b981;">
                <div class="info-label" style="color: #064e3b; font-weight: bold;">Tổng chi tiêu tích lũy</div>
                <div class="info-value" style="font-size: 18px; font-weight: bold; color: #047857; margin-top: 5px;">
                    <fmt:formatNumber value="${totalAmountSpent}" pattern="#,##0" /> ₫
                </div>
            </div>

        </div>

    </div>


    <!-- ================= CẬP NHẬT ================= -->

    <div class="box">

        <h2>
            Cập nhật thông tin
        </h2>

        <form method="post"
              action="${pageContext.request.contextPath}/manage/sales/customer-detail">

            <input type="hidden"
                   name="id"
                   value="${customer.id}">


            <div class="form-grid">


                <!-- MÃ KH -->

                <div class="form-group">

                    <label>
                        Mã khách hàng
                    </label>

                    <input type="text"
                           value="${customer.id}"
                           class="readonly"
                           readonly>

                </div>


                <!-- USERNAME -->

                <div class="form-group">

                    <label>
                        Tên tài khoản
                    </label>

                    <input type="text"
                           value="${customer.username}"
                           class="readonly"
                           readonly>

                </div>


                <!-- HỌ TÊN -->

                <div class="form-group">

                    <label for="fullName">
                        Họ và tên
                    </label>

                    <input type="text"
                           id="fullName"
                           name="fullName"
                           value="${customer.fullName}"
                           required>

                </div>


                <!-- EMAIL -->

                <div class="form-group">

                    <label for="email">
                        Email
                    </label>

                    <input type="email"
                           id="email"
                           name="email"
                           value="${customer.email}"
                           required>

                </div>


                <!-- SỐ ĐIỆN THOẠI -->

                <div class="form-group">

                    <label for="phone">
                        Số điện thoại
                    </label>

                    <input type="text"
                           id="phone"
                           name="phone"
                           value="${customer.phone}"
                           required>

                </div>


                <!-- ĐỊA CHỈ -->

                <div class="form-group">

                    <label for="address">
                        Địa chỉ
                    </label>

                    <input type="text"
                           id="address"
                           name="address"
                           value="${customer.address}"
                           required>

                </div>

            </div>


            <!-- BUTTON -->

            <div class="buttons">

                <button type="submit"
                        class="btn btn-save">

                    Lưu thay đổi

                </button>

                <a href="${pageContext.request.contextPath}/manage/sales/customers"
                   class="btn btn-back">

                    Hủy

                </a>

            </div>

        </form>

    </div>

    <%-- ================= LỊCH SỬ MUA HÀNG ================= --%>

    <div class="box" style="margin-top:20px;">

        <h2>🛍 Lịch sử mua hàng</h2>

        <c:choose>
            <c:when test="${not empty customerOrders}">
                <table style="width:100%;border-collapse:collapse;font-size:14px;">
                    <thead>
                        <tr style="background:#fafafa;">
                            <th style="padding:12px 10px;border-bottom:2px solid #eee;text-align:left;color:#666;">Mã đơn</th>
                            <th style="padding:12px 10px;border-bottom:2px solid #eee;text-align:left;color:#666;">Ngày đặt</th>
                            <th style="padding:12px 10px;border-bottom:2px solid #eee;text-align:left;color:#666;">Tổng tiền</th>
                            <th style="padding:12px 10px;border-bottom:2px solid #eee;text-align:left;color:#666;">Trạng thái</th>
                            <th style="padding:12px 10px;border-bottom:2px solid #eee;text-align:left;color:#666;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="order" items="${customerOrders}">
                            <tr style="border-bottom:1px solid #f0f0f0;">
                                <td style="padding:12px 10px;font-weight:600;color:#2563eb;">${order.code}</td>
                                <td style="padding:12px 10px;color:#555;">${order.orderDate}</td>
                                <td style="padding:12px 10px;font-weight:600;">
                                    <fmt:formatNumber value="${order.totalPrice}" type="number" groupingUsed="true" var="formattedTotal" />
                                    ${order.totalPrice} ₫
                                </td>
                                <td style="padding:12px 10px;">
                                    <c:choose>
                                        <c:when test="${order.status == 'COMPLETED' or order.status == 'Hoàn thành'}">
                                            <span style="background:#d1fae5;color:#065f46;padding:4px 10px;border-radius:12px;font-size:12px;">Hoàn thành</span>
                                        </c:when>
                                        <c:when test="${order.status == 'CANCELLED' or order.status == 'Đã hủy'}">
                                            <span style="background:#fee2e2;color:#991b1b;padding:4px 10px;border-radius:12px;font-size:12px;">Đã hủy</span>
                                        </c:when>
                                        <c:when test="${order.status == 'SHIPPING' or order.status == 'Đang giao'}">
                                            <span style="background:#dbeafe;color:#1e40af;padding:4px 10px;border-radius:12px;font-size:12px;">Đang giao</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="background:#fef3c7;color:#92400e;padding:4px 10px;border-radius:12px;font-size:12px;">${order.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="padding:12px 10px;">
                                    <a href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}"
                                       style="color:#2563eb;text-decoration:none;font-weight:500;">Xem chi tiết →</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <div style="text-align:center;padding:30px;color:#aaa;">
                    <div style="font-size:40px;margin-bottom:10px;">📦</div>
                    <p>Khách hàng này chưa có đơn hàng nào.</p>
                </div>
            </c:otherwise>
        </c:choose>

    </div>

</div>

<c:if test="${not empty sessionScope.flash}">
    <div style="position:fixed;bottom:20px;right:20px;background:#16a34a;color:white;padding:14px 22px;border-radius:8px;font-size:14px;box-shadow:0 4px 12px rgba(0,0,0,.15);z-index:9999;">
        ${sessionScope.flash}
    </div>
    <c:remove var="flash" scope="session"/>
</c:if>

</body>
</html>