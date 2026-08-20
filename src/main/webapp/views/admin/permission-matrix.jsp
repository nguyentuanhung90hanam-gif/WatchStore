<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<style>
    .perm-container {
        max-width: 1000px;
        margin: 0 auto;
    }
    .perm-header-card {
        background: #fff;
        border-radius: 12px;
        padding: 22px 28px;
        box-shadow: 0 1px 4px rgba(0,0,0,0.06);
        margin-bottom: 24px;
        border: 1px solid #e2e8f0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 20px;
    }
    .perm-user-info {
        display: flex;
        align-items: center;
        gap: 16px;
    }
    .perm-avatar {
        width: 52px;
        height: 52px;
        border-radius: 50%;
        background: linear-gradient(135deg, #d4af37, #b8860b);
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        font-weight: 700;
        box-shadow: 0 4px 10px rgba(184,134,11,0.25);
    }
    .perm-user-title {
        font-size: 18px;
        font-weight: 700;
        color: #1e293b;
        margin: 0 0 4px;
    }
    .perm-user-meta {
        font-size: 14px;
        color: #64748b;
        display: flex;
        gap: 12px;
        align-items: center;
    }
    .badge-role-emp {
        display: inline-block;
        padding: 3px 10px;
        background: #eff6ff;
        color: #1d4ed8;
        font-weight: 700;
        font-size: 12px;
        border-radius: 20px;
        border: 1px solid #bfdbfe;
    }
    .perm-card {
        background: #fff;
        border-radius: 12px;
        border: 1px solid #e2e8f0;
        box-shadow: 0 1px 4px rgba(0,0,0,0.05);
        margin-bottom: 24px;
        overflow: hidden;
    }
    .perm-card-header {
        padding: 16px 24px;
        background: #f8fafc;
        border-bottom: 1px solid #e2e8f0;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .perm-card-title {
        font-size: 15px;
        font-weight: 700;
        color: #1e293b;
        letter-spacing: 0.5px;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .system-card .perm-card-header {
        background: #fef2f2;
        border-bottom-color: #fecaca;
    }
    .system-card .perm-card-title {
        color: #991b1b;
    }
    .perm-badge-locked {
        background: #fee2e2;
        color: #991b1b;
        font-size: 11px;
        font-weight: 700;
        padding: 4px 10px;
        border-radius: 6px;
        border: 1px solid #fca5a5;
        letter-spacing: 0.5px;
    }
    .locked-row {
        opacity: 0.85;
        background: #fafafa;
        border-radius: 8px;
        padding: 12px 16px;
        margin-bottom: 8px;
        border: 1px dashed #e2e8f0;
    }
    .locked-row:last-child {
        margin-bottom: 0;
    }

    /* Functional Group Block */
    .group-section {
        padding: 18px 24px;
        border-bottom: 1px solid #f1f5f9;
    }
    .group-section:last-child {
        border-bottom: none;
    }
    .group-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;
    }
    .group-name {
        font-size: 15px;
        font-weight: 700;
        color: #0f172a;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .group-tag {
        font-size: 11px;
        font-weight: 600;
        padding: 2px 8px;
        background: #e2e8f0;
        color: #475569;
        border-radius: 4px;
    }
    .perm-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 10px 18px;
    }
    .perm-item {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        padding: 8px 12px;
        border-radius: 8px;
        background: #f8fafc;
        border: 1px solid #f1f5f9;
        transition: all 0.15s ease;
    }
    .perm-item:hover {
        background: #f1f5f9;
        border-color: #cbd5e1;
    }
    .perm-item input[type="checkbox"] {
        width: 18px;
        height: 18px;
        margin-top: 2px;
        cursor: pointer;
        accent-color: #d4af37;
    }
    .perm-item label {
        cursor: pointer;
        flex: 1;
    }
    .perm-label-title {
        font-size: 13.5px;
        font-weight: 600;
        color: #1e293b;
        display: block;
        margin-bottom: 2px;
    }
    .perm-label-desc {
        font-size: 12px;
        color: #64748b;
        display: block;
        line-height: 1.35;
    }
</style>

