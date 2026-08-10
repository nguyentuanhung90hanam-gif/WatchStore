<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card">

    <h2>
        ${empty role.roleId || role.roleId == 0 ? "Thêm vai trò" : "Sửa vai trò"}
    </h2>

    <%-- Hiển thị lỗi validation --%>
    <c:if test="${not empty errorMessage}">
        <div style="background:#fff3cd;color:#856404;border:1px solid #ffc107;border-radius:8px;padding:12px 16px;margin-bottom:16px;font-size:0.95em;">
            ⚠ ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/roles/${empty role.roleId || role.roleId == 0 ? 'save' : 'update'}">

        <%-- Hidden ID khi sửa --%>
        <c:if test="${not empty role.roleId && role.roleId > 0}">
            <input type="hidden" name="roleId" value="${role.roleId}">
        </c:if>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">

            <div>
                <label>Mã vai trò <span style="color:red;">*</span></label>
                <input type="text" name="roleCode"
                       value="${role.roleCode}"
                       placeholder="VD: ADMIN, SALES..."
                       required
                       style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>

            <div>
                <label>Tên vai trò <span style="color:red;">*</span></label>
                <input type="text" name="roleName"
                       value="${role.roleName}"
                       placeholder="VD: Quản trị viên"
                       required
                       style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>

        </div>

        <div style="margin-bottom:16px;">
            <label>Mô tả</label>
            <textarea name="description"
                      placeholder="Mô tả chức năng của vai trò..."
                      rows="3"
                      style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;resize:vertical;">${role.description}</textarea>
        </div>

        <div style="margin-bottom:16px;">
            <label style="display:inline-flex;align-items:center;gap:6px;cursor:pointer;">
                <input type="checkbox" name="isSystem" value="true"
                       ${role.isSystem ? 'checked' : ''}>
                <span>Vai trò hệ thống</span>
                <small style="color:#888;">(không thể xóa nếu bật)</small>
            </label>
        </div>

        <div style="display:flex;gap:12px;">
            <button type="submit" class="button button-gold">Lưu</button>
            <a href="${pageContext.request.contextPath}/manage/admin/roles"
               class="button" style="background:#eee;color:#333;text-decoration:none;">Hủy</a>
        </div>

    </form>

</div>