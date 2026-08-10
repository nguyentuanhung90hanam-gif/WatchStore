<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Kiểm duyệt đánh giá</title>

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
            min-width: 1000px;
        }

        th,
        td {
            padding: 14px 12px;
            border-bottom: 1px solid #eee;
            text-align: left;
            vertical-align: top;
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

        /* ================= STAR ================= */

        .stars {
            color: #f59e0b;
            font-size: 17px;
            letter-spacing: 2px;
            white-space: nowrap;
        }

        .review-content {
            max-width: 350px;
            line-height: 1.5;
        }

        .reviewer {
            font-weight: 500;
        }

        .product {
            font-weight: 500;
            color: #444;
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

        /* ================= ACTION ================= */

        .actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .action-btn {
            border: none;
            border-radius: 5px;
            padding: 7px 12px;
            cursor: pointer;
            font-size: 12px;
        }

        .approve {
            background: #198754;
            color: white;
        }

        .reject {
            background: #dc3545;
            color: white;
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
        <h1>Kiểm duyệt đánh giá</h1>
        <p>Kiểm tra và xử lý đánh giá của khách hàng về sản phẩm.</p>
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
              action="${pageContext.request.contextPath}/manage/sales/reviews"
              class="filter-form">

            <input type="text"
                   name="keyword"
                   value="${keyword}"
                   placeholder="Nhập tên khách hàng hoặc sản phẩm...">


            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="APPROVED" ${param.status == 'APPROVED' or status == 'APPROVED' or status == 'Đã duyệt' ? 'selected' : ''}>Đã duyệt</option>
                <option value="PENDING" ${param.status == 'PENDING' or status == 'PENDING' or status == 'Chờ duyệt' ? 'selected' : ''}>Chờ duyệt</option>
                <option value="REJECTED" ${param.status == 'REJECTED' or status == 'REJECTED' or status == 'Đã từ chối' ? 'selected' : ''}>Đã từ chối</option>
            </select>


            <button type="submit"
                    class="btn btn-search">

                Tìm kiếm

            </button>


            <a href="${pageContext.request.contextPath}/manage/sales/reviews"
               class="btn btn-reset">

                Đặt lại

            </a>

        </form>

    </div>


    <!-- ================= REVIEW TABLE ================= -->

    <div class="table-box">

        <div class="table-header">

            <h2>
                Danh sách đánh giá
            </h2>

            <span>

                Tổng:

                <strong>
                    ${reviews.size()}
                </strong>

                đánh giá

            </span>

        </div>


        <table>

            <thead>

            <tr>

                <th>Mã</th>

                <th>Khách hàng</th>

                <th>Sản phẩm</th>

                <th>Đánh giá</th>

                <th>Nội dung</th>

                <th>Ngày đăng</th>

                <th>Trạng thái</th>

                <th>Thao tác</th>

            </tr>

            </thead>


            <tbody>

            <c:choose>

                <c:when test="${not empty reviews}">

                    <c:forEach var="review"
                               items="${reviews}">

                        <tr>

                            <!-- MÃ -->

                            <td>

                                #${review.id}

                            </td>


                            <!-- KHÁCH HÀNG -->

                            <td>

                                <span class="reviewer">

                                    ${review.customerName}

                                </span>

                            </td>


                            <!-- SẢN PHẨM -->

                            <td>

                                <span class="product">

                                    ${review.productName}

                                </span>

                            </td>


                            <!-- SỐ SAO -->

                            <td>

                                <div class="stars">

                                    <c:choose>

                                        <c:when test="${review.rating == 1}">
                                            ★
                                        </c:when>

                                        <c:when test="${review.rating == 2}">
                                            ★★
                                        </c:when>

                                        <c:when test="${review.rating == 3}">
                                            ★★★
                                        </c:when>

                                        <c:when test="${review.rating == 4}">
                                            ★★★★
                                        </c:when>

                                        <c:when test="${review.rating == 5}">
                                            ★★★★★
                                        </c:when>

                                        <c:otherwise>
                                            Chưa có
                                        </c:otherwise>

                                    </c:choose>

                                </div>

                            </td>


                            <!-- NỘI DUNG -->

                            <td>

                                <div class="review-content">

                                    ${review.content}

                                </div>

                            </td>


                            <!-- NGÀY -->

                            <td>

                                ${review.reviewDate}

                            </td>


                            <!-- TRẠNG THÁI -->
                            <td>
                                <c:choose>
                                    <c:when test="${review.status == 'APPROVED' or review.status == 'Đã duyệt'}">
                                        <span class="status approved" style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#d1fae5;color:#065f46;">Đã duyệt</span>
                                    </c:when>
                                    <c:when test="${review.status == 'PENDING' or review.status == 'Chờ duyệt'}">
                                        <span class="status pending" style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#fef3c7;color:#92400e;">Chờ duyệt</span>
                                    </c:when>
                                    <c:when test="${review.status == 'REJECTED' or review.status == 'Đã từ chối'}">
                                        <span class="status rejected" style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#fee2e2;color:#991b1b;">Đã từ chối</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status" style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#f1f5f9;color:#64748b;">${review.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <!-- THAO TÁC -->
                            <td>
                                <form method="post" action="${pageContext.request.contextPath}/manage/sales/reviews" style="display:flex;gap:8px;align-items:center;">
                                    <input type="hidden" name="id" value="${review.id}">
                                    <select name="status" style="padding:6px 10px;border:1px solid #cbd5e1;border-radius:6px;font-size:13px;outline:none;background:white;color:#1e293b;">
                                        <option value="APPROVED" ${review.status == 'APPROVED' or review.status == 'Đã duyệt' ? 'selected' : ''}>Đã duyệt</option>
                                        <option value="PENDING" ${review.status == 'PENDING' or review.status == 'Chờ duyệt' ? 'selected' : ''}>Chờ duyệt</option>
                                        <option value="REJECTED" ${review.status == 'REJECTED' or review.status == 'Đã từ chối' ? 'selected' : ''}>Đã từ chối</option>
                                    </select>
                                    <button type="submit" style="padding:6px 14px;background:#2563eb;color:white;border:none;border-radius:6px;font-size:13px;font-weight:600;cursor:pointer;">Xác nhận</button>
                                </form>
                            </td>

                        </tr>

                    </c:forEach>

                </c:when>


                <c:otherwise>

                    <tr>

                        <td colspan="8"
                            class="empty">

                            Chưa có đánh giá.

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