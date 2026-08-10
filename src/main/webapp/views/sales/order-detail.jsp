<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn hàng</title>

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
            max-width: 1100px;
            margin: auto;
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
            text-decoration: none;
            color: #2563eb;
        }

        .grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 20px;
        }

        .box {
            background: white;
            padding: 22px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
            margin-bottom: 20px;
        }

        .box h2 {
            margin-top: 0;
            margin-bottom: 20px;
            font-size: 20px;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            gap: 20px;
            padding: 12px 0;
            border-bottom: 1px solid #eee;
        }

        .info-row:last-child {
            border-bottom: none;
        }

        .label {
            color: #777;
        }

        .value {
            font-weight: 500;
            text-align: right;
        }

        .total {
            font-size: 22px;
            font-weight: bold;
        }

        .status {
            display: inline-block;
            padding: 7px 13px;
            border-radius: 20px;
            font-size: 13px;
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

        .form-group {
            margin-bottom: 18px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
        }

        select {
            width: 100%;
            height: 42px;
            padding: 0 10px;
            border: 1px solid #ddd;
            border-radius: 6px;
            background: white;
        }

        .btn {
            width: 100%;
            height: 42px;
            border: none;
            border-radius: 6px;
            background: #2563eb;
            color: white;
            cursor: pointer;
            font-size: 14px;
        }

        .btn:hover {
            background: #1d4ed8;
        }

        .order-products {
            width: 100%;
            border-collapse: collapse;
        }

        .order-products th,
        .order-products td {
            padding: 13px 10px;
            border-bottom: 1px solid #eee;
            text-align: left;
        }

        .order-products th {
            color: #666;
            font-size: 14px;
        }

        .empty {
            text-align: center;
            color: #777;
            padding: 30px;
        }

        @media (max-width: 800px) {

            .container {
                padding: 15px;
            }

            .grid {
                grid-template-columns: 1fr;
            }

            .header {
                display: block;
            }

            .back {
                display: inline-block;
                margin-top: 12px;
            }
        }

    </style>

</head>

<body>

<div class="container">

    <!-- ================= HEADER ================= -->

    <div class="header">

        <div>
            <h1>Chi tiết đơn hàng #${order.id}</h1>
        </div>

        <a class="back"
           href="${pageContext.request.contextPath}/manage/sales/orders">

            ← Quay lại danh sách

        </a>

    </div>


    <!-- ================= NỘI DUNG ================= -->

    <div class="grid">


        <!-- ================= THÔNG TIN ĐƠN ================= -->

        <div>

            <div class="box">

                <h2>Thông tin đơn hàng</h2>

                <div class="info-row">

                    <span class="label">
                        Mã đơn hàng
                    </span>

                    <span class="value">
                        #${order.id}
                    </span>

                </div>

                <div class="info-row">

                    <span class="label">
                        Ngày đặt
                    </span>

                    <span class="value">
                        ${order.orderDate}
                    </span>

                </div>

                <div class="info-row">

                    <span class="label">
                        Trạng thái
                    </span>

                    <span class="value">

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
                            <c:when test="${order.status == 'RETURNED'}">
                                <span class="status cancelled">Đã đổi trả</span>
                            </c:when>
                            <c:otherwise>
                                <span class="status">${order.status}</span>
                            </c:otherwise>
                        </c:choose>

                    </span>

                </div>

                <div class="info-row">

                    <span class="label">
                        Tổng tiền
                    </span>

                    <span class="value total">
                        ${order.total} ₫
                    </span>

                </div>

            </div>


            <!-- ================= KHÁCH HÀNG ================= -->

            <div class="box">

                <h2>Thông tin khách hàng</h2>

                <div class="info-row">

                    <span class="label">
                        Họ tên
                    </span>

                    <span class="value">
                        ${order.customerName}
                    </span>

                </div>

                <div class="info-row">

                    <span class="label">
                        Số điện thoại
                    </span>

                    <span class="value">
                        ${order.customerPhone}
                    </span>

                </div>

                <div class="info-row">

                    <span class="label">
                        Mã khách hàng
                    </span>

                    <span class="value">
                        #${order.customerId}
                    </span>

                </div>

            </div>


            <!-- ================= SẢN PHẨM ================= -->

            <div class="box">

                <h2>Sản phẩm trong đơn</h2>

                <table class="order-products">

                    <thead>

                    <tr>

                        <th>Sản phẩm</th>

                        <th>Số lượng</th>

                        <th>Đơn giá</th>

                        <th>Thành tiền</th>

                    </tr>

                    </thead>

                    <tbody>

                    <c:choose>
                        <c:when test="${not empty orderItems}">
                            <c:forEach var="item" items="${orderItems}">
                                <tr>
                                    <td>${item.productName}<br/><small style="color:#888;">${item.variantName}</small></td>
                                    <td style="text-align:center;">${item.quantity}</td>
                                    <td>${item.price} ₫</td>
                                    <td style="font-weight:600;">${item.lineTotal} ₫</td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="4" class="empty">Không có sản phẩm nào trong đơn hàng này.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>

                    </tbody>

                </table>

            </div>

        </div>


        <!-- ================= CẬP NHẬT ================= -->

        <div>

            <div class="box">

                <h2>Cập nhật trạng thái</h2>

                <form method="post"
                      action="${pageContext.request.contextPath}/manage/sales/order-detail">

                    <input type="hidden"
                           name="id"
                           value="${order.id}">

                    <div class="form-group">

                        <label for="status">
                            Trạng thái đơn hàng
                        </label>

                        <select id="status" name="status" required>
                            <option value="PENDING" ${order.status == 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                            <option value="CONFIRMED" ${order.status == 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
                            <option value="PACKING" ${order.status == 'PACKING' ? 'selected' : ''}>Đang đóng gói</option>
                            <option value="SHIPPING" ${order.status == 'SHIPPING' ? 'selected' : ''}>Đang giao hàng</option>
                            <option value="DELIVERED" ${order.status == 'DELIVERED' ? 'selected' : ''}>Đã giao hàng</option>
                            <option value="COMPLETED" ${order.status == 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
                            <option value="CANCELLED" ${order.status == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                            <option value="RETURNED" ${order.status == 'RETURNED' ? 'selected' : ''}>Đã đổi trả</option>
                        </select>

                    </div>

                    <button type="submit"
                            class="btn">

                        Cập nhật trạng thái

                    </button>

                </form>

            </div>


            <!-- ================= THAO TÁC ================= -->

            <div class="box">

                <h2>Thao tác</h2>

                <p>

                    <a href="${pageContext.request.contextPath}/manage/sales/delivery">

                        🚚 Quản lý vận chuyển

                    </a>

                </p>

                <p>

                    <a href="${pageContext.request.contextPath}/manage/sales/returns">

                        🔄 Xử lý đổi trả

                    </a>

                </p>

            </div>

        </div>

    </div>

</div>

<c:if test="${not empty sessionScope.flash}">
    <div style="position:fixed;bottom:24px;right:24px;background:#16a34a;color:#fff;padding:14px 22px;border-radius:8px;font-size:14px;box-shadow:0 4px 14px rgba(0,0,0,.15);z-index:9999;">
        ${sessionScope.flash}
    </div>
    <c:remove var="flash" scope="session"/>
</c:if>

</body>
</html>