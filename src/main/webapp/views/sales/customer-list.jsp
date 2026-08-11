<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Danh sách khách hàng</title>

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

        /* =========================
           SEARCH
           ========================= */

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
        }

        .search-form input {
            flex: 1;
            height: 42px;
            padding: 0 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
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

        .btn-search { background: #2563eb; color: white; }
        .btn-reset { background: #e5e7eb; color: #333; }
        .btn-add { background: #16a34a; color: white; }
        .btn-add:hover { background: #15803d; }
        .flash { background: #f0fdf4; border: 1px solid #86efac; color: #166534; padding: 12px 16px; border-radius: 8px; margin-bottom: 18px; font-size: 14px; }

        /* =========================
           TABLE
           ========================= */

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

        .customer-id {
            font-weight: bold;
            color: #2563eb;
        }

        .customer-name {
            font-weight: 500;
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

            .search-form {
                display: block;
            }

            .search-form input,
            .btn {
                width: 100%;
                margin-bottom: 10px;
            }

        }

    </style>

</head>

<body>

<div class="container">

    <!-- ================= HEADER ================= -->

    <div class="header" style="display:flex;justify-content:space-between;align-items:flex-start;">
        <div>
            <h1>Danh sách khách hàng</h1>
            <p>Xem, tìm kiếm và thêm mới khách hàng.</p>
        </div>
        <a href="${pageContext.request.contextPath}/manage/sales/customer-add" class="btn btn-add" style="margin-top:8px;">➕ Thêm khách hàng</a>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="flash">${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>


    <!-- ================= SEARCH ================= -->

    <div class="search-box">

        <form method="get"
              action="${pageContext.request.contextPath}/manage/sales/customers"
              class="search-form">

            <input type="text"
                   name="keyword"
                   value="${keyword}"
                    placeholder="Nhập tên, số điện thoại, email hoặc mã KH...">

            <button type="submit"
                    class="btn btn-search">

                Tìm kiếm

            </button>

            <a href="${pageContext.request.contextPath}/manage/sales/customers"
               class="btn btn-reset">

                Đặt lại

            </a>

        </form>

    </div>


    <!-- ================= CUSTOMER TABLE ================= -->

    <div class="table-box">

        <div class="table-header">

            <h2>
                Danh sách khách hàng
            </h2>

            <span>

                Tổng:

                <strong>
                    ${customers.size()}
                </strong>

                khách hàng

            </span>

        </div>


        <table>

            <thead>

            <tr>

                <th>Mã KH</th>

                <th>Họ và tên</th>

                <th>Email</th>

                <th>Số điện thoại</th>

                <th>Địa chỉ</th>

                <th>Thao tác</th>

            </tr>

            </thead>


            <tbody>

            <c:choose>

                <c:when test="${not empty customers}">

                    <c:forEach var="customer"
                               items="${customers}">

                        <tr>

                            <!-- MÃ KH -->

                            <td>

                                <span class="customer-id">

                                    #${customer.id}

                                </span>

                            </td>


                            <!-- HỌ TÊN -->

                            <td>

                                <span class="customer-name">

                                    ${customer.fullName}

                                </span>

                            </td>


                            <!-- EMAIL -->

                            <td>

                                ${customer.email}

                            </td>


                            <!-- SỐ ĐIỆN THOẠI -->

                            <td>

                                ${customer.phone}

                            </td>


                            <!-- ĐỊA CHỈ -->

                            <td>

                                ${customer.address}

                            </td>


                            <!-- THAO TÁC -->

                            <td>

                                <a class="action"
                                   href="${pageContext.request.contextPath}/manage/sales/customer-detail?id=${customer.id}">

                                    Xem chi tiết

                                </a>

                            </td>

                        </tr>

                    </c:forEach>

                </c:when>


                <c:otherwise>

                    <tr>

                        <td colspan="6"
                            class="empty">

                            Không tìm thấy khách hàng.

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