<div class="perm-container">

    <%-- Thông báo Flash Message --%>
    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#ecfdf5;color:#065f46;border:1px solid #a7f3d0;border-radius:8px;padding:12px 18px;margin-bottom:24px;font-weight:600;display:flex;align-items:center;gap:10px;">
            <span>✅</span> ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <c:choose>
        <%-- TRƯỜNG HỢP 1: CHƯA CÓ NHÂN VIÊN TRONG HỆ THỐNG --%>
        <c:when test="${empty employees}">
            <div class="perm-header-card" style="flex-direction:column;align-items:center;text-align:center;padding:40px 24px;">
                <div style="font-size:48px;margin-bottom:12px;"></div>
                <h2 style="margin:0 0 8px 0;color:#1e293b;">Chưa có tài khoản Nhân viên (EMPLOYEE)</h2>
                <p style="color:#64748b;font-size:14px;max-width:500px;margin:0 0 20px 0;">
                    Hệ thống chưa có tài khoản nhân viên nào để phân quyền. Vui lòng tạo tài khoản nhân viên tại Quản lý tài khoản.
                </p>
                <a href="${pageContext.request.contextPath}/manage/admin/accounts/add" class="button button-gold" style="padding:10px 24px;border-radius:8px;text-decoration:none;font-weight:700;">
                    + Tạo tài khoản nhân viên
                </a>
            </div>
        </c:when>

        <%-- TRƯỜNG HỢP 2: CÓ DANH SÁCH NHÂN VIÊN --%>
        <c:otherwise>

            <%-- HEADER: THÔNG TIN & BỘ CHỌN NHÂN VIÊN --%>
            <div class="perm-header-card">
                <div class="perm-user-info">
                    <div class="perm-avatar">
                        ${empty selectedEmployee.fullName ? 'NV' : fn:substring(selectedEmployee.fullName, 0, 1)}
                    </div>
                    <div>
                        <h1 class="perm-user-title">${selectedEmployee.fullName}</h1>
                        <div class="perm-user-meta">
                            <span>✉ ${selectedEmployee.email}</span>
                            <c:if test="${not empty selectedEmployee.phone}">
                                <span> ${selectedEmployee.phone}</span>
                            </c:if>
                            <span class="badge-role-emp">Vai trò: EMPLOYEE</span>
                        </div>
                    </div>
                </div>

                <div>
                    <form method="get" action="${pageContext.request.contextPath}/manage/admin/permissions" style="display:flex;align-items:center;gap:10px;">
                        <label style="font-size:13.5px;font-weight:600;color:#475569;">Chọn nhân viên:</label>
                        <select name="userId" onchange="this.form.submit()"
                                style="padding:8px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:13.5px;background:#fff;font-weight:600;color:#1e293b;cursor:pointer;">
                            <c:forEach items="${employees}" var="emp">
                                <option value="${emp.userId}" ${emp.userId == selectedUserId ? 'selected' : ''}>
                                    ${emp.fullName} (${emp.email})
                                </option>
                            </c:forEach>
                        </select>
                    </form>
                </div>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/manage/admin/permissions">
                <input type="hidden" name="userId" value="${selectedUserId}">

                <%-- KHỐI 1: HỆ THỐNG — CHỈ ADMIN (KHÓA VĨNH VIỄN) --%>
                <div class="perm-card system-card">
                    <div class="perm-card-header">
                        <div class="perm-card-title">
                             HỆ THỐNG — CHỈ QUẢN TRỊ VIÊN (ADMIN)
                        </div>
                        <span class="perm-badge-locked">KHÓA VĨNH VIỄN</span>
                    </div>
                    <div style="padding: 16px 24px;">
                        <div class="locked-row">
                            <div style="display:flex;align-items:center;gap:10px;margin-bottom:2px;">
                                <strong style="color:#991b1b;font-size:14px;"> Tài khoản (ACCOUNT)</strong>
                                <span style="font-size:11.5px;color:#dc2626;background:#fef2f2;padding:1px 6px;border-radius:4px;font-weight:600;">Chỉ Admin</span>
                            </div>
                            <p style="margin:0;font-size:13px;color:#64748b;">Quản trị tài khoản người dùng, phân loại vai trò và trạng thái truy cập.</p>
                        </div>

                        <div class="locked-row">
                            <div style="display:flex;align-items:center;gap:10px;margin-bottom:2px;">
                                <strong style="color:#991b1b;font-size:14px;"> Vai trò (ROLE)</strong>
                                <span style="font-size:11.5px;color:#dc2626;background:#fef2f2;padding:1px 6px;border-radius:4px;font-weight:600;">Chỉ Admin</span>
                            </div>
                            <p style="margin:0;font-size:13px;color:#64748b;">Quản trị vai trò hệ thống và mô hình phân quyền cơ bản.</p>
                        </div>

                        <div class="locked-row">
                            <div style="display:flex;align-items:center;gap:10px;margin-bottom:2px;">
                                <strong style="color:#991b1b;font-size:14px;"> Phân quyền (PERMISSION)</strong>
                                <span style="font-size:11.5px;color:#dc2626;background:#fef2f2;padding:1px 6px;border-radius:4px;font-weight:600;">Chỉ Admin</span>
                            </div>
                            <p style="margin:0;font-size:13px;color:#64748b;">Cấu hình và phân quyền thao tác cho từng tài khoản nhân viên.</p>
                        </div>
                    </div>
                </div>

                <%-- KHỐI 2: PHÂN QUYỀN CHỨC NĂNG CHO NHÂN VIÊN --%>
                <div class="perm-card">
                    <div class="perm-card-header">
                        <div class="perm-card-title">
                            <span>⚡</span> PHÂN QUYỀN CHỨC NĂNG CHO NHÂN VIÊN
                        </div>
                        <span style="font-size:12.5px;color:#64748b;font-weight:500;">Chọn các quyền hạn cụ thể cấp cho tài khoản này</span>
                    </div>

                    <div>

                        <%-- 1. SẢN PHẨM & DANH MỤC --%>
                        <div class="group-section">
                            <div class="group-header">
                                <div class="group-name">
                                     SẢN PHẨM, DANH MỤC &amp; THƯƠNG HIỆU
                                </div>
                                <span class="group-tag">PRODUCT</span>
                            </div>
                            <div class="perm-grid">
                                <c:if test="${not empty permByCode['PRODUCT_VIEW']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['PRODUCT_VIEW'].permissionId}" name="permissionIds" value="${permByCode['PRODUCT_VIEW'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['PRODUCT_VIEW'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['PRODUCT_VIEW'].permissionId}">
                                            <span class="perm-label-title">${permByCode['PRODUCT_VIEW'].permissionName}</span>
                                            <span class="perm-label-desc">Xem danh sách, chi tiết sản phẩm, danh mục và thương hiệu</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['PRODUCT_CREATE']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['PRODUCT_CREATE'].permissionId}" name="permissionIds" value="${permByCode['PRODUCT_CREATE'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['PRODUCT_CREATE'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['PRODUCT_CREATE'].permissionId}">
                                            <span class="perm-label-title">${permByCode['PRODUCT_CREATE'].permissionName}</span>
                                            <span class="perm-label-desc">Thêm mới sản phẩm, biến thể và hình ảnh</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['PRODUCT_EDIT']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['PRODUCT_EDIT'].permissionId}" name="permissionIds" value="${permByCode['PRODUCT_EDIT'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['PRODUCT_EDIT'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['PRODUCT_EDIT'].permissionId}">
                                            <span class="perm-label-title">${permByCode['PRODUCT_EDIT'].permissionName}</span>
                                            <span class="perm-label-desc">Chỉnh sửa thông tin sản phẩm, giá bán, biến thể</span>
                                        </label>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <%-- 2. VOUCHER & KHUYẾN MÃI --%>
                        <div class="group-section">
                            <div class="group-header">
                                <div class="group-name">
                                     VOUCHER &amp; KHUYẾN MÃI
                                </div>
                                <span class="group-tag">VOUCHER</span>
                            </div>
                            <div class="perm-grid">
                                <c:if test="${not empty permByCode['VOUCHER_VIEW']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['VOUCHER_VIEW'].permissionId}" name="permissionIds" value="${permByCode['VOUCHER_VIEW'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['VOUCHER_VIEW'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['VOUCHER_VIEW'].permissionId}">
                                            <span class="perm-label-title">${permByCode['VOUCHER_VIEW'].permissionName}</span>
                                            <span class="perm-label-desc">Xem danh sách mã voucher và mã khuyến mãi</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['VOUCHER_CREATE']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['VOUCHER_CREATE'].permissionId}" name="permissionIds" value="${permByCode['VOUCHER_CREATE'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['VOUCHER_CREATE'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['VOUCHER_CREATE'].permissionId}">
                                            <span class="perm-label-title">${permByCode['VOUCHER_CREATE'].permissionName}</span>
                                            <span class="perm-label-desc">Tạo mã giảm giá và cài đặt điều kiện áp dụng</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['VOUCHER_EDIT']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['VOUCHER_EDIT'].permissionId}" name="permissionIds" value="${permByCode['VOUCHER_EDIT'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['VOUCHER_EDIT'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['VOUCHER_EDIT'].permissionId}">
                                            <span class="perm-label-title">${permByCode['VOUCHER_EDIT'].permissionName}</span>
                                            <span class="perm-label-desc">Chỉnh sửa hạn dùng, giá trị giảm và kích hoạt voucher</span>
                                        </label>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <%-- 3. ĐƠN HÀNG --%>
                        <div class="group-section">
                            <div class="group-header">
                                <div class="group-name">
                                     QUẢN LÝ ĐƠN HÀNG
                                </div>
                                <span class="group-tag">ORDER</span>
                            </div>
                            <div class="perm-grid">
                                <c:if test="${not empty permByCode['ORDER_VIEW']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['ORDER_VIEW'].permissionId}" name="permissionIds" value="${permByCode['ORDER_VIEW'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['ORDER_VIEW'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['ORDER_VIEW'].permissionId}">
                                            <span class="perm-label-title">${permByCode['ORDER_VIEW'].permissionName}</span>
                                            <span class="perm-label-desc">Xem danh sách và chi tiết các đơn đặt hàng</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['ORDER_CREATE']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['ORDER_CREATE'].permissionId}" name="permissionIds" value="${permByCode['ORDER_CREATE'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['ORDER_CREATE'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['ORDER_CREATE'].permissionId}">
                                            <span class="perm-label-title">${permByCode['ORDER_CREATE'].permissionName}</span>
                                            <span class="perm-label-desc">Tạo đơn hàng mới tại quầy POS / bán lẻ</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['ORDER_EDIT']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['ORDER_EDIT'].permissionId}" name="permissionIds" value="${permByCode['ORDER_EDIT'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['ORDER_EDIT'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['ORDER_EDIT'].permissionId}">
                                            <span class="perm-label-title">${permByCode['ORDER_EDIT'].permissionName}</span>
                                            <span class="perm-label-desc">Chỉnh sửa thông tin đơn hàng và người nhận</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['ORDER_APPROVE']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['ORDER_APPROVE'].permissionId}" name="permissionIds" value="${permByCode['ORDER_APPROVE'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['ORDER_APPROVE'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['ORDER_APPROVE'].permissionId}">
                                            <span class="perm-label-title">${permByCode['ORDER_APPROVE'].permissionName}</span>
                                            <span class="perm-label-desc">Xác nhận và duyệt trạng thái xử lý đơn hàng</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['ORDER_EXPORT']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['ORDER_EXPORT'].permissionId}" name="permissionIds" value="${permByCode['ORDER_EXPORT'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['ORDER_EXPORT'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['ORDER_EXPORT'].permissionId}">
                                            <span class="perm-label-title">${permByCode['ORDER_EXPORT'].permissionName}</span>
                                            <span class="perm-label-desc">In hóa đơn bán lẻ và xuất danh sách đơn hàng</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['SALES_ORDER']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['SALES_ORDER'].permissionId}" name="permissionIds" value="${permByCode['SALES_ORDER'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['SALES_ORDER'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['SALES_ORDER'].permissionId}">
                                            <span class="perm-label-title">${permByCode['SALES_ORDER'].permissionName}</span>
                                            <span class="perm-label-desc">Truy cập phân hệ quản lý đơn hàng bán hàng</span>
                                        </label>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <%-- 4. KHÁCH HÀNG & TƯƠNG TÁC --%>
                        <div class="group-section">
                            <div class="group-header">
                                <div class="group-name">
                                     QUẢN LÝ KHÁCH HÀNG &amp; TƯƠNG TÁC
                                </div>
                                <span class="group-tag">CUSTOMER</span>
                            </div>
                            <div class="perm-grid">
                                <c:if test="${not empty permByCode['CUSTOMER_VIEW']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['CUSTOMER_VIEW'].permissionId}" name="permissionIds" value="${permByCode['CUSTOMER_VIEW'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['CUSTOMER_VIEW'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['CUSTOMER_VIEW'].permissionId}">
                                            <span class="perm-label-title">${permByCode['CUSTOMER_VIEW'].permissionName}</span>
                                            <span class="perm-label-desc">Xem thông tin khách hàng, đánh giá và bình luận</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['CUSTOMER_CREATE']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['CUSTOMER_CREATE'].permissionId}" name="permissionIds" value="${permByCode['CUSTOMER_CREATE'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['CUSTOMER_CREATE'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['CUSTOMER_CREATE'].permissionId}">
                                            <span class="perm-label-title">${permByCode['CUSTOMER_CREATE'].permissionName}</span>
                                            <span class="perm-label-desc">Thêm mới thông tin khách hàng</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['CUSTOMER_EDIT']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['CUSTOMER_EDIT'].permissionId}" name="permissionIds" value="${permByCode['CUSTOMER_EDIT'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['CUSTOMER_EDIT'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['CUSTOMER_EDIT'].permissionId}">
                                            <span class="perm-label-title">${permByCode['CUSTOMER_EDIT'].permissionName}</span>
                                            <span class="perm-label-desc">Cập nhật thông tin và kiểm duyệt review/comment</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['SALES_CUSTOMER']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['SALES_CUSTOMER'].permissionId}" name="permissionIds" value="${permByCode['SALES_CUSTOMER'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['SALES_CUSTOMER'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['SALES_CUSTOMER'].permissionId}">
                                            <span class="perm-label-title">${permByCode['SALES_CUSTOMER'].permissionName}</span>
                                            <span class="perm-label-desc">Truy cập phân hệ chăm sóc khách hàng</span>
                                        </label>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <%-- 5. BẢO HÀNH --%>
                        <div class="group-section">
                            <div class="group-header">
                                <div class="group-name">
                                     DỊCH VỤ BẢO HÀNH
                                </div>
                                <span class="group-tag">WARRANTY</span>
                            </div>
                            <div class="perm-grid">
                                <c:if test="${not empty permByCode['SALES_WARRANTY']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['SALES_WARRANTY'].permissionId}" name="permissionIds" value="${permByCode['SALES_WARRANTY'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['SALES_WARRANTY'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['SALES_WARRANTY'].permissionId}">
                                            <span class="perm-label-title">${permByCode['SALES_WARRANTY'].permissionName}</span>
                                            <span class="perm-label-desc">Tiếp nhận yêu cầu bảo hành, tra cứu serial và xử lý sửa chữa</span>
                                        </label>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <%-- 6. BÁO CÁO & THỐNG KÊ --%>
                        <div class="group-section">
                            <div class="group-header">
                                <div class="group-name">
                                     BÁO CÁO &amp; THỐNG KÊ
                                </div>
                                <span class="group-tag">REPORT</span>
                            </div>
                            <div class="perm-grid">
                                <c:if test="${not empty permByCode['REPORT_VIEW']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['REPORT_VIEW'].permissionId}" name="permissionIds" value="${permByCode['REPORT_VIEW'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['REPORT_VIEW'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['REPORT_VIEW'].permissionId}">
                                            <span class="perm-label-title">${permByCode['REPORT_VIEW'].permissionName}</span>
                                            <span class="perm-label-desc">Xem báo cáo thống kê doanh thu và phân tích bán hàng</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['REPORT_EXPORT']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['REPORT_EXPORT'].permissionId}" name="permissionIds" value="${permByCode['REPORT_EXPORT'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['REPORT_EXPORT'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['REPORT_EXPORT'].permissionId}">
                                            <span class="perm-label-title">${permByCode['REPORT_EXPORT'].permissionName}</span>
                                            <span class="perm-label-desc">Xuất dữ liệu báo cáo ra định dạng file PDF/Excel</span>
                                        </label>
                                    </div>
                                </c:if>

                                <c:if test="${not empty permByCode['SALES_REPORT']}">
                                    <div class="perm-item">
                                        <input type="checkbox" id="p_${permByCode['SALES_REPORT'].permissionId}" name="permissionIds" value="${permByCode['SALES_REPORT'].permissionId}"
                                               ${activePermissionIds.contains(permByCode['SALES_REPORT'].permissionId) ? 'checked' : ''}>
                                        <label for="p_${permByCode['SALES_REPORT'].permissionId}">
                                            <span class="perm-label-title">${permByCode['SALES_REPORT'].permissionName}</span>
                                            <span class="perm-label-desc">Xem báo cáo doanh số chi tiết theo bán hàng</span>
                                        </label>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                    </div>
                </div>

                <%-- NÚT THAO TÁC --%>
                <div style="display:flex;gap:12px;justify-content:flex-end;margin-bottom:40px;">
                    <a href="${pageContext.request.contextPath}/manage/admin/permissions"
                       class="button button-outline"
                       style="padding:10px 24px;border-radius:8px;text-decoration:none;">
                        Hủy bỏ
                    </a>
                    <button type="submit"
                            class="button button-gold"
                            style="padding:10px 32px;border-radius:8px;border:none;cursor:pointer;font-weight:700;font-size:14px;">
                         Lưu phân quyền
                    </button>
                </div>

            </form>

        </c:otherwise>
    </c:choose>

</div>
