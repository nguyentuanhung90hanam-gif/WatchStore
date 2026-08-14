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
        @media print {
            body {
                background: white !important;
                color: black !important;
                font-size: 12px !important;
            }
            /* Hide non-printable elements */
            aside, .back, .header button, .box form, .box:has(form), .box:has(button), .btn, hr {
                display: none !important;
            }
            .container {
                max-width: 100% !important;
                padding: 0 !important;
                margin: 0 !important;
            }
            .grid {
                display: block !important;
            }
            .box {
                box-shadow: none !important;
                border: 1px solid #ccc !important;
                border-radius: 0 !important;
                margin-bottom: 15px !important;
                page-break-inside: avoid;
                padding: 15px !important;
            }
        }

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

    <div class="header" style="display:flex; justify-content:space-between; align-items:center;">

        <div>
            <h1 style="display:inline-block; margin-right:15px; vertical-align:middle; margin-top:0; margin-bottom:0;">Chi tiết đơn hàng #${order.id}</h1>
            <button type="button" class="btn" style="background:#4b5563; width:auto; height:36px; padding:0 12px; font-weight:600; display:inline-flex; align-items:center; gap:5px; vertical-align:middle; cursor:pointer;" onclick="window.print()">
                🖨️ In đơn hàng
            </button>
            <button type="button" class="btn" style="background:#d97706; color:white; width:auto; height:36px; padding:0 12px; font-weight:600; display:inline-flex; align-items:center; gap:5px; vertical-align:middle; cursor:pointer;" onclick="window.open('${pageContext.request.contextPath}/manage/sales/pos/print?id=${order.id}', '_blank', 'width=450,height=650')">
                📄 In hóa đơn bán lẻ
            </button>
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

            <!-- PROGRESS TIMELINE -->
            <div class="box" style="margin-bottom: 20px;">
                <h2 style="margin-top:0; margin-bottom:20px;">Tiến trình đơn hàng</h2>
                <div style="display: flex; justify-content: space-between; align-items: center; position: relative; padding: 10px 0;">
                    <!-- Line behind steps -->
                    <div style="position: absolute; top: 30px; left: 8%; right: 8%; height: 4px; background: #e5e7eb; z-index: 1;"></div>
                    
                    <!-- Steps -->
                    <!-- Step 1: Chờ xử lý -->
                    <div style="text-align: center; flex: 1; z-index: 2; position: relative;">
                        <div style="width: 40px; height: 40px; border-radius: 50%; background: #2563eb; color: white; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px auto; font-weight: bold; font-size: 15px; border: 4px solid #fff; box-shadow: 0 0 0 2px #2563eb;">
                            1
                        </div>
                        <span style="font-size: 12px; font-weight: 600; color: #333;">Chờ xử lý</span>
                    </div>
                    
                    <!-- Step 2: Xác nhận -->
                    <c:set var="step2Active" value="${order.statusCode != 'PENDING'}" />
                    <div style="text-align: center; flex: 1; z-index: 2; position: relative;">
                        <div style="width: 40px; height: 40px; border-radius: 50%; background: ${step2Active ? '#2563eb' : '#e5e7eb'}; color: ${step2Active ? 'white' : '#666'}; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px auto; font-weight: bold; font-size: 15px; border: 4px solid #fff; box-shadow: 0 0 0 2px ${step2Active ? '#2563eb' : '#e5e7eb'};">
                            2
                        </div>
                        <span style="font-size: 12px; font-weight: ${step2Active ? '600' : 'normal'}; color: ${step2Active ? '#333' : '#999'};">Đã xác nhận</span>
                    </div>
                    
                    <!-- Step 3: Đóng gói -->
                    <c:set var="step3Active" value="${order.statusCode != 'PENDING' && order.statusCode != 'CONFIRMED'}" />
                    <div style="text-align: center; flex: 1; z-index: 2; position: relative;">
                        <div style="width: 40px; height: 40px; border-radius: 50%; background: ${step3Active ? '#2563eb' : '#e5e7eb'}; color: ${step3Active ? 'white' : '#666'}; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px auto; font-weight: bold; font-size: 15px; border: 4px solid #fff; box-shadow: 0 0 0 2px ${step3Active ? '#2563eb' : '#e5e7eb'};">
                            3
                        </div>
                        <span style="font-size: 12px; font-weight: ${step3Active ? '600' : 'normal'}; color: ${step3Active ? '#333' : '#999'};">Đóng gói</span>
                    </div>
                    
                    <!-- Step 4: Đang giao -->
                    <c:set var="step4Active" value="${order.statusCode != 'PENDING' && order.statusCode != 'CONFIRMED' && order.statusCode != 'PACKING'}" />
                    <div style="text-align: center; flex: 1; z-index: 2; position: relative;">
                        <div style="width: 40px; height: 40px; border-radius: 50%; background: ${step4Active ? '#2563eb' : '#e5e7eb'}; color: ${step4Active ? 'white' : '#666'}; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px auto; font-weight: bold; font-size: 15px; border: 4px solid #fff; box-shadow: 0 0 0 2px ${step4Active ? '#2563eb' : '#e5e7eb'};">
                            4
                        </div>
                        <span style="font-size: 12px; font-weight: ${step4Active ? '600' : 'normal'}; color: ${step4Active ? '#333' : '#999'};">Đang giao</span>
                    </div>
                    
                    <!-- Step 5: Kết thúc -->
                    <c:set var="isCompleted" value="${order.statusCode == 'COMPLETED'}" />
                    <c:set var="isCancelled" value="${order.statusCode == 'CANCELLED'}" />
                    <c:set var="isReturned" value="${order.statusCode == 'RETURNED'}" />
                    <c:set var="step5Color" value="${isCompleted ? '#059669' : (isCancelled ? '#dc2626' : (isReturned ? '#d97706' : '#e5e7eb'))}" />
                    <c:set var="step5Text" value="${isCancelled ? 'Đã hủy' : (isReturned ? 'Đổi trả' : 'Hoàn thành')}" />
                    <c:set var="step5Active" value="${isCompleted || isCancelled || isReturned}" />
                    <div style="text-align: center; flex: 1; z-index: 2; position: relative;">
                        <div style="width: 40px; height: 40px; border-radius: 50%; background: ${step5Active ? step5Color : '#e5e7eb'}; color: ${step5Active ? 'white' : '#666'}; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px auto; font-weight: bold; font-size: 15px; border: 4px solid #fff; box-shadow: 0 0 0 2px ${step5Active ? step5Color : '#e5e7eb'};">
                            5
                        </div>
                        <span style="font-size: 12px; font-weight: ${step5Active ? '600' : 'normal'}; color: ${step5Active ? step5Color : '#999'};">${step5Text}</span>
                    </div>
                </div>
            </div>

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
                            <c:when test="${order.statusCode == 'RETURNED'}">
                                <span class="status cancelled">Đã đổi trả</span>
                            </c:when>
                            <c:otherwise>
                                <span class="status">${order.statusCode}</span>
                            </c:otherwise>
                        </c:choose>

                    </span>

                </div>

                <div class="info-row">
                    <span class="label">
                        Thanh toán
                    </span>
                    <span class="value">
                        <c:choose>
                            <c:when test="${order.paymentStatus == 'PAID'}">
                                <span style="background: #d1fae5; color: #065f46; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600;">Đã thanh toán</span>
                            </c:when>
                            <c:otherwise>
                                <span style="background: #fee2e2; color: #991b1b; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600;">Chưa thanh toán</span>
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


            <!-- ================= THÔNG TIN GIAO HÀNG ================= -->
            <div class="box">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px; border-bottom: 1px solid #eee; padding-bottom: 10px;">
                    <h2 style="margin:0;">Thông tin giao nhận hàng</h2>
                    <c:if test="${order.statusCode != 'COMPLETED' && order.statusCode != 'CANCELLED'}">
                        <button type="button" class="btn" style="background:#e5e7eb; color:#333; width:auto; height:32px; padding:0 12px; font-size:12px; font-weight:600; cursor:pointer;" onclick="toggleEditShipping()">
                            ✏️ Sửa thông tin nhận
                        </button>
                    </c:if>
                </div>
                
                <div id="shipping-display">
                    <div class="info-row">
                        <span class="label">Người nhận hàng</span>
                        <span class="value" style="font-weight:bold;">${order.customerName}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Số điện thoại nhận</span>
                        <span class="value" style="font-weight:bold;">${order.phone}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Địa chỉ giao hàng</span>
                        <span class="value" style="font-weight:bold;">${order.shippingAddress}</span>
                    </div>
                </div>
                
                <c:if test="${order.statusCode != 'COMPLETED' && order.statusCode != 'CANCELLED'}">
                    <form id="shipping-form" method="post" action="${pageContext.request.contextPath}/manage/sales/order-update-shipping" style="display:none; margin-top:10px;">
                        <input type="hidden" name="id" value="${order.id}">
                        <div style="margin-bottom:12px;">
                            <label style="font-size:12px; font-weight:600; color:#555; display:block; margin-bottom:4px;">Tên người nhận</label>
                            <input type="text" name="customerName" value="${order.customerName}" required style="width:100%; height:36px; padding:0 10px; border:1px solid #ddd; border-radius:6px; font-size:13px;">
                        </div>
                        <div style="margin-bottom:12px;">
                            <label style="font-size:12px; font-weight:600; color:#555; display:block; margin-bottom:4px;">Số điện thoại</label>
                            <input type="text" name="phone" value="${order.phone}" required style="width:100%; height:36px; padding:0 10px; border:1px solid #ddd; border-radius:6px; font-size:13px;">
                        </div>
                        <div style="margin-bottom:15px;">
                            <label style="font-size:12px; font-weight:600; color:#555; display:block; margin-bottom:4px;">Địa chỉ giao hàng</label>
                            <input type="text" name="shippingAddress" value="${order.shippingAddress}" required style="width:100%; height:36px; padding:0 10px; border:1px solid #ddd; border-radius:6px; font-size:13px;">
                        </div>
                        <div style="display:flex; gap:10px;">
                            <button type="submit" class="btn" style="background:#2563eb; color:white; width:auto; height:34px; padding:0 15px; font-weight:bold; cursor:pointer;">Lưu</button>
                            <button type="button" class="btn" style="background:#e5e7eb; color:#333; width:auto; height:34px; padding:0 15px; cursor:pointer;" onclick="toggleEditShipping()">Hủy</button>
                        </div>
                    </form>
                    <script>
                        function toggleEditShipping() {
                            var displayDiv = document.getElementById('shipping-display');
                            var formEl = document.getElementById('shipping-form');
                            if (formEl.style.display === 'none') {
                                displayDiv.style.display = 'none';
                                formEl.style.display = 'block';
                            } else {
                                displayDiv.style.display = 'block';
                                formEl.style.display = 'none';
                            }
                        }
                    </script>
                </c:if>
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

                <c:choose>
                    <c:when test="${order.statusCode == 'COMPLETED'}">
                        <div style="background:#f0fdf4; border:1px solid #bbf7d0; color:#15803d; padding:12px; border-radius:6px; font-size:13px; font-weight:600;">
                            ✓ Đơn hàng đã Hoàn thành. Không thể cập nhật trạng thái.
                        </div>
                    </c:when>
                    <c:when test="${order.statusCode == 'CANCELLED'}">
                        <div style="background:#fef2f2; border:1px solid #fecaca; color:#991b1b; padding:12px; border-radius:6px; font-size:13px; font-weight:600;">
                            ✕ Đơn hàng đã bị Hủy. Không thể cập nhật trạng thái.
                        </div>
                    </c:when>
                    <c:when test="${order.statusCode == 'RETURNED'}">
                        <div style="background:#fffbeb; border:1px solid #fef3c7; color:#b45309; padding:12px; border-radius:6px; font-size:13px; font-weight:600;">
                            ⚠ Đơn hàng đã Đổi trả. Không thể cập nhật trạng thái.
                        </div>
                    </c:when>
                    <c:otherwise>
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

                            <button type="submit"
                                    class="btn">

                                Cập nhật trạng thái

                            </button>

                        </form>
                    </c:otherwise>
                </c:choose>

            </div>


            <!-- ================= THAO TÁC ================= -->

            <div class="box">

                <h2>Thao tác</h2>

                <c:if test="${order.statusCode == 'PENDING'}">
                    <p style="margin-bottom: 12px;">
                        <button type="button" class="btn" style="background:#059669; font-weight:600; cursor:pointer;" onclick="confirmAction(${order.id}, 'confirm')">
                            ✅ Xác nhận đơn hàng
                        </button>
                    </p>
                </c:if>

                <c:if test="${order.statusCode != 'COMPLETED'}">
                    <p style="margin-bottom: 12px;">
                        <a href="${pageContext.request.contextPath}/manage/sales/order-edit?id=${order.id}" class="btn" style="background:#d97706; font-weight:600; display:inline-flex; align-items:center; justify-content:center; text-decoration:none; cursor:pointer;">
                            ✏️ Sửa thông tin đơn hàng
                        </a>
                    </p>
                </c:if>

                <c:if test="${order.statusCode != 'COMPLETED' && order.statusCode != 'CANCELLED' && order.statusCode != 'RETURNED'}">
                    <p style="margin-bottom: 15px;">
                        <button type="button" class="btn" style="background:#dc2626; font-weight:600; cursor:pointer;" onclick="confirmAction(${order.id}, 'cancel')">
                            ❌ Hủy đơn hàng
                        </button>
                    </p>
                </c:if>

                <hr style="border:none; border-top:1px solid #eee; margin:15px 0;"/>

                <p>

                    <a href="${pageContext.request.contextPath}/manage/sales/delivery" style="text-decoration:none; color:#2563eb;">

                        🚚 Quản lý vận chuyển

                    </a>

                </p>

                <p>

                    <a href="${pageContext.request.contextPath}/manage/sales/returns" style="text-decoration:none; color:#2563eb;">

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

<script>
    function confirmAction(id, action) {
        let message = '';
        let url = '';
        if (action === 'confirm') {
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

</body>
</html>