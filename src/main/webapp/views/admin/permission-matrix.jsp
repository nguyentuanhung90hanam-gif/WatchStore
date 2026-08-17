<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<style>
    .perm-container {
        max-width: 960px;
        margin: 0 auto;
    }
    .perm-header-card {
        background: #fff;
        border-radius: 12px;
        padding: 24px 28px;
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
        font-weight: 600;
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
    .perm-list {
        padding: 12px 24px;
    }
    .perm-row {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        padding: 16px 0;
        border-bottom: 1px solid #f1f5f9;
        transition: background 0.15s;
    }
    .perm-row:last-child {
        border-bottom: none;
    }
    .perm-checkbox-wrap {
        padding-top: 2px;
    }
    .perm-checkbox-wrap input[type="checkbox"] {
        width: 20px;
        height: 20px;
        cursor: pointer;
        accent-color: #d4af37;
    }
    .perm-content {
        flex: 1;
    }
    .perm-title-row {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 4px;
    }
    .perm-group-name {
        font-size: 16px;
        font-weight: 700;
        color: #1e293b;
        cursor: pointer;
    }
    .perm-desc {
        font-size: 13.5px;
        color: #64748b;
        margin: 0 0 6px 0;
        line-height: 1.4;
    }
    .perm-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
    }
    .perm-tag {
        font-size: 11.5px;
        padding: 2px 8px;
        background: #f1f5f9;
        color: #475569;
        border-radius: 4px;
        font-weight: 500;
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
</style>

<div class="perm-container">

    <%-- Thông tin nhân viên được chọn --%>
    <div class="perm-header-card">
        <div class="perm-user-info">
            <div class="perm-avatar">
                ${empty selectedEmployee.fullName ? 'S' : fn:substring(selectedEmployee.fullName, 0, 1)}
            </div>
            <div>
                <h1 class="perm-user-title">${selectedEmployee.fullName}</h1>
                <div class="perm-user-meta">
                    <span>✉ ${selectedEmployee.email}</span>
                    <span class="badge-role-emp">Role: ${selectedEmployee.role.label}</span>
                </div>
            </div>
        </div>

        <c:if test="${fn:length(employees) > 1}">
            <div>
                <form method="get" action="${pageContext.request.contextPath}/manage/admin/permissions" style="display:flex;align-items:center;gap:8px;">
                    <label style="font-size:13px;font-weight:600;color:#64748b;">Chọn nhân viên:</label>
                    <select name="userId" onchange="this.form.submit()"
                            style="padding:6px 12px;border:1px solid #cbd5e1;border-radius:6px;font-size:13px;background:#fff;">
                        <c:forEach items="${employees}" var="emp">
                            <option value="${emp.userId}" ${emp.userId == selectedUserId ? 'selected' : ''}>
                                ${emp.fullName} (${emp.email})
                            </option>
                        </c:forEach>
                    </select>
                </form>
            </div>
        </c:if>
    </div>

    <%-- Alert Thông báo --%>
    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#ecfdf5;color:#065f46;border:1px solid #a7f3d0;border-radius:8px;padding:12px 18px;margin-bottom:24px;font-weight:600;display:flex;align-items:center;gap:10px;">
            <span>✅</span> ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/manage/admin/permissions">
        <input type="hidden" name="userId" value="${selectedUserId}">

        <%-- KHỐI 1: HỆ THỐNG — CHỈ ADMIN (KHÓA VĨNH VIỄN) --%>
        <div class="perm-card system-card">
            <div class="perm-card-header">
                <div class="perm-card-title">
                    <span>🔒</span> HỆ THỐNG — CHỈ ADMIN
                </div>
                <span class="perm-badge-locked">KHÓA VĨNH VIỄN</span>
            </div>
            <div class="perm-list" style="padding: 16px 24px;">
                <div class="locked-row">
                    <div style="display:flex;align-items:center;gap:10px;margin-bottom:2px;">
                        <strong style="color:#991b1b;font-size:14px;">🔒 Tài khoản</strong>
                        <span style="font-size:12px;color:#dc2626;background:#fef2f2;padding:1px 6px;border-radius:4px;">Chỉ Admin</span>
                    </div>
                    <p style="margin:0;font-size:13px;color:#64748b;">Quản trị tài khoản người dùng và thông tin hệ thống</p>
                </div>

                <div class="locked-row">
                    <div style="display:flex;align-items:center;gap:10px;margin-bottom:2px;">
                        <strong style="color:#991b1b;font-size:14px;">🔒 Vai trò</strong>
                        <span style="font-size:12px;color:#dc2626;background:#fef2f2;padding:1px 6px;border-radius:4px;">Chỉ Admin</span>
                    </div>
                    <p style="margin:0;font-size:13px;color:#64748b;">Quản lý các nhóm vai trò và phân quyền cơ bản</p>
                </div>

                <div class="locked-row">
                    <div style="display:flex;align-items:center;gap:10px;margin-bottom:2px;">
                        <strong style="color:#991b1b;font-size:14px;">🔒 Phân quyền</strong>
                        <span style="font-size:12px;color:#dc2626;background:#fef2f2;padding:1px 6px;border-radius:4px;">Chỉ Admin</span>
                    </div>
                    <p style="margin:0;font-size:13px;color:#64748b;">Cấu hình và phân công chức năng cho nhân viên</p>
                </div>
            </div>
        </div>

        <%-- KHỐI 2: PHÂN CÔNG CHỨC NĂNG CHO NHÂN VIÊN --%>
        <div class="perm-card">
            <div class="perm-card-header">
                <div class="perm-card-title">
                    <span>⚡</span> PHÂN CÔNG CHỨC NĂNG
                </div>
                <span style="font-size:12px;color:#64748b;font-weight:500;">Chọn nhóm chức năng cho phép nhân viên thao tác</span>
            </div>

            <div class="perm-list">

                <%-- 1. SẢN PHẨM --%>
                <div class="perm-row">
                    <div class="perm-checkbox-wrap">
                        <input type="checkbox" id="grp_product" name="functionalGroups" value="PRODUCT"
                               ${activeGroups.contains('PRODUCT') ? 'checked' : ''}>
                    </div>
                    <div class="perm-content">
                        <div class="perm-title-row">
                            <label for="grp_product" class="perm-group-name">Sản phẩm</label>
                        </div>
                        <p class="perm-desc">Quản lý sản phẩm, danh mục và thương hiệu</p>
                        <div class="perm-tags">
                            <span class="perm-tag">Sản phẩm</span>
                            <span class="perm-tag">Danh mục</span>
                            <span class="perm-tag">Thương hiệu</span>
                        </div>
                    </div>
                </div>

                <%-- 2. BÁN HÀNG --%>
                <div class="perm-row">
                    <div class="perm-checkbox-wrap">
                        <input type="checkbox" id="grp_sales" name="functionalGroups" value="SALES"
                               ${activeGroups.contains('SALES') ? 'checked' : ''}>
                    </div>
                    <div class="perm-content">
                        <div class="perm-title-row">
                            <label for="grp_sales" class="perm-group-name">Bán hàng</label>
                        </div>
                        <p class="perm-desc">Đơn hàng và khách hàng</p>
                        <div class="perm-tags">
                            <span class="perm-tag">Đơn hàng</span>
                            <span class="perm-tag">Khách hàng</span>
                        </div>
                    </div>
                </div>

                <%-- 3. REVIEW & COMMENT --%>
                <div class="perm-row">
                    <div class="perm-checkbox-wrap">
                        <input type="checkbox" id="grp_review" name="functionalGroups" value="REVIEW_COMMENT"
                               ${activeGroups.contains('REVIEW_COMMENT') ? 'checked' : ''}>
                    </div>
                    <div class="perm-content">
                        <div class="perm-title-row">
                            <label for="grp_review" class="perm-group-name">Review &amp; Comment</label>
                        </div>
                        <p class="perm-desc">Quản lý đánh giá và bình luận của khách hàng</p>
                        <div class="perm-tags">
                            <span class="perm-tag">Review</span>
                            <span class="perm-tag">Comment</span>
                        </div>
                    </div>
                </div>

                <%-- 4. BẢO HÀNH --%>
                <div class="perm-row">
                    <div class="perm-checkbox-wrap">
                        <input type="checkbox" id="grp_warranty" name="functionalGroups" value="WARRANTY"
                               ${activeGroups.contains('WARRANTY') ? 'checked' : ''}>
                    </div>
                    <div class="perm-content">
                        <div class="perm-title-row">
                            <label for="grp_warranty" class="perm-group-name">Bảo hành</label>
                        </div>
                        <p class="perm-desc">Tiếp nhận và xử lý yêu cầu bảo hành</p>
                        <div class="perm-tags">
                            <span class="perm-tag">Quản lý bảo hành</span>
                        </div>
                    </div>
                </div>

                <%-- 5. VOUCHER --%>
                <div class="perm-row">
                    <div class="perm-checkbox-wrap">
                        <input type="checkbox" id="grp_voucher" name="functionalGroups" value="VOUCHER"
                               ${activeGroups.contains('VOUCHER') ? 'checked' : ''}>
                    </div>
                    <div class="perm-content">
                        <div class="perm-title-row">
                            <label for="grp_voucher" class="perm-group-name">Voucher</label>
                        </div>
                        <p class="perm-desc">Quản lý mã giảm giá</p>
                        <div class="perm-tags">
                            <span class="perm-tag">Voucher</span>
                        </div>
                    </div>
                </div>

                <%-- 6. BANNER & BÀI VIẾT --%>
                <div class="perm-row">
                    <div class="perm-checkbox-wrap">
                        <input type="checkbox" id="grp_banner_post" name="functionalGroups" value="BANNER_POST"
                               ${activeGroups.contains('BANNER_POST') ? 'checked' : ''}>
                    </div>
                    <div class="perm-content">
                        <div class="perm-title-row">
                            <label for="grp_banner_post" class="perm-group-name">Banner &amp; Bài viết</label>
                        </div>
                        <p class="perm-desc">Quản lý banner quảng cáo và nội dung tin tức website</p>
                        <div class="perm-tags">
                            <span class="perm-tag">Banner</span>
                            <span class="perm-tag">Bài viết</span>
                        </div>
                    </div>
                </div>

                <%-- 7. BÁO CÁO --%>
                <div class="perm-row">
                    <div class="perm-checkbox-wrap">
                        <input type="checkbox" id="grp_report" name="functionalGroups" value="REPORT"
                               ${activeGroups.contains('REPORT') ? 'checked' : ''}>
                    </div>
                    <div class="perm-content">
                        <div class="perm-title-row">
                            <label for="grp_report" class="perm-group-name">Báo cáo</label>
                        </div>
                        <p class="perm-desc">Xem thống kê và báo cáo bán hàng</p>
                        <div class="perm-tags">
                            <span class="perm-tag">Thống kê &amp; Báo cáo</span>
                        </div>
                    </div>
                </div>

            </div>
        </div>

        <%-- Nút Lưu / Hủy --%>
        <div style="display:flex;gap:12px;justify-content:flex-end;margin-bottom:40px;">
            <a href="${pageContext.request.contextPath}/manage/admin/permissions"
               class="button button-outline"
               style="padding:10px 24px;border-radius:8px;text-decoration:none;">
                Hủy bỏ
            </a>
            <button type="submit"
                    class="button button-gold"
                    style="padding:10px 32px;border-radius:8px;border:none;cursor:pointer;font-weight:700;font-size:14px;">
                💾 Lưu phân quyền
            </button>
        </div>

    </form>

</div>
