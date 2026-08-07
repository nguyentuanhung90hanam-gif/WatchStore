<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card">

    <h2>
        ${empty role ? "Thêm vai trò" : "Sửa vai trò"}
    </h2>


    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/roles/save">


        <input type="hidden"
               name="id"
               value="${role.id}">


        <div>
            <label>Mã vai trò</label>


            <input type="text"
                   name="code"
                   value="${role.code}">
        </div>


        <div>
            <label>Tên vai trò</label>


            <input type="text"
                   name="name"
                   value="${role.name}">
        </div>


        <div>
            <label>Số người</label>


            <input type="number"
                   name="userCount"
                   value="${role.userCount}">
        </div>


      <div>
          <label>Trạng thái</label>


          <select name="status">

              <option value="true"
                  ${role != null && role.status ? 'selected' : ''}>
                  Hoạt động
              </option>


              <option value="false"
                  ${role != null && !role.status ? 'selected' : ''}>
                  Khóa
              </option>

          </select>

      </div>


        <button type="submit"
                class="button button-gold">
            Lưu
        </button>


    </form>

</div>