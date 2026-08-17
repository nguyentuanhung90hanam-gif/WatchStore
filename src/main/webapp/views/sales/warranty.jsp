<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

    <style>
        .container { max-width: 1200px; margin: auto; padding: 10px 0; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
        .page-header h1 { font-size: 26px; font-weight: 700; }
        .page-header p { color: #777; font-size: 14px; margin-top: 4px; }
        .btn {
            display: inline-flex; align-items: center; gap: 6px;
            padding: 10px 20px; border: none; border-radius: 8px;
            font-size: 14px; font-weight: 600; cursor: pointer; text-decoration: none;
            justify-content: center;
        }
        .btn-primary { background: #2563eb; color: white; }
        .btn-primary:hover { background: #1d4ed8; }
        .btn-sm { padding: 5px 12px; font-size: 12px; border-radius: 5px; height: 30px; }
        .btn-outline { background: white; border: 1px solid #ddd; color: #333; }
        .btn-outline:hover { background: #f0f0f0; }

        /* Stats */
        .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
        .stat-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
        .stat-card .label { font-size: 13px; color: #888; margin-bottom: 8px; }
        .stat-card .value { font-size: 30px; font-weight: 700; color: #1e293b; }
        .stat-card.active .value { color: #16a34a; }
        .stat-card.expired .value { color: #dc2626; }
        .stat-card.pending .value { color: #d97706; }

        /* Filter */
        .filter-bar { background: white; padding: 16px 20px; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06); margin-bottom: 20px; display: flex; gap: 12px; flex-wrap: wrap; align-items: flex-end; }
        .filter-bar input, .filter-bar select { padding: 9px 12px; border: 1px solid #ddd; border-radius: 7px; font-size: 14px; outline: none; min-width: 180px; }
        .filter-bar input:focus, .filter-bar select:focus { border-color: #2563eb; }

        /* Table */
        .card { background: white; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06); overflow: hidden; }
        table { width: 100%; border-collapse: collapse; font-size: 14px; }
        thead tr { background: #f8fafc; }
        th { padding: 14px 16px; text-align: left; font-size: 12px; font-weight: 600; color: #888; text-transform: uppercase; letter-spacing: .5px; border-bottom: 1px solid #f0f0f0; }
        td { padding: 14px 16px; border-bottom: 1px solid #f9f9f9; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background: #fafafa; }
        .empty { text-align: center; padding: 40px; color: #aaa; }
        .empty-icon { font-size: 48px; margin-bottom: 12px; }

        /* Status badges */
        .badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-active   { background: #d1fae5; color: #065f46; }
        .badge-expired  { background: #fee2e2; color: #991b1b; }
        .badge-pending  { background: #fef3c7; color: #92400e; }
        .badge-repair   { background: #dbeafe; color: #1e40af; }
        .badge-cancelled{ background: #f1f5f9; color: #64748b; }

        /* Flash */
        .flash-success { background: #d1fae5; border: 1px solid #6ee7b7; color: #065f46; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }
        .flash-error   { background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }

        @media (max-width: 768px) {
            .stats-row { grid-template-columns: 1fr 1fr; }
        }
    </style>

<div class="container">

    <div class="page-header">
        <div>
            <h1>🛡 Quản lý Bảo hành</h1>
            <p>Theo dõi phiếu bảo hành sản phẩm đồng hồ.</p>
        </div>
        <a href="${pageContext.request.contextPath}/manage/sales/warranty-add" class="btn btn-primary">
            ➕ Thêm phiếu bảo hành
        </a>
    </div>

    <%-- Flash message --%>
    <c:if test="${not empty sessionScope.flash}">
        <div class="flash-success">✅ ${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <%-- Thống kê nhanh --%>
    <div class="stats-row">
        <div class="stat-card">
            <div class="label">Tổng phiếu</div>
            <div class="value">${warranties.size()}</div>
        </div>
        <div class="stat-card active">
            <div class="label">🟢 Đang bảo hành</div>
            <div class="value">
                <c:set var="activeCount" value="0"/>
                <c:forEach var="w" items="${warranties}">
                    <c:if test="${w.status == 'Đang bảo hành' or w.status == 'ACTIVE'}"><c:set var="activeCount" value="${activeCount + 1}"/></c:if>
                </c:forEach>
                ${activeCount}
            </div>
        </div>
        <div class="stat-card pending">
            <div class="label">🔧 Đang sửa chữa</div>
            <div class="value">
                <c:set var="repairCount" value="0"/>
                <c:forEach var="w" items="${warranties}">
                    <c:if test="${w.status == 'Đang sửa chữa' or w.status == 'REPAIR'}"><c:set var="repairCount" value="${repairCount + 1}"/></c:if>
                </c:forEach>
                ${repairCount}
            </div>
        </div>
        <div class="stat-card expired">
            <div class="label">🔴 Hết hạn / Từ chối</div>
            <div class="value">
                <c:set var="expiredCount" value="0"/>
                <c:forEach var="w" items="${warranties}">
                    <c:if test="${w.status == 'Hết hạn' or w.status == 'Từ chối bảo hành' or w.status == 'EXPIRED' or w.status == 'REJECTED'}"><c:set var="expiredCount" value="${expiredCount + 1}"/></c:if>
                </c:forEach>
                ${expiredCount}
            </div>
        </div>
    </div>

    <%-- Filter --%>
    <form method="get" action="${pageContext.request.contextPath}/manage/sales/warranty">
        <div class="filter-bar">
            <input type="text" name="keyword" value="${param.keyword}" placeholder="🔍 Tìm mã đơn, tên KH, serial..."/>
            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="Đang bảo hành"   ${param.status == 'Đang bảo hành'   ? 'selected' : ''}>Đang bảo hành</option>
                <option value="Đang sửa chữa"   ${param.status == 'Đang sửa chữa'   ? 'selected' : ''}>Đang sửa chữa</option>
                <option value="Đã sửa xong"     ${param.status == 'Đã sửa xong'     ? 'selected' : ''}>Đã sửa xong</option>
                <option value="Đã trả khách"     ${param.status == 'Đã trả khách'     ? 'selected' : ''}>Đã trả khách</option>
                <option value="Từ chối bảo hành" ${param.status == 'Từ chối bảo hành' ? 'selected' : ''}>Từ chối bảo hành</option>
                <option value="Hết hạn"         ${param.status == 'Hết hạn'         ? 'selected' : ''}>Hết hạn</option>
            </select>
            <button type="submit" class="btn btn-primary">Lọc</button>
            <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="btn btn-outline">Đặt lại</a>
        </div>
    </form>

    <%-- Table --%>
    <div class="card">
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Mã đơn hàng</th>
                    <th>Sản phẩm</th>
                    <th>Số Serial</th>
                    <th>Thời hạn BH</th>
                    <th>Ngày bắt đầu</th>
                    <th>Ngày hết hạn</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty warranties}">
                        <c:forEach var="w" items="${warranties}" varStatus="loop">
                            <tr>
                                <td style="color:#aaa;">${loop.count}</td>
                                <td>
                                    <span style="font-weight:600;color:#2563eb;">${w.orderCode != null ? w.orderCode : 'N/A'}</span>
                                </td>
                                <td style="font-weight:500;">${w.productName}</td>
                                <td style="font-family:monospace;color:#555;">${w.serial != null ? w.serial : '—'}</td>
                                <td style="text-align:center;font-weight:600;">${w.months} tháng</td>
                                <td>
                                    <fmt:formatDate value="${w.startDate}" pattern="dd/MM/yyyy"/>
                                </td>
                                <td>
                                    <fmt:formatDate value="${w.endDate}" pattern="dd/MM/yyyy"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${w.status == 'Đang bảo hành' or w.status == 'ACTIVE'}">
                                            <span class="badge badge-active">Đang bảo hành</span>
                                        </c:when>
                                        <c:when test="${w.status == 'Đang sửa chữa' or w.status == 'REPAIR'}">
                                            <span class="badge badge-repair">Đang sửa chữa</span>
                                        </c:when>
                                        <c:when test="${w.status == 'Đã sửa xong'}">
                                            <span class="badge badge-repair" style="background:#dbeafe; color:#1e40af;">Đã sửa xong</span>
                                        </c:when>
                                        <c:when test="${w.status == 'Đã trả khách'}">
                                            <span class="badge badge-active" style="background:#e0f2fe; color:#0369a1;">Đã trả khách</span>
                                        </c:when>
                                        <c:when test="${w.status == 'Từ chối bảo hành'}">
                                            <span class="badge badge-expired">Từ chối BH</span>
                                        </c:when>
                                        <c:when test="${w.status == 'Hết hạn' or w.status == 'EXPIRED'}">
                                            <span class="badge badge-expired">Hết hạn</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-cancelled">${w.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div style="display:flex; gap:6px;">
                                        <button type="button" class="btn btn-sm btn-outline"
                                                data-id="${w.id}"
                                                data-order-code="${w.orderCode}"
                                                data-customer="${w.customerName}"
                                                data-phone="${w.customerPhone}"
                                                data-email="${w.customerEmail}"
                                                data-product="${w.productName}"
                                                data-serial="${w.serial}"
                                                data-buy-date="<fmt:formatDate value="${w.buyDate}" pattern="dd/MM/yyyy"/>"
                                                data-months="${w.months}"
                                                data-start-date="<fmt:formatDate value="${w.startDate}" pattern="dd/MM/yyyy"/>"
                                                data-end-date="<fmt:formatDate value="${w.endDate}" pattern="dd/MM/yyyy"/>"
                                                data-status="${w.status}"
                                                data-note="${w.note}"
                                                data-receive-date="<fmt:formatDate value="${w.receiveDate}" pattern="dd/MM/yyyy"/>"
                                                data-receive-note="${w.receiveNote}"
                                                data-repair-content="${w.repairContent}"
                                                data-component-replaced="${w.componentReplaced}"
                                                data-repair-note="${w.repairNote}"
                                                data-complete-date="<fmt:formatDate value="${w.completeDate}" pattern="dd/MM/yyyy"/>"
                                                data-return-date="<fmt:formatDate value="${w.returnDate}" pattern="dd/MM/yyyy"/>"
                                                onclick="openDetailModal(event)">
                                            Chi tiết
                                        </button>
                                        
                                        <c:choose>
                                            <c:when test="${w.status == 'Đang bảo hành' or w.status == 'ACTIVE'}">
                                                <button type="button" class="btn btn-sm btn-primary"
                                                        data-id="${w.id}"
                                                        data-order-code="${w.orderCode}"
                                                        data-product="${w.productName}"
                                                        onclick="openReceiveModal(event)">
                                                    Tiếp nhận
                                                </button>
                                                <button type="button" class="btn btn-sm btn-outline" style="border-color:#dc2626; color:#dc2626;"
                                                        onclick="quickRejectWarranty(${w.id})">
                                                    Từ chối
                                                </button>
                                            </c:when>
                                            <c:when test="${w.status == 'Đang sửa chữa' or w.status == 'REPAIR'}">
                                                <button type="button" class="btn btn-sm btn-primary" style="background:#d97706; color:white; border:none;"
                                                        data-id="${w.id}"
                                                        data-order-code="${w.orderCode}"
                                                        data-product="${w.productName}"
                                                        onclick="openRepairModal(event)">
                                                    Cập nhật sửa chữa
                                                </button>
                                            </c:when>
                                            <c:when test="${w.status == 'Đã sửa xong'}">
                                                <button type="button" class="btn btn-sm btn-primary" style="background:#16a34a; color:white; border:none;"
                                                        onclick="quickReturnWarranty(${w.id})">
                                                    Trả khách
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
                            <td colspan="9" class="empty">
                                <div class="empty-icon">🛡</div>
                                <p>Chưa có phiếu bảo hành nào.</p>
                                <br/>
                                <a href="${pageContext.request.contextPath}/manage/sales/warranty-add" class="btn btn-primary btn-sm">Thêm phiếu đầu tiên</a>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <!-- ================= MODAL XEM CHI TIẾT PHIẾU BẢO HÀNH ================= -->
    <div id="detail-modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center; padding:15px;">
        <div style="background:white; border-radius:12px; max-width:600px; width:100%; box-shadow:0 10px 25px rgba(0,0,0,0.25); padding:25px; position:relative; max-height:90vh; overflow-y:auto; text-align:left;">
            <span style="position:absolute; top:15px; right:15px; font-size:24px; color:#aaa; cursor:pointer; font-weight:bold;" onclick="closeModal('detail-modal')">&times;</span>
            <h2 style="margin-top:0; margin-bottom:20px; font-size:20px; color:#1e293b; border-bottom:1px solid #e2e8f0; padding-bottom:12px;">Chi tiết phiếu bảo hành <span id="modal-id-text" style="color:#2563eb;"></span></h2>
            
            <div style="display:grid; grid-template-columns:1fr; gap:16px; font-size:14px;">
                <!-- TRẠNG THÁI -->
                <div style="display:flex; justify-content:space-between; align-items:center; background:#f8fafc; padding:10px 15px; border-radius:8px; border:1px solid #e2e8f0;">
                    <span style="font-weight:600; color:#475569;">Trạng thái bảo hành:</span>
                    <span id="modal-status-badge" class="badge"></span>
                </div>

                <!-- KHÁCH HÀNG & ĐƠN HÀNG -->
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:15px; background:#f8fafc; padding:15px; border-radius:8px; border:1px solid #e2e8f0;">
                    <div>
                        <strong style="color:#0f172a; display:block; margin-bottom:6px; font-size:13px; text-transform:uppercase;">👤 Khách hàng</strong>
                        <span id="modal-customer" style="font-weight:bold; color:#1e293b;"></span><br>
                        SĐT: <span id="modal-phone"></span><br>
                        Email: <span id="modal-email"></span>
                    </div>
                    <div>
                        <strong style="color:#0f172a; display:block; margin-bottom:6px; font-size:13px; text-transform:uppercase;">📦 Thông tin mua hàng</strong>
                        Mã đơn: <span id="modal-order-code" style="font-weight:bold; color:#2563eb;"></span><br>
                        Ngày mua: <span id="modal-buy-date"></span><br>
                        Thời hạn: <span id="modal-months" style="font-weight:bold;"></span> tháng
                    </div>
                </div>

                <!-- SẢN PHẨM & THỜI HẠN -->
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:15px; background:#f8fafc; padding:15px; border-radius:8px; border:1px solid #e2e8f0;">
                    <div>
                        <strong style="color:#0f172a; display:block; margin-bottom:4px; font-size:13px; text-transform:uppercase;">⌚ Sản phẩm & Serial</strong>
                        Tên sản phẩm: <span id="modal-product" style="font-weight:bold;"></span><br>
                        Số Serial: <span id="modal-serial" style="font-family:monospace; font-weight:bold; color:#ef4444;"></span>
                    </div>
                    <div>
                        <strong style="color:#0f172a; display:block; margin-bottom:4px; font-size:13px; text-transform:uppercase;">📅 Ngày bảo hành</strong>
                        Ngày bắt đầu: <span id="modal-start-date"></span><br>
                        Ngày kết thúc: <span id="modal-end-date" style="color:#dc2626; font-weight:bold;"></span>
                    </div>
                </div>

                <!-- LÝ DO BẢO HÀNH BAN ĐẦU -->
                <div style="background:#f1f5f9; padding:15px; border-radius:8px; border:1px solid #cbd5e1;">
                    <strong style="color:#475569; display:block; margin-bottom:4px; font-size:13px; text-transform:uppercase;">📝 Nội dung / Lý do bảo hành ban đầu</strong>
                    <span id="modal-note" style="font-style:italic;"></span>
                </div>

                <!-- TIẾP NHẬN BẢO HÀNH -->
                <div id="modal-receive-section" style="background:#fffbeb; padding:15px; border-radius:8px; border:1px solid #fef3c7; display:none;">
                    <strong style="color:#92400e; display:block; margin-bottom:6px; font-size:13px; text-transform:uppercase;">🔧 Thông tin tiếp nhận sửa chữa</strong>
                    Ngày tiếp nhận: <span id="modal-receive-date" style="font-weight:bold;"></span><br>
                    Nội dung/Tình trạng máy khi nhận:<br>
                    <p id="modal-receive-note" style="margin-top:4px; background:white; padding:8px; border-radius:4px; border:1px solid #fde047; font-style:italic;"></p>
                </div>

                <!-- KẾT QUẢ SỬA CHỮA -->
                <div id="modal-repair-section" style="background:#eff6ff; padding:15px; border-radius:8px; border:1px solid #bfdbfe; display:none;">
                    <strong style="color:#1e40af; display:block; margin-bottom:6px; font-size:13px; text-transform:uppercase;">⚙️ Kết quả sửa chữa</strong>
                    Ngày hoàn thành: <span id="modal-complete-date" style="font-weight:bold;"></span><br>
                    Nội dung đã sửa: <span id="modal-repair-content" style="font-weight:bold;"></span><br>
                    Linh kiện đã thay: <span id="modal-component" style="font-weight:bold; color:#2563eb;"></span><br>
                    Ghi chú sửa chữa: <span id="modal-repair-note" style="font-style:italic;"></span>
                </div>

                <!-- NGÀY TRẢ KHÁCH -->
                <div id="modal-return-section" style="background:#ecfdf5; padding:12px; border-radius:8px; border:1px solid #a7f3d0; display:none;">
                    🚚 <strong>Ngày đã trả sản phẩm cho khách:</strong> <span id="modal-return-date" style="font-weight:bold; color:#065f46;"></span>
                </div>
            </div>
            
            <div style="display:flex; justify-content:flex-end; gap:10px; border-top:1px solid #e2e8f0; padding-top:15px; margin-top:15px;">
                <button type="button" class="btn" style="background:#e2e8f0; color:#334155; height:36px; padding:0 18px; cursor:pointer; border:none; border-radius:6px; font-weight:bold;" onclick="closeModal('detail-modal')">Đóng</button>
            </div>
        </div>
    </div>

    <!-- ================= MODAL TIẾP NHẬN BẢO HÀNH ================= -->
    <div id="receive-modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center; padding:15px;">
        <div style="background:white; border-radius:12px; max-width:500px; width:100%; box-shadow:0 10px 25px rgba(0,0,0,0.25); padding:25px; position:relative; text-align:left;">
            <span style="position:absolute; top:15px; right:15px; font-size:24px; color:#aaa; cursor:pointer; font-weight:bold;" onclick="closeModal('receive-modal')">&times;</span>
            <h2 style="margin-top:0; margin-bottom:20px; font-size:20px; color:#1e293b; border-bottom:1px solid #e2e8f0; padding-bottom:12px;">Tiếp nhận bảo hành <span id="receive-order-code" style="color:#2563eb;"></span></h2>
            
            <form method="post" action="${pageContext.request.contextPath}/manage/sales/warranty">
                <input type="hidden" name="id" id="receive-id">
                <input type="hidden" name="action" value="receive">
                
                <div style="margin-bottom:12px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Ngày tiếp nhận:</label>
                    <input type="date" name="receiveDate" id="receive-date-input" required style="width:100%; height:38px; padding:0 10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none;">
                </div>
                
                <div style="margin-bottom:20px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Lý do lỗi & Tình trạng khi nhận:</label>
                    <textarea name="receiveNote" placeholder="Mô tả chi tiết lỗi và vết trầy xước bên ngoài của đồng hồ khi nhận..." required style="width:100%; height:80px; padding:10px; border:1px solid #cbd5e1; border-radius:6px; font-family:inherit; font-size:13px; resize:vertical; outline:none;"></textarea>
                </div>
                
                <div style="display:flex; justify-content:flex-end; gap:10px; border-top:1px solid #e2e8f0; padding-top:15px;">
                    <button type="submit" class="btn" style="background:#2563eb; color:white; height:36px; padding:0 18px; font-weight:bold; cursor:pointer; border:none; border-radius:6px;">🔧 Xác nhận tiếp nhận</button>
                    <button type="button" class="btn" style="background:#e2e8f0; color:#334155; height:36px; padding:0 18px; cursor:pointer; border:none; border-radius:6px;" onclick="closeModal('receive-modal')">Đóng</button>
                </div>
            </form>
        </div>
    </div>

    <!-- ================= MODAL CẬP NHẬT SỬA CHỮA ================= -->
    <div id="repair-modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center; padding:15px;">
        <div style="background:white; border-radius:12px; max-width:500px; width:100%; box-shadow:0 10px 25px rgba(0,0,0,0.25); padding:25px; position:relative; text-align:left;">
            <span style="position:absolute; top:15px; right:15px; font-size:24px; color:#aaa; cursor:pointer; font-weight:bold;" onclick="closeModal('repair-modal')">&times;</span>
            <h2 style="margin-top:0; margin-bottom:20px; font-size:20px; color:#1e293b; border-bottom:1px solid #e2e8f0; padding-bottom:12px;">Cập nhật kết quả sửa chữa <span id="repair-order-code" style="color:#d97706;"></span></h2>
            
            <form method="post" action="${pageContext.request.contextPath}/manage/sales/warranty">
                <input type="hidden" name="id" id="repair-id">
                <input type="hidden" name="action" value="repair">
                
                <div style="margin-bottom:12px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Nội dung đã sửa chữa:</label>
                    <textarea name="repairContent" placeholder="Ví dụ: Lau dầu bộ máy, căn chỉnh độ sai giờ..." required style="width:100%; height:60px; padding:10px; border:1px solid #cbd5e1; border-radius:6px; font-family:inherit; font-size:13px; resize:vertical; outline:none;"></textarea>
                </div>
                
                <div style="margin-bottom:12px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Linh kiện thay thế (nếu có):</label>
                    <input type="text" name="componentReplaced" placeholder="Ví dụ: Thay mặt kính sapphire, thay gioăng chống nước..." style="width:100%; height:38px; padding:0 10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none;">
                </div>

                <div style="margin-bottom:12px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Ghi chú thêm:</label>
                    <input type="text" name="repairNote" placeholder="Ghi chú về kiểm tra áp suất nước..." style="width:100%; height:38px; padding:0 10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none;">
                </div>

                <div style="margin-bottom:20px;">
                    <label style="font-weight:600; display:block; margin-bottom:5px; font-size:14px;">Ngày hoàn thành sửa chữa:</label>
                    <input type="date" name="completeDate" id="repair-date-input" required style="width:100%; height:38px; padding:0 10px; border:1px solid #cbd5e1; border-radius:6px; font-size:14px; outline:none;">
                </div>
                
                <div style="display:flex; justify-content:flex-end; gap:10px; border-top:1px solid #e2e8f0; padding-top:15px;">
                    <button type="submit" class="btn" style="background:#d97706; color:white; height:36px; padding:0 18px; font-weight:bold; cursor:pointer; border:none; border-radius:6px;">💾 Xác nhận hoàn thành</button>
                    <button type="button" class="btn" style="background:#e2e8f0; color:#334155; height:36px; padding:0 18px; cursor:pointer; border:none; border-radius:6px;" onclick="closeModal('repair-modal')">Đóng</button>
                </div>
            </form>
        </div>
    </div>

    <!-- HIDDEN FORM FOR QUICK STATE UPDATES -->
    <form id="action-form" method="post" action="${pageContext.request.contextPath}/manage/sales/warranty" style="display:none;">
        <input type="hidden" name="id" id="action-id">
        <input type="hidden" name="action" id="action-type">
        <input type="hidden" name="status" id="action-status">
        <input type="hidden" name="returnDate" id="action-return-date">
    </form>

    <script>
        // Set default dates to today
        document.addEventListener("DOMContentLoaded", function() {
            var today = new Date().toISOString().split('T')[0];
            if (document.getElementById('receive-date-input')) {
                document.getElementById('receive-date-input').value = today;
            }
            if (document.getElementById('repair-date-input')) {
                document.getElementById('repair-date-input').value = today;
            }
        });

        function submitWarrantyAction(id, status) {
            document.getElementById('action-id').value = id;
            document.getElementById('action-type').value = '';
            document.getElementById('action-status').value = status;
            document.getElementById('action-form').submit();
        }

        function quickRejectWarranty(id) {
            if (confirm("Xác nhận từ chối bảo hành cho sản phẩm này?")) {
                document.getElementById('action-id').value = id;
                document.getElementById('action-type').value = '';
                document.getElementById('action-status').value = 'Từ chối bảo hành';
                document.getElementById('action-form').submit();
            }
        }

        function quickReturnWarranty(id) {
            if (confirm("Xác nhận đã bàn giao lại sản phẩm đã sửa xong cho khách hàng?")) {
                var today = new Date().toISOString().split('T')[0];
                document.getElementById('action-id').value = id;
                document.getElementById('action-type').value = 'return';
                document.getElementById('action-return-date').value = today;
                document.getElementById('action-form').submit();
            }
        }

        function openReceiveModal(event) {
            var btn = event.currentTarget;
            var id = btn.getAttribute('data-id');
            var code = btn.getAttribute('data-order-code');
            var product = btn.getAttribute('data-product');
            
            document.getElementById('receive-id').value = id;
            document.getElementById('receive-order-code').innerText = code ? '#' + code : '#' + id;
            document.getElementById('receive-modal').style.display = 'flex';
        }

        function openRepairModal(event) {
            var btn = event.currentTarget;
            var id = btn.getAttribute('data-id');
            var code = btn.getAttribute('data-order-code');
            var product = btn.getAttribute('data-product');
            
            document.getElementById('repair-id').value = id;
            document.getElementById('repair-order-code').innerText = code ? '#' + code : '#' + id;
            document.getElementById('repair-modal').style.display = 'flex';
        }

        function openDetailModal(event) {
            var btn = event.currentTarget;
            var id = btn.getAttribute('data-id');
            var orderCode = btn.getAttribute('data-order-code');
            var customer = btn.getAttribute('data-customer');
            var phone = btn.getAttribute('data-phone');
            var email = btn.getAttribute('data-email');
            var product = btn.getAttribute('data-product');
            var serial = btn.getAttribute('data-serial');
            var buyDate = btn.getAttribute('data-buy-date');
            var months = btn.getAttribute('data-months');
            var startDate = btn.getAttribute('data-start-date');
            var endDate = btn.getAttribute('data-end-date');
            var status = btn.getAttribute('data-status');
            
            var receiveDate = btn.getAttribute('data-receive-date');
            var receiveNote = btn.getAttribute('data-receive-note');
            var repairContent = btn.getAttribute('data-repair-content');
            var componentReplaced = btn.getAttribute('data-component-replaced');
            var repairNote = btn.getAttribute('data-repair-note');
            var completeDate = btn.getAttribute('data-complete-date');
            var returnDate = btn.getAttribute('data-return-date');
            var note = btn.getAttribute('data-note');
            
            document.getElementById('modal-id-text').innerText = '#' + id;
            document.getElementById('modal-customer').innerText = customer || 'Chưa rõ';
            document.getElementById('modal-phone').innerText = phone || 'Chưa rõ';
            document.getElementById('modal-email').innerText = email || 'Chưa rõ';
            document.getElementById('modal-order-code').innerText = orderCode ? '#' + orderCode : 'N/A';
            document.getElementById('modal-buy-date').innerText = buyDate || 'Chưa rõ';
            document.getElementById('modal-months').innerText = months || '12';
            document.getElementById('modal-product').innerText = product || '—';
            document.getElementById('modal-serial').innerText = serial || '—';
            document.getElementById('modal-start-date').innerText = startDate || '—';
            document.getElementById('modal-end-date').innerText = endDate || '—';
            document.getElementById('modal-note').innerText = note || 'Không có ghi chú lỗi ban đầu.';
            
            // Status badge rendering
            var badge = document.getElementById('modal-status-badge');
            badge.className = 'badge';
            badge.style.background = '';
            badge.style.color = '';
            
            if (status === 'Đang bảo hành' || status === 'ACTIVE') {
                badge.innerText = 'Đang bảo hành';
                badge.classList.add('badge-active');
            } else if (status === 'Đang sửa chữa' || status === 'REPAIR') {
                badge.innerText = 'Đang sửa chữa';
                badge.classList.add('badge-repair');
            } else if (status === 'Đã sửa xong') {
                badge.innerText = 'Đã sửa xong';
                badge.classList.add('badge-repair');
                badge.style.background = '#dbeafe';
                badge.style.color = '#1e40af';
            } else if (status === 'Đã trả khách') {
                badge.innerText = 'Đã trả khách';
                badge.classList.add('badge-active');
                badge.style.background = '#e0f2fe';
                badge.style.color = '#0369a1';
            } else if (status === 'Từ chối bảo hành') {
                badge.innerText = 'Từ chối BH';
                badge.classList.add('badge-expired');
            } else if (status === 'Hết hạn' || status === 'EXPIRED') {
                badge.innerText = 'Hết hạn';
                badge.classList.add('badge-expired');
            } else {
                badge.innerText = status || 'Chờ xử lý';
                badge.classList.add('badge-cancelled');
            }

            // Receive section
            if (receiveDate && receiveDate.trim() !== '') {
                document.getElementById('modal-receive-date').innerText = receiveDate;
                document.getElementById('modal-receive-note').innerText = receiveNote || '—';
                document.getElementById('modal-receive-section').style.display = 'block';
            } else {
                document.getElementById('modal-receive-section').style.display = 'none';
            }
            
            // Repair section
            if (completeDate && completeDate.trim() !== '') {
                document.getElementById('modal-complete-date').innerText = completeDate;
                document.getElementById('modal-repair-content').innerText = repairContent || '—';
                document.getElementById('modal-component').innerText = componentReplaced || 'Không thay linh kiện';
                document.getElementById('modal-repair-note').innerText = repairNote || 'Không';
                document.getElementById('modal-repair-section').style.display = 'block';
            } else {
                document.getElementById('modal-repair-section').style.display = 'none';
            }
            
            // Return section
            if (returnDate && returnDate.trim() !== '') {
                document.getElementById('modal-return-date').innerText = returnDate;
                document.getElementById('modal-return-section').style.display = 'block';
            } else {
                document.getElementById('modal-return-section').style.display = 'none';
            }
            
            document.getElementById('detail-modal').style.display = 'flex';
        }

        function closeModal(modalId) {
            document.getElementById(modalId).style.display = 'none';
        }
    </script>
</div>

