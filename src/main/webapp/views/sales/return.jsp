<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Yêu cầu đổi trả</title>

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

        .return-id {
            color: #2563eb;
            font-weight: bold;
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

        .completed {
            background: #cfe2ff;
            color: #084298;
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
        <h1>Yêu cầu đổi trả</h1>
        <p>Quản lý và xử lý các yêu cầu đổi trả của khách hàng.</p>
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
              action="${pageContext.request.contextPath}/manage/sales/returns"
              class="filter-form">

            <input type="text"
                   name="keyword"
                   value="${keyword}"
                   placeholder="Nhập mã đơn hoặc tên khách hàng...">


            <select name="status">

                <option value="">
                    -- Tất cả trạng thái --
                </option>

                <option value="Chờ xử lý"
                    ${status == 'Chờ xử lý' ? 'selected' : ''}>

                    Chờ xử lý

                </option>

                <option value="Đã duyệt"
                    ${status == 'Đã duyệt' ? 'selected' : ''}>

                    Đã duyệt

                </option>

                <option value="Đã từ chối"
                    ${status == 'Đã từ chối' ? 'selected' : ''}>

                    Đã từ chối

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


            <a href="${pageContext.request.contextPath}/manage/sales/returns"
               class="btn btn-reset">

                Đặt lại

            </a>

        </form>

    </div>


    <!-- ================= TABLE ================= -->

    <div class="table-box">

        <div class="table-header">

            <h2>
                Danh sách yêu cầu
            </h2>

            <span>

                Tổng:

                <strong>
                    ${returns.size()}
                </strong>

                yêu cầu

            </span>

        </div>


        <table>

            <thead>

            <tr>

                <th>Mã yêu cầu</th>

                <th>Mã đơn</th>

                <th>Khách hàng</th>

                <th>Lý do</th>

                <th>Ngày yêu cầu</th>

                <th>Trạng thái</th>

                <th>Thao tác</th>

            </tr>

            </thead>


            <tbody>

            <c:choose>

                <c:when test="${not empty returns}">

                    <c:forEach var="item"
                               items="${returns}">

                        <tr>

                            <!-- MÃ YÊU CẦU -->

                            <td>

                                <span class="return-id">

                                    #${item.id}

                                </span>

                            </td>


                            <!-- MÃ ĐƠN -->

                            <td>

                                #${item.orderId}

                            </td>


                            <!-- KHÁCH HÀNG -->

                            <td>

                                ${item.customerName}

                            </td>


                            <!-- LÝ DO -->

                            <td>

                                ${item.reason}

                            </td>


                            <!-- NGÀY -->

                            <td>

                                ${item.requestDate}

                            </td>


                            <!-- TRẠNG THÁI -->

                            <td>

                                <c:choose>

                                    <c:when test="${item.status == 'Chờ xử lý'}">

                                        <span class="status pending">

                                            ${item.status}

                                        </span>

                                    </c:when>


                                    <c:when test="${item.status == 'Đã duyệt'}">

                                        <span class="status approved">

                                            ${item.status}

                                        </span>

                                    </c:when>


                                    <c:when test="${item.status == 'Đã từ chối'}">

                                        <span class="status rejected">

                                            ${item.status}

                                        </span>

                                    </c:when>


                                    <c:when test="${item.status == 'Hoàn thành'}">

                                        <span class="status completed">

                                            ${item.status}

                                        </span>

                                    </c:when>


                                    <c:otherwise>

                                        <span class="status">

                                            ${item.status}

                                        </span>

                                    </c:otherwise>

                                </c:choose>

                            </td>


                            <!-- THAO TÁC -->
                            <td>
                                <form method="post" action="${pageContext.request.contextPath}/manage/sales/returns" style="display:flex;gap:6px;align-items:center;">
                                    <input type="hidden" name="id" value="${item.id}">
                                    <select name="status" style="padding:6px 10px;border:1px solid #ddd;border-radius:6px;font-size:13px;outline:none;">
                                        <option value="Chờ xử lý" ${item.status == 'Chờ xử lý' ? 'selected' : ''}>Chờ xử lý</option>
                                        <option value="Đã duyệt" ${item.status == 'Đã duyệt' ? 'selected' : ''}>Đã duyệt</option>
                                        <option value="Đã từ chối" ${item.status == 'Đã từ chối' ? 'selected' : ''}>Đã từ chối</option>
                                        <option value="Hoàn thành" ${item.status == 'Hoàn thành' ? 'selected' : ''}>Hoàn thành</option>
                                    </select>
                                    <button type="submit" style="padding:6px 14px;background:#2563eb;color:white;border:none;border-radius:6px;font-size:13px;font-weight:600;cursor:pointer;">Lưu</button>
                                </form>
                            </td>

                        </tr>

                    </c:forEach>

                </c:when>


                <c:otherwise>

                    <tr>

                        <td colspan="7"
                            class="empty">

                            Chưa có yêu cầu đổi trả.

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