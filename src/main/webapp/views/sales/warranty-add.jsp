<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<style>
    .container { max-width: 800px; margin: auto; padding: 10px 0; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { font-size: 26px; font-weight: 700; }
    .back-link { color: #2563eb; text-decoration: none; font-size: 14px; font-weight: 500; }
    .card { background: white; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0,0,0,.07); }
    .tab-bar { display: flex; gap: 8px; border-bottom: 2px solid #e2e8f0; margin-bottom: 24px; }
    .tab-btn {
        padding: 10px 20px; font-weight: 600; font-size: 14px; border: none; background: none;
        cursor: pointer; border-bottom: 3px solid transparent; margin-bottom: -2px; color: #64748b;
    }
    .tab-btn.active { color: #2563eb; border-bottom-color: #2563eb; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .form-group { margin-bottom: 4px; }
    .form-group.full { grid-column: 1 / -1; }
    label { display: block; margin-bottom: 7px; font-weight: 600; font-size: 13px; color: #555; }
    .req { color: #e53e3e; }
    input, select, textarea {
        width: 100%; padding: 10px 13px; border: 1.5px solid #e2e8f0;
        border-radius: 8px; font-size: 14px; outline: none; transition: border-color .2s;
        font-family: inherit;
    }
    input:focus, select:focus, textarea:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,.08); }
    textarea { height: 90px; resize: vertical; }
    .hint { font-size: 12px; color: #94a3b8; margin-top: 5px; }
    .actions { display: flex; gap: 12px; margin-top: 28px; }
    .btn { display: inline-flex; align-items: center; gap: 6px; padding: 11px 26px; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; text-decoration: none; }
    .btn-save   { background: #2563eb; color: white; }
    .btn-save:hover { background: #1d4ed8; }
    .btn-cancel { background: #f1f5f9; color: #475569; }
    .btn-cancel:hover { background: #e2e8f0; }
    .flash-error { background: #fef2f2; border: 1px solid #fca5a5; color: #b91c1c; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }
    @media (max-width: 600px) { .form-grid { grid-template-columns: 1fr; } .actions { flex-direction: column; } }
</style>

<div class="container">

    <div class="page-header">
        <div>
            <h1>🛡 Thêm phiếu bảo hành</h1>
            <p style="color:#64748b; font-size:14px; margin-top:4px;">Chọn nguồn bảo hành tương ứng: Khách mua tại quầy hoặc Đơn hàng Online.</p>
        </div>
        <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="back-link">← Quay lại danh sách</a>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="flash-error">⚠ ${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <div class="card">
        <!-- TAB BAR: 2 NGUỒN TẠO PHIẾU BẢO HÀNH -->
        <div class="tab-bar">
            <button type="button" class="tab-btn active" id="tabOfflineBtn" onclick="switchTab('offline')">
                🏪 Khách mua trực tiếp tại cửa hàng (Offline)
            </button>
            <button type="button" class="tab-btn" id="tabOnlineBtn" onclick="switchTab('online')">
                🌐 Khách mua qua Đơn hàng Online
            </button>
        </div>

        <!-- FORM 1: KHÁCH MUA TRỰC TIẾP TẠI CỬA HÀNG (OFFLINE) -->
        <form id="offlineForm" method="post" action="${pageContext.request.contextPath}/manage/sales/warranty-add">
            <input type="hidden" name="warrantyType" value="OFFLINE"/>

            <div style="background:#ecfdf5; border:1px solid #a7f3d0; border-radius:8px; padding:12px 16px; margin-bottom:20px; font-size:13px; color:#065f46;">
                💡 <b>Lưu ý:</b> Phiếu bảo hành cho khách mua trực tiếp sẽ chuyển thẳng sang trạng thái <b>Đang xử lý</b> để kỹ thuật viên bắt đầu kiểm tra ngay.
            </div>

            <div class="form-grid">
                <div class="form-group">
                    <label for="offCustomerName">Tên khách hàng <span class="req">*</span></label>
                    <input type="text" id="offCustomerName" name="customerName" required placeholder="Ví dụ: Nguyễn Văn A"/>
                </div>

                <div class="form-group">
                    <label for="offCustomerPhone">Số điện thoại <span class="req">*</span></label>
                    <input type="text" id="offCustomerPhone" name="customerPhone" required placeholder="Ví dụ: 0912345678"/>
                </div>

                <div class="form-group full">
                    <label for="offCustomerEmail">Email (nếu có)</label>
                    <input type="email" id="offCustomerEmail" name="customerEmail" placeholder="Ví dụ: khachhang@gmail.com"/>
                </div>

                <div class="form-group">
                    <label for="offProductSelect">Chọn sản phẩm bảo hành <span class="req">*</span></label>
                    <select id="offProductSelect" onchange="onProductSelectChange(this)">
                        <option value="">-- Chọn sản phẩm từ danh mục --</option>
                        <c:forEach items="${activeProducts}" var="p">
                            <option value="${p.name}" data-months="${p.warrantyMonths > 0 ? p.warrantyMonths : 24}">${p.name} (${p.brand} - BH: ${p.warrantyMonths > 0 ? p.warrantyMonths : 24} tháng)</option>
                        </c:forEach>
                    </select>
                    <div class="hint">Hoặc bạn có thể tự nhập tên sản phẩm chính xác ở ô bên dưới.</div>
                </div>

                <div class="form-group">
                    <label for="offProductName">Tên sản phẩm chính xác <span class="req">*</span></label>
                    <input type="text" id="offProductName" name="productName" required placeholder="Tên sản phẩm đồng hồ"/>
                </div>

                <div class="form-group">
                    <label for="offSerial">Số Serial trên đồng hồ</label>
                    <input type="text" id="offSerial" name="serial" placeholder="Ví dụ: RLX-2024-009876"/>
                </div>

                <div class="form-group">
                    <label for="offMonths">Thời hạn bảo hành (tháng) <span class="req">*</span></label>
                    <select id="offMonths" name="months" required>
                        <option value="6">6 tháng</option>
                        <option value="12">12 tháng</option>
                        <option value="18">18 tháng</option>
                        <option value="24" selected>24 tháng</option>
                        <option value="36">36 tháng (3 năm)</option>
                        <option value="60">60 tháng (5 năm)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="offBuyDate">Ngày mua hàng <span class="req">*</span></label>
                    <input type="date" id="offBuyDate" name="buyDate" required/>
                </div>

                <div class="form-group">
                    <label for="offImageUrl">Ảnh minh chứng / Tình trạng lỗi (Link hoặc path)</label>
                    <input type="text" id="offImageUrl" name="imageUrl" placeholder="Ví dụ: /assets/images/warranty/w1.jpg hoặc link ảnh..."/>
                </div>

                <div class="form-group full">
                    <label for="offNote">Lý do bảo hành / Mô tả lỗi ban đầu <span class="req">*</span></label>
                    <textarea id="offNote" name="note" required placeholder="Mô tả hiện trạng hư hỏng, lỗi máy, vào nước, trầy xước bên ngoài..."></textarea>
                </div>
            </div>

            <div class="actions">
                <button type="submit" class="btn btn-save">💾 Lập phiếu bảo hành (Chuyển Đang xử lý)</button>
                <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="btn btn-cancel">Hủy</a>
            </div>
        </form>

        <!-- FORM 2: TẠO PHIẾU TỪ ĐƠN HÀNG ONLINE -->
        <form id="onlineForm" method="post" action="${pageContext.request.contextPath}/manage/sales/warranty-add" style="display:none;">
            <input type="hidden" name="warrantyType" value="ONLINE"/>

            <div class="form-grid">
                <div class="form-group full" style="margin-bottom: 10px;">
                    <label for="searchQuery">🔍 Tìm kiếm đơn hàng Online <span class="req">*</span></label>
                    <div style="display: flex; gap: 8px;">
                        <input type="text" id="searchQuery" placeholder="Nhập tên khách hàng, số điện thoại hoặc mã đơn hàng..."/>
                        <button type="button" id="btnSearchOrder" class="btn btn-save" style="padding: 0 20px; white-space: nowrap; height: 42px;">Tìm kiếm</button>
                    </div>
                </div>

                <div class="form-group">
                    <label for="orderId">Đơn hàng liên kết <span class="req">*</span></label>
                    <select id="orderId" name="orderId" required disabled style="background-color: #f1f5f9;">
                        <option value="">-- Vui lòng tìm kiếm đơn hàng trước --</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="productName">Tên sản phẩm <span class="req">*</span></label>
                    <select id="productName" name="productName" required>
                        <option value="">-- Chọn đơn hàng trước --</option>
                    </select>
                </div>

                <div class="form-group full" id="customerInfoCard" style="display: none; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 15px; margin: 10px 0;">
                    <h3 style="margin-top: 0; font-size: 14px; color: #334155; border-bottom: 1px solid #e2e8f0; padding-bottom: 6px;">👤 Thông tin khách hàng liên kết</h3>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 13px; color: #475569;">
                        <div><strong>Khách hàng:</strong> <span id="infoCustomerName">-</span></div>
                        <div><strong>Số điện thoại:</strong> <span id="infoCustomerPhone">-</span></div>
                    </div>
                </div>

                <div class="form-group">
                    <label for="onlineSerial">Số Serial trên đồng hồ</label>
                    <input type="text" id="onlineSerial" name="serial" placeholder="Ví dụ: RLX-2024-001234"/>
                </div>

                <div class="form-group">
                    <label for="onlineImageUrl">Ảnh minh chứng / Tình trạng lỗi</label>
                    <input type="text" id="onlineImageUrl" name="imageUrl" placeholder="Link ảnh hoặc đường dẫn file..."/>
                </div>

                <div class="form-group full">
                    <label for="note">Lý do bảo hành / Mô tả lỗi ban đầu <span class="req">*</span></label>
                    <textarea id="note" name="note" required placeholder="Ghi chú về tình trạng lỗi khi tiếp nhận..."></textarea>
                </div>
            </div>

            <div class="actions">
                <button type="submit" class="btn btn-save">💾 Tạo phiếu bảo hành</button>
                <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="btn btn-cancel">Hủy</a>
            </div>
        </form>
    </div>

</div>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        var today = new Date().toISOString().split('T')[0];
        document.getElementById('offBuyDate').value = today;
    });

    function switchTab(mode) {
        if (mode === 'offline') {
            document.getElementById('offlineForm').style.display = 'block';
            document.getElementById('onlineForm').style.display = 'none';
            document.getElementById('tabOfflineBtn').classList.add('active');
            document.getElementById('tabOnlineBtn').classList.remove('active');
        } else {
            document.getElementById('offlineForm').style.display = 'none';
            document.getElementById('onlineForm').style.display = 'block';
            document.getElementById('tabOfflineBtn').classList.remove('active');
            document.getElementById('tabOnlineBtn').classList.add('active');
        }
    }

    function onProductSelectChange(select) {
        var opt = select.options[select.selectedIndex];
        if (opt && opt.value) {
            document.getElementById('offProductName').value = opt.value;
            var months = opt.getAttribute('data-months');
            if (months) {
                document.getElementById('offMonths').value = months;
            }
        }
    }

    // Search Order Logic for Online tab
    const searchInput = document.getElementById("searchQuery");
    const searchBtn = document.getElementById("btnSearchOrder");
    const orderSelect = document.getElementById("orderId");
    const productSelect = document.getElementById("productName");
    const customerCard = document.getElementById("customerInfoCard");
    const infoName = document.getElementById("infoCustomerName");
    const infoPhone = document.getElementById("infoCustomerPhone");

    searchBtn.addEventListener("click", function() {
        const query = searchInput.value.trim();
        if (!query) {
            alert("Vui lòng nhập tên khách hàng, số điện thoại hoặc mã đơn hàng để tìm kiếm.");
            return;
        }
        searchBtn.disabled = true;
        searchBtn.innerText = "Đang tìm...";

        fetch("${pageContext.request.contextPath}/api/orders/search?q=" + encodeURIComponent(query))
            .then(res => res.json())
            .then(data => {
                searchBtn.disabled = false;
                searchBtn.innerText = "Tìm kiếm";
                orderSelect.innerHTML = '<option value="">-- Chọn đơn hàng liên kết --</option>';
                productSelect.innerHTML = '<option value="">-- Chọn đơn hàng trước --</option>';
                customerCard.style.display = "none";

                if (!data || data.length === 0) {
                    orderSelect.innerHTML = '<option value="">❌ Không tìm thấy đơn hàng phù hợp</option>';
                    orderSelect.disabled = true;
                    return;
                }

                orderSelect.disabled = false;
                orderSelect.style.backgroundColor = "#fff";
                orderSelect.style.cursor = "pointer";

                data.forEach(order => {
                    const opt = document.createElement("option");
                    opt.value = order.id;
                    opt.text = "#" + order.code + " - " + (order.customerName || "Khách hàng") + " (" + (order.phone || "") + ") - " + (order.status || "");
                    opt.setAttribute("data-customer", order.customerName || "");
                    opt.setAttribute("data-phone", order.phone || "");
                    opt.setAttribute("data-code", order.code || "");
                    orderSelect.appendChild(opt);
                });
            })
            .catch(err => {
                searchBtn.disabled = false;
                searchBtn.innerText = "Tìm kiếm";
                alert("Lỗi khi tìm kiếm đơn hàng: " + err.message);
            });
    });

    orderSelect.addEventListener("change", function() {
        const orderId = this.value;
        const selectedOpt = this.options[this.selectedIndex];
        if (!orderId) {
            productSelect.innerHTML = '<option value="">-- Chọn đơn hàng trước --</option>';
            customerCard.style.display = "none";
            return;
        }

        infoName.innerText = selectedOpt.getAttribute("data-customer") || "Chưa rõ";
        infoPhone.innerText = selectedOpt.getAttribute("data-phone") || "Chưa rõ";
        customerCard.style.display = "block";

        fetch("${pageContext.request.contextPath}/api/orders/" + orderId + "/items")
            .then(res => res.json())
            .then(items => {
                productSelect.innerHTML = '<option value="">-- Chọn sản phẩm cần bảo hành --</option>';
                if (!items || items.length === 0) {
                    productSelect.innerHTML = '<option value="">Không có sản phẩm nào</option>';
                    return;
                }
                items.forEach(item => {
                    const opt = document.createElement("option");
                    opt.value = item.productName;
                    opt.text = item.productName + (item.variantName ? " (" + item.variantName + ")" : "");
                    productSelect.appendChild(opt);
                });
            })
            .catch(err => {
                alert("Lỗi tải sản phẩm của đơn hàng: " + err.message);
            });
    });
</script>
