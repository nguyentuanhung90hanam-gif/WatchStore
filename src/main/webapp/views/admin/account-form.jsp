<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card">

    <h2>
        ${empty user ? "Thêm tài khoản" : "Sửa tài khoản"}
    </h2>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/accounts/save">

        <input type="hidden"
               name="id"
               value="${user.id}">

        <div>
            <label>Họ và tên</label>

            <input type="text"
                   name="fullName"
                   value="${user.fullName}">
        </div>

        <div>
            <label>Email</label>

            <input type="email"
                   name="email"
                   value="${user.email}">
        </div>

        <div>
            <label>Số điện thoại</label>

            <input type="text"
                   name="phone"
                   value="${user.phone}">
        </div>

        <div>
            <label>Vai trò</label>

            <select name="role">

                <option value="ADMIN"
                    ${user != null && user.role.name() == 'ADMIN' ? 'selected' : ''}>
                    ADMIN
                </option>

                <option value="SALES"
                    ${user != null && user.role.name() == 'SALES' ? 'selected' : ''}>
                    SALES
                </option>

                <option value="CUSTOMER"
                    ${user != null && user.role.name() == 'CUSTOMER' ? 'selected' : ''}>
                    CUSTOMER
                </option>

            </select>

        </div>

        <button type="submit"
                class="button button-gold">
            Lưu
        </button>

    </form>

</div>