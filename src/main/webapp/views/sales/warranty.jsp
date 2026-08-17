<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />

<style>
    /* Modal Styling */
    .modal-backdrop {
        display: none;
        position: fixed;
        top: 0; left: 0; width: 100%; height: 100%;
        background: rgba(15, 23, 42, 0.6);
        backdrop-filter: blur(4px);
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }
    .modal-box {
        background: white;
        border-radius: 12px;
        width: 100%;
        max-width: 550px;
        box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
        overflow: hidden;
        animation: modalFadeIn 0.25s ease-out;
    }
    @keyframes modalFadeIn {
        from { transform: scale(0.95); opacity: 0; }
        to { transform: scale(1); opacity: 1; }
    }
    .modal-header {
        background: #f8fafc;
        padding: 16px 20px;
        border-bottom: 1px solid #e2e8f0;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .modal-header h3 {
        margin: 0;
        font-size: 16px;
        color: #0f172a;
        font-weight: 700;
    }
    .modal-close {
        background: none;
        border: none;
        font-size: 20px;
        color: #64748b;
        cursor: pointer;
    }
    .modal-close:hover {
        color: #0f172a;
    }
    .modal-body {
        padding: 20px;
    }
    .modal-footer {
        background: #f8fafc;
        padding: 12px 20px;
        border-top: 1px solid #e2e8f0;
        display: flex;
        justify-content: flex-end;
        gap: 10px;
    }
    .form-group {
        margin-bottom: 16px;
        display: flex;
        flex-direction: column;
        gap: 6px;
    }
    .form-group label {
        font-size: 13px;
        font-weight: 600;
        color: #475569;
    }
    .form-group input, .form-group select, .form-group textarea {
        padding: 10px 12px;
        border: 1px solid #cbd5e1;
        border-radius: 6px;
        font-size: 13px;
        outline: none;
        width: 100%;
    }
    .form-group input:focus, .form-group select:focus, .form-group textarea:focus {
        border-color: #2563eb;
    }
    .btn {
        height: 38px;
        padding: 0 16px;
        border-radius: 6px;
        border: none;
        font-size: 13px;
        font-weight: 600;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        text-decoration: none;
    }
    .btn-primary { background: #2563eb; color: white; }
    .btn-primary:hover { background: #1d4ed8; }
    .btn-secondary { background: #e2e8f0; color: #475569; }
    .btn-secondary:hover { background: #cbd5e1; }
    .btn-danger { background: #ef4444; color: white; }
    .btn-danger:hover { background: #dc2626; }
    .btn-success { background: #16a34a; color: white; }
    .btn-success:hover { background: #15803d; }
    .flash-alert {
        background: #f0fdf4; border: 1px solid #bbf7d0; color: #166534;
        padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px;
    }
</style>

<div class="content-header" style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px;">
    <div class="header-left">
        <h2>🛡 ${moduleTitle}</h2>
        <p class="text-muted" style="margin-top: 4px;">Duyệt yêu cầu bảo hành online hoặc lập phiếu biên nhận trực tiếp tại quầy.</p>
    </div>
    <div>
        <button type="button" class="btn btn-primary" onclick="openCreateModal()">
            ➕ Tạo phiếu bảo hành
        </button>
    </div>
</div>

<c:if test="${not empty sessionScope.flash}">
    <div class="flash-alert">${sessionScope.flash}</div>
    <c:remove var="flash" scope="session"/>
</c:if>

<form id="state-form" method="post" action="" style="display:none;">
    <input type="hidden" name="id" id="state-form-id">
</form>

<div class="table-card" style="background:white; border-radius:10px; box-shadow:0 1px 3px rgba(0,0,0,0.1); overflow:hidden;">
    <table class="data-table" style="width:100%; border-collapse:collapse;">
        <thead>
            <tr style="background:#f8fafc; border-bottom:1px solid #e2e8f0;">
                <th style="padding:14px; text-align:left;">MÃ PHIẾU</th>
                <th style="padding:14px; text-align:left;">MÃ ĐƠN HÀNG</th>
                <th style="padding:14px; text-align:left;">KHÁCH HÀNG</th>
                <th style="padding:14px; text-align:left;">SẢN PHẨM</th>
                <th style="padding:14px; text-align:left;">NGÀY TẠO</th>
                <th style="padding:14px; text-align:left;">LÝ DO/CHI TIẾT</th>
                <th style="padding:14px; text-align:left;">TRẠNG THÁI</th>
                <th style="padding:14px; text-align:center;">THAO TÁC</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${warranties}" var="war">
                <tr style="border-bottom:1px solid #f1f5f9;">
                    <td style="padding:14px;"><b>${war.returnCode}</b></td>
                    <td style="padding:14px;"><a href="${cp}/manage/sales/orders?keyword=${war.orderCode}" style="color:#2563eb; text-decoration:none; font-weight:600;">${war.orderCode}</a></td>
                    <td style="padding:14px;">${war.customerName}</td>
                    <td style="padding:14px;"><b>${war.productName}</b></td>
                    <td style="padding:14px;">
                        <fmt:formatDate value="${war.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                    </td>
                    <td style="padding:14px; max-width:200px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="${war.reason}">${war.reason}</td>
                    <td style="padding:14px;">
                        <c:choose>
                            <c:when test="${war.status == 'PENDING'}">
                                <span style="background:#fef3c7; color:#d97706; padding:4px 10px; border-radius:20px; font-size:11px; font-weight:600; display:inline-block;">Chờ duyệt</span>
                            </c:when>
                            <c:when test="${war.status == 'APPROVED'}">
                                <span style="background:#dcfce7; color:#15803d; padding:4px 10px; border-radius:20px; font-size:11px; font-weight:600; display:inline-block;">Đã duyệt</span>
                            </c:when>
                            <c:when test="${war.status == 'RECEIVED'}">
                                <span style="background:#e0f2fe; color:#0369a1; padding:4px 10px; border-radius:20px; font-size:11px; font-weight:600; display:inline-block;">Đang bảo hành</span>
                            </c:when>
                            <c:when test="${war.status == 'COMPLETED'}">
                                <span style="background:#f1f5f9; color:#475569; padding:4px 10px; border-radius:20px; font-size:11px; font-weight:600; display:inline-block;">Hoàn thành</span>
                            </c:when>
                            <c:when test="${war.status == 'REJECTED'}">
                                <span style="background:#fee2e2; color:#b91c1c; padding:4px 10px; border-radius:20px; font-size:11px; font-weight:600; display:inline-block;">Từ chối</span>
                            </c:when>
                            <c:when test="${war.status == 'CANCELLED'}">
                                <span style="background:#f1f5f9; color:#94a3b8; padding:4px 10px; border-radius:20px; font-size:11px; font-weight:600; display:inline-block;">Đã hủy</span>
                            </c:when>
                            <c:otherwise>
                                <span style="background:#f1f5f9; color:#475569; padding:4px 10px; border-radius:20px; font-size:11px; font-weight:600; display:inline-block;">${war.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td style="padding:14px; text-align:center;">
                        <div style="display:flex; justify-content:center; gap:6px; align-items:center; flex-wrap:nowrap;">
                            <c:choose>
                                <c:when test="${war.status == 'PENDING'}">
                                    <button type="button" class="btn btn-success" style="height:28px; padding:0 8px; font-size:12px; background:#16a34a;" 
                                            onclick="openApproveModal('${war.returnRequestId}', '${war.returnCode}', '${war.orderCode}', '${war.customerName}', '${war.reason}', '${war.evidenceNote}', false)">
                                        Duyệt / Từ chối
                                    </button>
                                    <button type="button" class="btn btn-danger" style="height:28px; padding:0 8px; font-size:12px;" 
                                            onclick="submitStateAction('${cp}/manage/sales/warranty/cancel', '${war.returnRequestId}', 'Bạn có chắc muốn HỦY phiếu bảo hành này?')">
                                        Hủy
                                    </button>
                                </c:when>
                                <c:when test="${war.status == 'APPROVED'}">
                                    <button type="button" class="btn btn-primary" style="height:28px; padding:0 8px; font-size:12px;" 
                                            onclick="submitStateAction('${cp}/manage/sales/warranty/receive', '${war.returnRequestId}', 'Bạn có chắc muốn TIẾP NHẬN bảo hành cho sản phẩm này?')">
                                        Tiếp nhận bảo hành
                                    </button>
                                </c:when>
                                <c:when test="${war.status == 'RECEIVED'}">
                                    <button type="button" class="btn btn-success" style="height:28px; padding:0 8px; font-size:12px; background:#16a34a;" 
                                            onclick="submitStateAction('${cp}/manage/sales/warranty/complete', '${war.returnRequestId}', 'Bạn có chắc muốn XÁC NHẬN bảo hành thành công cho sản phẩm này?')">
                                        Xác nhận bảo hành thành công
                                    </button>
                                </c:when>
                            </c:choose>
                            <button type="button" class="btn btn-secondary" style="height:28px; padding:0 8px; font-size:12px;" 
                                    onclick="openApproveModal('${war.returnRequestId}', '${war.returnCode}', '${war.orderCode}', '${war.customerName}', '${war.reason}', '${war.evidenceNote}', true)">
                                Xem
                            </button>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty warranties}">
                <tr>
                    <td colspan="8" class="text-center text-muted" style="padding: 3rem; text-align:center; color:#64748b;">
                        Không có yêu cầu bảo hành nào.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>

<!-- MODAL 1: DUYỆT BẢO HÀNH ONLINE -->
<div class="modal-backdrop" id="approveModal">
    <div class="modal-box">
        <div class="modal-header">
            <h3>🛡 Xem & Duyệt yêu cầu bảo hành</h3>
            <button class="modal-close" onclick="closeApproveModal()">×</button>
        </div>
        <div class="modal-body">
            <div style="font-size: 13px; color: #475569; display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 15px;">
                <div>Mã yêu cầu: <b id="appCode" style="color:#0f172a;"></b></div>
                <div>Đơn hàng: <b id="appOrder" style="color:#0f172a;"></b></div>
                <div style="grid-column: 1/-1;">Khách hàng: <b id="appCustomer" style="color:#0f172a;"></b></div>
            </div>
            
            <div style="margin-bottom:15px;">
                <label style="font-weight: 600; font-size:12px; color:#475569; display:block; margin-bottom:4px;">Lý do lỗi (Khách báo):</label>
                <div id="appReason" style="background:#f8fafc; border:1px solid #e2e8f0; padding:10px; border-radius:6px; font-size:13px; min-height:50px;"></div>
            </div>

            <div style="margin-bottom: 15px;">
                <label style="font-weight: 600; font-size:12px; color:#475569; display: block; margin-bottom: 4px;">Hình ảnh minh chứng:</label>
                <div style="text-align: center; background: #fafafa; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 10px;">
                    <img id="appEvidenceImg" src="" style="max-width: 100%; max-height: 220px; object-fit: contain; border-radius: 6px;" alt="Không có ảnh minh chứng">
                </div>
            </div>

            <!-- Form Reject (Hidden by default, shown when clicking Từ chối) -->
            <div id="rejectSection" style="display:none; border-top:1px dashed #cbd5e1; padding-top:15px; margin-top:15px;">
                <form id="rejectForm" method="post" action="${cp}/manage/sales/warranty/reject">
                    <input type="hidden" name="id" id="rejectId">
                    <div class="form-group">
                        <label for="rejectReason" style="color:#b91c1c;">Lý do từ chối bảo hành <span style="color:red;">*</span></label>
                        <textarea id="rejectReason" name="rejectReason" rows="3" placeholder="Bắt buộc nhập lý do từ chối chi tiết..." required></textarea>
                    </div>
                    <div style="display:flex; justify-content:flex-end; gap:8px;">
                        <button type="button" class="btn btn-secondary" onclick="hideRejectSection()">Quay lại</button>
                        <button type="submit" class="btn btn-danger">Xác nhận Từ chối</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="modal-footer" id="approveActions">
            <form id="approveForm" method="post" action="${cp}/manage/sales/warranty/approve" style="margin:0;">
                <input type="hidden" name="id" id="approveId">
                <button type="submit" class="btn btn-success">Đồng ý duyệt</button>
            </form>
            <button type="button" class="btn btn-danger" onclick="showRejectSection()">Từ chối</button>
            <button type="button" class="btn btn-secondary" onclick="closeApproveModal()">Hủy</button>
        </div>
    </div>
</div>

<!-- MODAL 2: TẠO MỚI PHIẾU BẢO HÀNH TẠI CỬA HÀNG -->
<div class="modal-backdrop" id="createModal">
    <div class="modal-box">
        <div class="modal-header">
            <h3>🛡 Lập phiếu bảo hành trực tiếp</h3>
            <button class="modal-close" onclick="closeCreateModal()">×</button>
        </div>
        <form id="createForm" method="post" action="${cp}/manage/sales/warranty/create" enctype="multipart/form-data" onsubmit="return validateCreateForm()">
            <div class="modal-body">
                
                <div class="form-group">
                    <label for="orderIdOrPhone">Mã đơn hàng / SĐT khách <span style="color:red;">*</span></label>
                    <div style="display:flex; gap:8px;">
                        <input type="text" id="orderIdOrPhone" name="orderIdOrPhone" placeholder="Ví dụ: 1001 hoặc 0988000005" required>
                        <button type="button" class="btn btn-primary" onclick="lookupOrderItems()">Tìm</button>
                    </div>
                </div>

                <!-- Hiển thị thông tin đơn hàng sau khi tìm thấy -->
                <div id="foundOrderInfo" style="display:none; margin-bottom:15px; padding:12px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:6px; font-size:13px;">
                    <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
                        <div>Mã đơn: <b id="foundOrderCode" style="color:#0f172a;"></b></div>
                        <div>Ngày mua: <span id="foundOrderDate" style="color:#0f172a;"></span></div>
                        <div style="grid-column: 1/-1;">Khách hàng: <b id="foundCustomerName" style="color:#0f172a;"></b> (<span id="foundCustomerPhone"></span>)</div>
                    </div>
                </div>

                <div class="form-group">
                    <label for="variantId">Chọn Đồng hồ <span style="color:red;">*</span></label>
                    <select id="variantId" name="variantId" required>
                        <option value="">-- Vui lòng bấm Tìm đơn trước --</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="reason">Mô tả lỗi sản phẩm <span style="color:red;">*</span></label>
                    <textarea id="reason" name="reason" rows="3" placeholder="Nhập chi tiết tình trạng lỗi của đồng hồ..." required></textarea>
                </div>

                <div class="form-group">
                    <label for="evidenceImage">Tải ảnh chụp trạng thái đồng hồ <span style="color:#64748b;">(Tùy chọn)</span></label>
                    <input type="file" id="evidenceImage" name="evidenceImage" accept="image/*">
                    <small style="color:#64748b; font-size:11px;">Chụp ảnh chi tiết mặt số, dây đeo hoặc vết xước để làm căn cứ.</small>
                </div>

            </div>
            <div class="modal-footer">
                <button type="submit" class="btn btn-primary">Xác nhận tạo phiếu</button>
                <button type="button" class="btn btn-secondary" onclick="closeCreateModal()">Hủy</button>
            </div>
        </form>
    </div>
</div>

<script>
    const contextPath = '${pageContext.request.contextPath}';

    function submitStateAction(actionUrl, id, confirmMessage) {
        if (!confirmMessage || confirm(confirmMessage)) {
            let form = document.getElementById("state-form");
            form.action = actionUrl;
            document.getElementById("state-form-id").value = id;
            form.submit();
        }
    }

    // Modal 1: Approve actions
    function openApproveModal(requestId, returnCode, orderCode, customerName, reason, evidenceNote, isViewOnly) {
        document.getElementById("approveId").value = requestId;
        document.getElementById("rejectId").value = requestId;

        document.getElementById("appCode").textContent = returnCode;
        document.getElementById("appOrder").textContent = orderCode;
        document.getElementById("appCustomer").textContent = customerName;
        document.getElementById("appReason").textContent = reason;

        let imgEl = document.getElementById("appEvidenceImg");
        if (evidenceNote && evidenceNote.trim() !== "" && evidenceNote !== "null") {
            imgEl.src = contextPath + "/" + evidenceNote;
            imgEl.style.display = "block";
        } else {
            imgEl.src = contextPath + "/assets/images/seiko-5.jpg"; // fallback placeholder
        }

        hideRejectSection();

        let footer = document.getElementById("approveActions");
        if (isViewOnly) {
            footer.innerHTML = '<button type="button" class="btn btn-secondary" onclick="closeApproveModal()">Đóng</button>';
        } else {
            footer.innerHTML = `
                <form id="approveForm" method="post" action="${contextPath}/manage/sales/warranty/approve" style="margin:0;">
                    <input type="hidden" name="id" value="${requestId}">
                    <button type="submit" class="btn btn-success" style="background:#16a34a; color:white;">Đồng ý duyệt</button>
                </form>
                <button type="button" class="btn btn-danger" onclick="showRejectSection()">Từ chối</button>
                <button type="button" class="btn btn-secondary" onclick="closeApproveModal()">Hủy</button>
            `;
        }

        document.getElementById("approveModal").style.display = "flex";
    }

    function closeApproveModal() {
        document.getElementById("approveModal").style.display = "none";
    }

    function showRejectSection() {
        document.getElementById("rejectSection").style.display = "block";
        document.getElementById("approveActions").style.display = "none";
        document.getElementById("rejectReason").focus();
    }

    function hideRejectSection() {
        document.getElementById("rejectSection").style.display = "none";
        document.getElementById("approveActions").style.display = "flex";
        document.getElementById("rejectReason").value = "";
    }

    // Modal 2: Create actions
    function openCreateModal() {
        document.getElementById("createForm").reset();
        let select = document.getElementById("variantId");
        select.innerHTML = '<option value="">-- Vui lòng bấm Tìm đơn trước --</option>';
        document.getElementById("createModal").style.display = "flex";
    }

    function closeCreateModal() {
        document.getElementById("createModal").style.display = "none";
    }

    function lookupOrderItems() {
        let inputVal = document.getElementById("orderIdOrPhone").value.trim();
        if (inputVal === "") {
            alert("Vui lòng điền mã đơn hàng hoặc SĐT khách trước!");
            return;
        }

        let url = contextPath + '/manage/sales/warranty/get-items?orderIdOrPhone=' + encodeURIComponent(inputVal);
        let select = document.getElementById("variantId");
        let infoDiv = document.getElementById("foundOrderInfo");
        
        select.innerHTML = '<option value="">Đang tải danh sách đồng hồ...</option>';
        infoDiv.style.display = "none";

        fetch(url)
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    // Display found order info
                    document.getElementById("foundOrderCode").textContent = data.orderCode;
                    document.getElementById("foundOrderDate").textContent = data.buyDate;
                    document.getElementById("foundCustomerName").textContent = data.customerName;
                    document.getElementById("foundCustomerPhone").textContent = data.phone;
                    infoDiv.style.display = "block";

                    // Load products to dropdown select
                    select.innerHTML = '<option value="">-- Chọn sản phẩm đồng hồ --</option>';
                    data.items.forEach(item => {
                        let opt = document.createElement("option");
                        opt.value = item.variantId;
                        opt.textContent = item.name;
                        select.appendChild(opt);
                    });
                } else {
                    select.innerHTML = '<option value="">-- Không tìm thấy đồng hồ nào thuộc đơn/SĐT này --</option>';
                    alert(data.message);
                }
            })
            .catch(err => {
                console.error("Error looking up order items:", err);
                select.innerHTML = '<option value="">-- Lỗi tải danh sách --</option>';
            });
    }

    function validateCreateForm() {
        let variantId = document.getElementById("variantId").value;
        if (variantId === "") {
            alert("Vui lòng tìm đơn và chọn sản phẩm đồng hồ cần bảo hành!");
            return false;
        }
        return true;
    }

    document.addEventListener("DOMContentLoaded", function() {
        let urlParams = new URLSearchParams(window.location.search);
        let orderId = urlParams.get('orderId') || urlParams.get('orderIdOrPhone');
        if (orderId) {
            document.getElementById("orderIdOrPhone").value = orderId;
            openCreateModal();
            lookupOrderItems();
        }
    });
</script>
