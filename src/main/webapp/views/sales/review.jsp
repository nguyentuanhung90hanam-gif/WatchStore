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



<div class="container">

    <!-- ================= HEADER ================= -->

    <div class="header">
        <h1>Kiểm duyệt đánh giá</h1>
        <p>Kiểm tra và xử lý đánh giá của khách hàng về sản phẩm.</p>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#d1fae5; border:1px solid #6ee7b7; color:#065f46; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
             ${sessionScope.flash}
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

            <select name="rating">
                <option value="">-- Tất cả số sao --</option>
                <option value="5" ${param.rating == '5' or rating == '5' ? 'selected' : ''}>⭐⭐⭐⭐⭐ 5 sao</option>
                <option value="4" ${param.rating == '4' or rating == '4' ? 'selected' : ''}>⭐⭐⭐⭐ 4 sao</option>
                <option value="3" ${param.rating == '3' or rating == '3' ? 'selected' : ''}>⭐⭐⭐ 3 sao</option>
                <option value="2" ${param.rating == '2' or rating == '2' ? 'selected' : ''}>⭐⭐ 2 sao</option>
                <option value="1" ${param.rating == '1' or rating == '1' ? 'selected' : ''}>⭐ 1 sao</option>
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
                                ${not empty review.createdAt ? review.createdAt : review.reviewDate}
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
                                <div style="display:flex; gap:8px;">
                                    <c:choose>
                                        <c:when test="${review.status == 'PENDING' or review.status == 'Chờ duyệt'}">
                                            <button type="button" class="btn" style="background:#059669; color:white; padding:6px 12px; font-size:13px; font-weight:600; cursor:pointer;" onclick="submitReviewAction(${review.id}, 'APPROVED')">
                                                Duyệt
                                            </button>
                                            <button type="button" class="btn" style="background:#dc2626; color:white; padding:6px 12px; font-size:13px; font-weight:600; cursor:pointer;" onclick="submitReviewAction(${review.id}, 'REJECTED')">
                                                Từ chối
                                            </button>
                                        </c:when>
                                        <c:when test="${review.status == 'APPROVED' or review.status == 'Đã duyệt'}">
                                            <button type="button" class="btn" style="background:#d97706; color:white; padding:6px 12px; font-size:13px; font-weight:600; cursor:pointer;" 
                                                    data-id="${review.id}"
                                                    data-customer="${review.customerName}"
                                                    data-email="${review.email}"
                                                    data-phone="${review.phone}"
                                                    data-product="${review.productName}"
                                                    data-rating="${review.rating}"
                                                    data-content="${review.content}"
                                                    data-date="${not empty review.createdAt ? review.createdAt : review.reviewDate}"
                                                    data-order="${review.orderCode}"
                                                    data-reply="${review.reply}"
                                                    onclick="openReplyModal(event)">
                                                Phản hồi
                                            </button>
                                            <button type="button" class="btn" style="background:#2563eb; color:white; padding:6px 12px; font-size:13px; font-weight:600; cursor:pointer;" 
                                                    data-id="${review.id}"
                                                    data-customer="${review.customerName}"
                                                    data-email="${review.email}"
                                                    data-phone="${review.phone}"
                                                    data-product="${review.productName}"
                                                    data-rating="${review.rating}"
                                                    data-content="${review.content}"
                                                    data-date="${not empty review.createdAt ? review.createdAt : review.reviewDate}"
                                                    data-order="${review.orderCode}"
                                                    data-reply="${review.reply}"
                                                    onclick="openDetailModal(event)">
                                                Xem chi tiết
                                            </button>
                                        </c:when>
                                        <c:when test="${review.status == 'REJECTED' or review.status == 'Đã từ chối'}">
                                            <button type="button" class="btn" style="background:#4b5563; color:white; padding:6px 12px; font-size:13px; font-weight:600; cursor:pointer;" 
                                                    data-id="${review.id}"
                                                    data-customer="${review.customerName}"
                                                    data-email="${review.email}"
                                                    data-phone="${review.phone}"
                                                    data-product="${review.productName}"
                                                    data-rating="${review.rating}"
                                                    data-content="${review.content}"
                                                    data-date="${not empty review.createdAt ? review.createdAt : review.reviewDate}"
                                                    data-order="${review.orderCode}"
                                                    data-reply="${review.reply}"
                                                    onclick="openDetailModal(event)">
                                                Xem chi tiết
                                            </button>
                                        </c:when>
                                    </c:choose>
                                </div>
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

    <!-- ================= MODAL XEM CHI TIẾT & PHẢN HỒI ================= -->
    <div id="detail-modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center; padding:15px;">
        <div style="background:white; border-radius:12px; max-width:550px; width:100%; box-shadow:0 10px 25px rgba(0,0,0,0.25); padding:25px; position:relative;">
            <span style="position:absolute; top:15px; right:15px; font-size:24px; color:#aaa; cursor:pointer; font-weight:bold;" onclick="closeModal()">&times;</span>
            <h2 style="margin-top:0; margin-bottom:20px; font-size:20px; color:#1e293b; border-bottom:1px solid #e2e8f0; padding-bottom:12px;">Chi tiết đánh giá <span id="modal-id-text" style="color:#2563eb;"></span></h2>
            
            <div style="display:grid; grid-template-columns:1fr; gap:12px; margin-bottom:20px; font-size:14px; text-align:left;">
                <div>
                    <span style="color:#64748b; font-weight:600; display:block; margin-bottom:2px;">Khách hàng:</span>
                    <span id="modal-customer" style="font-weight:bold; color:#1e293b;"></span>
                </div>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px;">
                    <div>
                        <span style="color:#64748b; font-weight:600; display:block; margin-bottom:2px;">Sản phẩm:</span>
                        <span id="modal-product" style="font-weight:bold; color:#1e293b;"></span>
                    </div>
                    <div>
                        <span style="color:#64748b; font-weight:600; display:block; margin-bottom:2px;">Đơn hàng:</span>
                        <span id="modal-order" style="font-weight:bold; color:#2563eb;"></span>
                    </div>
                </div>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px;">
                    <div>
                        <span style="color:#64748b; font-weight:600; display:block; margin-bottom:2px;">Ngày đăng:</span>
                        <span id="modal-date" style="color:#1e293b;"></span>
                    </div>
                    <div>
                        <span style="color:#64748b; font-weight:600; display:block; margin-bottom:2px;">Đánh giá:</span>
                        <span id="modal-stars" style="color:#d97706; font-size:16px;"></span>
                    </div>
                </div>
                <div>
                    <span style="color:#64748b; font-weight:600; display:block; margin-bottom:2px;">Nội dung đánh giá:</span>
                    <p id="modal-content" style="margin:0; background:#f8fafc; padding:12px; border-radius:8px; border:1px solid #e2e8f0; line-height:1.5; color:#334155; font-style:italic;"></p>
                </div>
                <div id="modal-reply-display-container">
                    <span style="color:#64748b; font-weight:600; display:block; margin-bottom:4px;">Phản hồi hiện tại:</span>
                    <div id="modal-reply-display" style="font-size:13px;"></div>
                </div>
            </div>
            
            <!-- FORM PHẢN HỒI -->
            <form method="post" action="${pageContext.request.contextPath}/manage/sales/reviews" style="border-top:1px solid #e2e8f0; padding-top:15px; text-align:left;">
                <input type="hidden" name="id" id="modal-reply-id">
                <div style="margin-bottom:15px;">
                    <label for="modal-reply-input" style="font-weight:600; display:block; margin-bottom:6px; font-size:14px; color:#1e293b;">Nhập/Chỉnh sửa phản hồi của bạn:</label>
                    <textarea name="replyContent" id="modal-reply-input" placeholder="Nhập câu trả lời cho đánh giá..." style="width:100%; height:80px; padding:10px; border:1px solid #cbd5e1; border-radius:6px; font-family:inherit; font-size:13px; resize:vertical; outline:none;"></textarea>
                </div>
                <div style="display:flex; justify-content:flex-end; gap:10px;">
                    <button type="submit" class="btn" style="background:#2563eb; color:white; height:36px; padding:0 18px; font-weight:bold; cursor:pointer; border:none; border-radius:6px;">💾 Gửi phản hồi</button>
                    <button type="button" class="btn" style="background:#e2e8f0; color:#334155; height:36px; padding:0 18px; cursor:pointer; border:none; border-radius:6px;" onclick="closeModal()">Đóng</button>
                </div>
            </form>
        </div>
    </div>

    <!-- HIDDEN FORM FOR APPROVE/REJECT -->
    <form id="action-form" method="post" action="${pageContext.request.contextPath}/manage/sales/reviews" style="display:none;">
        <input type="hidden" name="id" id="action-id">
        <input type="hidden" name="status" id="action-status">
    </form>

    <script>
        function submitReviewAction(id, status) {
            let message = status === 'APPROVED' ? 'Bạn có chắc chắn muốn DUYỆT đánh giá này?' : 'Bạn có chắc chắn muốn TỪ CHỐI đánh giá này?';
            if (confirm(message)) {
                document.getElementById('action-id').value = id;
                document.getElementById('action-status').value = status;
                document.getElementById('action-form').submit();
            }
        }
        
        function openDetailModal(event) {
            var btn = event.currentTarget;
            populateModal(btn);
            document.getElementById('detail-modal').style.display = 'flex';
        }
        
        function openReplyModal(event) {
            var btn = event.currentTarget;
            populateModal(btn);
            document.getElementById('detail-modal').style.display = 'flex';
            setTimeout(function() {
                document.getElementById('modal-reply-input').focus();
            }, 100);
        }
        
        function populateModal(btn) {
            var id = btn.getAttribute('data-id');
            var customer = btn.getAttribute('data-customer');
            var email = btn.getAttribute('data-email');
            var phone = btn.getAttribute('data-phone');
            var product = btn.getAttribute('data-product');
            var rating = parseInt(btn.getAttribute('data-rating') || '5');
            var content = btn.getAttribute('data-content');
            var date = btn.getAttribute('data-date');
            var order = btn.getAttribute('data-order');
            var reply = btn.getAttribute('data-reply');
            
            document.getElementById('modal-id-text').innerText = '#' + id;
            document.getElementById('modal-customer').innerText = customer + ' (' + phone + ' - ' + email + ')';
            document.getElementById('modal-product').innerText = product;
            document.getElementById('modal-order').innerText = order ? '#' + order : 'Chưa rõ';
            document.getElementById('modal-date').innerText = date;
            document.getElementById('modal-stars').innerText = '★'.repeat(rating) + '☆'.repeat(5 - rating);
            document.getElementById('modal-content').innerText = content;
            
            document.getElementById('modal-reply-id').value = id;
            document.getElementById('modal-reply-input').value = reply || '';
            
            var replyDisplay = document.getElementById('modal-reply-display');
            if (reply && reply.trim() !== '') {
                replyDisplay.innerHTML = '<p style="margin:0; background:#f0fdf4; padding:10px; border-radius:6px; border:1px solid #bbf7d0; color:#166534; line-height:1.4;">' + reply + '</p>';
            } else {
                replyDisplay.innerHTML = '<p style="margin:0; color:#94a3b8; font-style:italic;">Chưa có phản hồi nào từ cửa hàng.</p>';
            }
        }
        
        function closeModal() {
            document.getElementById('detail-modal').style.display = 'none';
        }
    </script>

</div>
