<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>



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

        .comment-id {
            color: #2563eb;
            font-weight: bold;
        }

        .customer-name {
            font-weight: 500;
        }

        .product-name {
            font-weight: 500;
        }

        .comment-content {
            max-width: 400px;
            line-height: 1.5;
            word-break: break-word;
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

        .visible {
            background: #d1e7dd;
            color: #0f5132;
        }

        .hidden {
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

        .show-btn {
            background: #198754;
            color: white;
        }

        .hide-btn {
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



<div class="container">

    <!-- ================= HEADER ================= -->

    <div class="header">
        <h1>Bình luận sản phẩm</h1>
        <p>Kiểm duyệt và quản lý các bình luận từ người dùng.</p>
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
              action="${pageContext.request.contextPath}/manage/sales/comments"
              class="filter-form">

            <input type="text"
                   name="keyword"
                   value="${keyword}"
                   placeholder="Nhập tên khách hàng hoặc sản phẩm...">


            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="APPROVED" ${param.status == 'APPROVED' or status == 'APPROVED' or status == 'Hiển thị' ? 'selected' : ''}>Hiển thị</option>
                <option value="PENDING" ${param.status == 'PENDING' or status == 'PENDING' or status == 'Chờ duyệt' ? 'selected' : ''}>Chờ duyệt</option>
                <option value="HIDDEN" ${param.status == 'HIDDEN' or status == 'HIDDEN' or status == 'Ẩn' ? 'selected' : ''}>Ẩn</option>
            </select>


            <button type="submit"
                    class="btn btn-search">

                Tìm kiếm

            </button>


            <a href="${pageContext.request.contextPath}/manage/sales/comments"
               class="btn btn-reset">

                Đặt lại

            </a>

        </form>

    </div>


    <!-- ================= COMMENT TABLE ================= -->

    <div class="table-box">

        <div class="table-header">

            <h2>
                Danh sách bình luận
            </h2>

            <span>

                Tổng:

                <strong>
                    ${comments.size()}
                </strong>

                bình luận

            </span>

        </div>


        <table>

            <thead>

            <tr>

                <th>Mã</th>

                <th>Khách hàng</th>

                <th>Sản phẩm</th>

                <th>Nội dung</th>

                <th>Ngày đăng</th>

                <th>Trạng thái</th>

                <th>Thao tác</th>

            </tr>

            </thead>


            <tbody>

            <c:choose>

                <c:when test="${not empty comments}">

                    <c:forEach var="comment"
                               items="${comments}">

                        <tr>

                            <!-- MÃ -->

                            <td>

                                <span class="comment-id">

                                    #${comment.id}

                                </span>

                            </td>


                            <!-- KHÁCH HÀNG -->

                            <td>

                                <span class="customer-name">

                                    ${comment.customerName}

                                </span>

                            </td>


                            <!-- SẢN PHẨM -->

                            <td>

                                <span class="product-name">

                                    ${comment.productName}

                                </span>

                            </td>


                            <!-- NỘI DUNG -->

                            <td>

                                <div class="comment-content">

                                    ${comment.content}

                                </div>

                            </td>


                            <!-- NGÀY -->

                            <td>

                                ${comment.commentDate}

                            </td>


                            <!-- TRẠNG THÁI -->
                            <td>
                                <c:choose>
                                    <c:when test="${comment.status == 'APPROVED' or comment.status == 'Hiển thị'}">
                                        <span class="status visible" style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#d1fae5;color:#065f46;">Hiển thị</span>
                                    </c:when>
                                    <c:when test="${comment.status == 'PENDING' or comment.status == 'Chờ duyệt'}">
                                        <span class="status pending" style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#fef3c7;color:#92400e;">Chờ duyệt</span>
                                    </c:when>
                                    <c:when test="${comment.status == 'HIDDEN' or comment.status == 'Ẩn'}">
                                        <span class="status hidden" style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#fee2e2;color:#991b1b;">Ẩn</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status" style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:#f1f5f9;color:#64748b;">${comment.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <!-- THAO TÁC -->
                            <td>
                                <form method="post" action="${pageContext.request.contextPath}/manage/sales/comments" style="display:flex;gap:8px;align-items:center;">
                                    <input type="hidden" name="id" value="${comment.id}">
                                    <select name="status" style="padding:6px 10px;border:1px solid #cbd5e1;border-radius:6px;font-size:13px;outline:none;background:white;color:#1e293b;">
                                        <option value="APPROVED" ${comment.status == 'APPROVED' or comment.status == 'Hiển thị' ? 'selected' : ''}>Hiển thị</option>
                                        <option value="PENDING" ${comment.status == 'PENDING' or comment.status == 'Chờ duyệt' ? 'selected' : ''}>Chờ duyệt</option>
                                        <option value="HIDDEN" ${comment.status == 'HIDDEN' or comment.status == 'Ẩn' ? 'selected' : ''}>Ẩn</option>
                                    </select>
                                    <button type="submit" style="padding:6px 14px;background:#2563eb;color:white;border:none;border-radius:6px;font-size:13px;font-weight:600;cursor:pointer;">Xác nhận</button>
                                </form>
                            </td>

                        </tr>

                    </c:forEach>

                </c:when>


                <c:otherwise>

                    <tr>

                        <td colspan="7"
                            class="empty">

                            Chưa có bình luận.

                        </td>

                    </tr>

                </c:otherwise>

            </c:choose>

            </tbody>

        </table>

    </div>

</div>