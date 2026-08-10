<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <title>Tổng quan bán hàng</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
            background: #f5f6fa;
            color: #333;
        }

        .container {
            padding: 30px;
        }

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }

        .page-header h1 {
            margin: 0;
            font-size: 28px;
        }

        .page-header p {
            margin-top: 8px;
            color: #777;
        }

        .cards {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 16px;
            margin-bottom: 30px;
        }

        .card {
            background: white;
            border-radius: 12px;
            padding: 22px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
        }

        .card-title {
            color: #777;
            font-size: 14px;
            margin-bottom: 12px;
        }

        .card-value {
            font-size: 27px;
            font-weight: bold;
        }

        .card-link {
            display: inline-block;
            margin-top: 15px;
            text-decoration: none;
            color: #2563eb;
            font-size: 14px;
        }

        .content {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 20px;
        }

        .box {
            background: white;
            border-radius: 12px;
            padding: 22px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
        }

        .box h2 {
            margin-top: 0;
            margin-bottom: 20px;
            font-size: 20px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        table th,
        table td {
            padding: 13px 10px;
            border-bottom: 1px solid #eee;
            text-align: left;
        }

        table th {
            font-size: 14px;
            color: #666;
        }

        table td {
            font-size: 14px;
        }

        .status {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 12px;
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

        .menu-list {
            list-style: none;
            margin: 0;
            padding: 0;
        }

        .menu-list li {
            margin-bottom: 12px;
        }

        .menu-list a {
            display: block;
            padding: 13px 15px;
            background: #f8f9fa;
            border-radius: 8px;
            text-decoration: none;
            color: #333;
        }

        .menu-list a:hover {
            background: #e9ecef;
        }

        @media (max-width: 1000px) {
            .cards {
                grid-template-columns: repeat(2, 1fr);
            }

            .content {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 600px) {
            .container {
                padding: 15px;
            }

            .cards {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>

<div class="container">

    <!-- HEADER -->
    <div class="page-header">
        <div>
            <h1>Tổng quan bán hàng</h1>
            <p>Xin chào nhân viên bán hàng, đây là tổng quan hoạt động bán hàng.</p>
        </div>
    </div>

    <!-- THỐNG KÊ -->
    <div class="cards">

        <!-- Doanh thu -->
        <div class="card">
            <div class="card-title">Doanh thu</div>
            <div class="card-value">
                <c:choose>
                    <c:when test="${revenue != null}">
                        <fmt:formatNumber value="${revenue}" pattern="#,##0" /> ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                </c:choose>
            </div>
            <a class="card-link" href="${pageContext.request.contextPath}/manage/sales/report">
                Xem báo cáo →
            </a>
        </div>

        <!-- Đơn hoàn thành -->
        <div class="card">
            <div class="card-title">Đơn hoàn thành</div>
            <div class="card-value">
                <c:choose>
                    <c:when test="${completedOrders != null}">${completedOrders}</c:when>
                    <c:otherwise>0</c:otherwise>
                </c:choose>
            </div>
            <a class="card-link" href="${pageContext.request.contextPath}/manage/sales/orders?status=COMPLETED">
                Xem đơn hàng →
            </a>
        </div>

        <!-- Đơn đang xử lý -->
        <div class="card">
            <div class="card-title">Đơn đang xử lý</div>
            <div class="card-value">
                <c:choose>
                    <c:when test="${processingOrders != null}">${processingOrders}</c:when>
                    <c:otherwise>0</c:otherwise>
                </c:choose>
            </div>
            <a class="card-link" href="${pageContext.request.contextPath}/manage/sales/orders?status=PENDING">
                Xem đơn hàng →
            </a>
        </div>

        <!-- Đơn đang giao -->
        <div class="card">
            <div class="card-title">Đơn đang giao</div>
            <div class="card-value">
                <c:choose>
                    <c:when test="${shippingOrders != null}">${shippingOrders}</c:when>
                    <c:otherwise>0</c:otherwise>
                </c:choose>
            </div>
            <a class="card-link" href="${pageContext.request.contextPath}/manage/sales/orders?status=SHIPPING">
                Xem đơn hàng →
            </a>
        </div>

        <!-- Bảo hành -->
        <div class="card" style="border-left:4px solid #7c3aed;">
            <div class="card-title">🛡 Bảo hành</div>
            <div class="card-value" style="color:#7c3aed;">
                <c:choose>
                    <c:when test="${warrantyCount != null}">${warrantyCount}</c:when>
                    <c:otherwise>0</c:otherwise>
                </c:choose>
            </div>
            <a class="card-link" href="${pageContext.request.contextPath}/manage/sales/warranty">
                Xem bảo hành →
            </a>
        </div>

    </div>

    <!-- NỘI DUNG -->
    <div class="content">

        <!-- ĐƠN HÀNG GẦN ĐÂY -->
        <div class="box">
            <h2>Đơn hàng gần đây</h2>
            <table>
                <thead>
                <tr>
                    <th>Mã đơn</th>
                    <th>Khách hàng</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty orders}">
                        <c:forEach var="order" items="${orders}" varStatus="status">
                            <c:if test="${status.index < 5}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/manage/sales/order-detail?id=${order.id}">
                                            #${order.id}
                                        </a>
                                    </td>
                                    <td>${order.customerName}</td>
                                    <td>
                                        <fmt:formatNumber value="${order.total}" pattern="#,##0" /> ₫
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${order.status == 'COMPLETED'}">
                                                <span class="status completed">Hoàn thành</span>
                                            </c:when>
                                            <c:when test="${order.status == 'SHIPPING' or order.status == 'DELIVERED'}">
                                                <span class="status shipping">Đang giao</span>
                                            </c:when>
                                            <c:when test="${order.status == 'CANCELLED'}">
                                                <span class="status" style="background:#fee2e2;color:#991b1b;">Đã hủy</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status processing">${order.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="4" style="text-align: center; color: #888;">
                                Chưa có đơn hàng.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <!-- CHỨC NĂNG NHANH -->
        <div class="box">
            <h2>Chức năng nhanh</h2>
            <ul class="menu-list">
                <li><a href="${pageContext.request.contextPath}/manage/sales/orders">📦 Quản lý đơn hàng</a></li>
                <li><a href="${pageContext.request.contextPath}/manage/sales/customers">👤 Quản lý khách hàng</a></li>
                <li><a href="${pageContext.request.contextPath}/manage/sales/delivery">🚚 Vận chuyển</a></li>
                <li><a href="${pageContext.request.contextPath}/manage/sales/returns">🔄 Đổi trả</a></li>
                <li><a href="${pageContext.request.contextPath}/manage/sales/reviews">⭐ Kiểm duyệt đánh giá</a></li>
                <li><a href="${pageContext.request.contextPath}/manage/sales/report">📊 Báo cáo bán hàng</a></li>
            </ul>
        </div>

    </div>

</div>

</body>

</html>