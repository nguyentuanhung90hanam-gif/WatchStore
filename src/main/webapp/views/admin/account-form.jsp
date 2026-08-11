<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card">

    <h2>
        <c:choose>
            <c:when test="${formMode == 'edit'}">
                Sửa tài khoản
            </c:when>
            <c:otherwise>
                Thêm tài khoản
            </c:otherwise>
        </c:choose>
    </h2>

    <%-- Hiển thị lỗi validation --%>
    <c:if test="${not empty errorMessage}">
        <div style="background:#fff3cd;color:#856404;border:1px solid #ffc107;border-radius:8px;padding:12px 16px;margin-bottom:16px;font-size:0.95em;">
            ⚠ ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/accounts/${formMode == 'edit' ? 'update' : 'save'}"
          autocomplete="off">

        <%-- Hidden ID khi sửa --%>
        <c:if test="${formMode == 'edit' && not empty requestScope.account && requestScope.account.userId > 0}">
            <input type="hidden" name="userId" value="${requestScope.account.userId}">
        </c:if>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">

            <div>
                <label>Email <span style="color:red;">*</span></label>
                <input type="email" name="email"
                       value="${not empty requestScope.account ? requestScope.account.email : ''}"
                       placeholder="example@watchstore.vn"
                       autocomplete="off"
                       required
                       style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>

            <div>
                <label>Mật khẩu
                    <c:choose>
                        <c:when test="${formMode == 'edit'}">
                            <small style="color:#888;">(để trống nếu không đổi)</small>
                        </c:when>
                        <c:otherwise>
                            <span style="color:red;">*</span>
                        </c:otherwise>
                    </c:choose>
                </label>
                <input type="password" name="password"
                       value=""
                       placeholder="${formMode == 'edit' ? 'Để trống giữ mật khẩu cũ' : 'Nhập mật khẩu'}"
                       autocomplete="new-password"
                       ${formMode == 'edit' ? '' : 'required'}
                       style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>

            <div>
                <label>Họ và tên <span style="color:red;">*</span></label>
                <input type="text" name="fullName"
                       value="${not empty requestScope.account ? requestScope.account.fullName : ''}"
                       placeholder="Nguyễn Văn A"
                       autocomplete="off"
                       required
                       style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>

            <div>
                <label>Số điện thoại</label>
                <input type="text" name="phone"
                       value="${not empty requestScope.account ? requestScope.account.phone : ''}"
                       placeholder="0988000001"
                       autocomplete="off"
                       style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>

            <div>
                <label>Giới tính</label>
                <select name="gender"
                        style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="">-- Chọn --</option>
                    <option value="MALE" ${not empty requestScope.account && requestScope.account.gender == 'MALE' ? 'selected' : ''}>Nam</option>
                    <option value="FEMALE" ${not empty requestScope.account && requestScope.account.gender == 'FEMALE' ? 'selected' : ''}>Nữ</option>
                    <option value="OTHER" ${not empty requestScope.account && requestScope.account.gender == 'OTHER' ? 'selected' : ''}>Khác</option>
                </select>
            </div>

            <div>
                <label>Ngày sinh</label>
                <input type="date" name="dateOfBirth"
                       value="${not empty requestScope.account ? requestScope.account.dateOfBirth : ''}"
                       autocomplete="off"
                       style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>

            <div>
                <label>Trạng thái</label>
                <select name="status"
                        style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="ACTIVE"   ${empty requestScope.account || empty requestScope.account.status || requestScope.account.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                    <option value="INACTIVE" ${not empty requestScope.account && requestScope.account.status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng</option>
                    <option value="LOCKED"   ${not empty requestScope.account && requestScope.account.status == 'LOCKED' ? 'selected' : ''}>Khóa</option>
                </select>
            </div>

        </div>

        <%-- Chọn nhiều Roles (checkbox M:N) --%>
        <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:8px;font-weight:600;">Vai trò</label>
            <div style="display:flex;flex-wrap:wrap;gap:12px;">
                <c:forEach items="${allRoles}" var="r">
                    <c:set var="isChecked" value="false"/>
                    <c:if test="${not empty selectedRoleIds}">
                        <c:forEach items="${selectedRoleIds}" var="rid">
                            <c:if test="${rid == r.roleId}">
                                <c:set var="isChecked" value="true"/>
                            </c:if>
                        </c:forEach>
                    </c:if>
                    <label style="display:inline-flex;align-items:center;gap:4px;padding:6px 12px;border:1px solid #ddd;border-radius:6px;cursor:pointer;background:${isChecked ? '#e8f5e9' : '#fff'};">
                        <input type="checkbox" name="roleIds" value="${r.roleId}"
                               ${isChecked ? 'checked' : ''}>
                        ${r.roleName}
                        <small style="color:#888;">(${r.roleCode})</small>
                    </label>
                </c:forEach>
            </div>
        </div>

        <div style="display:flex;gap:12px;">
            <button type="submit" class="button button-gold">Lưu</button>
            <a href="${pageContext.request.contextPath}/manage/admin/accounts"
               class="button" style="background:#eee;color:#333;text-decoration:none;">Hủy</a>
        </div>

    </form>

</div>