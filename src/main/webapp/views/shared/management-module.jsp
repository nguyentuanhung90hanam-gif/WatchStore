<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>


<div class="module-heading">

    <div class="module-title-area">

        <p class="eyebrow dark">${moduleKicker}</p>

        <h2>${moduleTitle}</h2>

        <p class="module-desc">
            ${moduleDescription}
        </p>

    </div>


   <c:if test="${not empty primaryAction}">

       <c:choose>

           <c:when test="${tableKind == 'products'}">

               <a class="button button-gold"
                  href="${pageContext.request.contextPath}/manage/admin/products/add">
                   Thêm sản phẩm
               </a>

           </c:when>


           <c:when test="${tableKind == 'roles'}">

               <a class="button button-gold"
                  href="${pageContext.request.contextPath}/manage/admin/roles/add">
                   Thêm vai trò
               </a>

           </c:when>
           <c:when test="${tableKind == 'accounts'}">

               <a class="button button-gold"
                  href="${pageContext.request.contextPath}/manage/admin/accounts/add">
                   Thêm tài khoản
               </a>

           </c:when>


           <c:otherwise>

               <button class="button button-gold"
                       data-demo-toast="${primaryAction}">
                   ${primaryAction}
               </button>

           </c:otherwise>

       </c:choose>

   </c:if>


</div>



<div class="module-toolbar">

    <form method="get"
          action="${pageContext.request.contextPath}/manage/admin/${tableKind == 'roles' ? 'roles' : tableKind == 'accounts' ? 'accounts' : tableKind == 'brands' ? 'brands' : 'products'}">

        <div class="search-box">

            <input
                type="text"
                name="keyword"
                value="${param.keyword}"
                placeholder="Nhập từ khóa tìm kiếm">


           <button type="submit">
               Tìm kiếm
           </button>

           <c:if test="${not empty param.keyword}">
              <a class="reset-btn"
                 href="${pageContext.request.contextPath}/manage/admin/${tableKind == 'roles' ? 'roles' : tableKind == 'accounts' ? 'accounts' : tableKind == 'brands' ? 'brands' : 'products'}">
                  ↻ Đặt lại
              </a>
           </c:if>

        </div>

    </form>





</div>



<c:choose>

    <c:when test="${tableKind == 'orders'}">

        <div class="dashboard-card">

            <div class="table-wrap">

                <table>

                    <thead>
                    <tr>
                        <th>Mã đơn</th>
                        <th>Khách hàng</th>
                        <th>Thời gian</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                        <th></th>
                    </tr>
                    </thead>


                    <tbody>

                    <c:forEach items="${orders}" var="order">

                        <tr>

                            <td>
                                <b>#${order.code}</b>
                            </td>

                            <td>
                                ${order.customerName}
                            </td>

                            <td>
                                ${order.createdAt}
                            </td>

                            <td>
                                <fmt:formatNumber value="${order.total}" pattern="#,##0"/>₫
                            </td>

                            <td>
                                <span class="status-badge ${order.status.cssClass}">
                                    ${order.status.label}
                                </span>
                            </td>

                            <td>
                                <a class="table-action"
                                   href="${cp}/manage/sales/order-detail?code=${order.code}">
                                    Chi tiết →
                                </a>
                            </td>

                        </tr>

                    </c:forEach>

                    </tbody>

                </table>

            </div>

        </div>


    </c:when>



    <c:when test="${tableKind == 'products'}">


        <div class="dashboard-card">

            <div class="table-wrap">

                <table>

                    <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>SKU</th>
                        <th>Giá bán</th>
                        <th>Tồn kho</th>
                        <th>Trạng thái</th>
                        <th></th>
                    </tr>
                    </thead>


                    <tbody>


                    <c:forEach items="${products}" var="product">

                        <tr>

                            <td>

                                <div class="table-product">

                                    <img src="${cp}/assets/images/${product.image}" alt="">

                                    <span>
                                        <b>${product.name}</b>
                                        <small>${product.brand}</small>
                                    </span>

                                </div>

                            </td>


                            <td>${product.sku}</td>


                            <td>
                                <fmt:formatNumber value="${product.price}" pattern="#,##0"/>₫
                            </td>


                            <td>
                                <b>${product.stock}</b>
                            </td>


                            <td>

                                <span class="status-badge success">
                                    Đang bán
                                </span>

                            </td>


                            <td>
                                <button class="table-action">
                                    Chỉnh sửa
                                </button>
                            </td>


                        </tr>


                    </c:forEach>


                    </tbody>


                </table>


            </div>


        </div>


    </c:when>



    <c:when test="${tableKind == 'permissions'}">


        <div class="dashboard-card permission-matrix">

            <table>

                <thead>

                <tr>
                    <th>Module</th>
                    <th>Xem</th>
                    <th>Thêm</th>
                    <th>Sửa</th>
                    <th>Duyệt</th>
                    <th>Xuất báo cáo</th>
                </tr>

                </thead>


                <tbody>


                <c:forTokens items="Sản phẩm,Đơn hàng,Khách hàng,Kho hàng,Voucher,Báo cáo"
                             delims=","
                             var="item">


                    <tr>

                        <td>
                            <b>${item}</b>
                        </td>

                        <td><input type="checkbox" checked></td>
                        <td><input type="checkbox" checked></td>
                        <td><input type="checkbox" checked></td>
                        <td><input type="checkbox"></td>
                        <td><input type="checkbox"></td>

                    </tr>


                </c:forTokens>


                </tbody>

            </table>


        </div>


    </c:when>

    <c:when test="${tableKind == 'accounts'}">


    <div class="dashboard-card">

    <div class="table-wrap">

    <table>

    <thead>

    <tr>
        <th>ID</th>
        <th>Họ tên</th>
        <th>Email</th>
        <th>Số điện thoại</th>
        <th>Vai trò</th>
        <th></th>
    </tr>

    </thead>


    <tbody>


    <c:forEach items="${users}" var="user">

    <tr>

    <td>
        ${user.id}
    </td>


    <td>
        <b>${user.fullName}</b>
    </td>


    <td>
        ${user.email}
    </td>


    <td>
        ${user.phone}
    </td>


    <td>

    <span class="status-badge success">
        ${user.role.label}
    </span>

    </td>


    <td>

        <a class="table-action"
           href="${pageContext.request.contextPath}/manage/admin/accounts/edit?id=${user.id}">
            Sửa
        </a>

        <a class="table-action"
           href="${pageContext.request.contextPath}/manage/admin/accounts/delete?id=${user.id}"
           onclick="return confirm('Bạn có chắc muốn xóa tài khoản này?')">
            Xóa
        </a>

    </td>


    </tr>


    </c:forEach>


    </tbody>


    </table>

    </div>

    </div>


    </c:when>
<c:when test="${tableKind == 'roles'}">

    <div class="dashboard-card">

        <div class="table-wrap">

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Mã</th>
                    <th>Tên vai trò</th>
                    <th>Số người</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
                </thead>

                <tbody>

                <c:forEach items="${roles}" var="role">

                    <tr>

                        <td>${role.id}</td>

                        <td>${role.code}</td>

                        <td>${role.name}</td>

                        <td>${role.userCount}</td>

                        <td>

                            <span class="status-badge ${role.status ? 'success' : 'danger'}">
                                ${role.status ? 'Hoạt động' : 'Khóa'}
                            </span>

                        </td>

                        <td>

                          <a class="table-action"
                             href="${pageContext.request.contextPath}/manage/admin/roles/edit?id=${role.id}">
                              Sửa
                          </a>

                          <a class="table-action"
                             href="${pageContext.request.contextPath}/manage/admin/roles/delete?id=${role.id}"
                             onclick="return confirm('Bạn có chắc muốn xóa vai trò này?')">
                              Xóa
                          </a>

                        </td>

                    </tr>

                </c:forEach>

                </tbody>

            </table>

        </div>

    </div>

</c:when>

    <c:otherwise>


        <div class="brand-grid">


            <c:forEach begin="1" end="6" var="i">


                <article class="brand-card">


                    <div class="module-card-icon">
                        ${moduleIcon}
                    </div>


                    <span class="status-badge success">
                        Đang hoạt động
                    </span>


                    <h3>
                        ${moduleItemName} ${i}
                    </h3>


                    <p>
                        Dữ liệu mẫu phục vụ phát triển giao diện và luồng nghiệp vụ.
                    </p>


                    <div>

                        <button>
                            Chi tiết
                        </button>

                        <button>
                            Chỉnh sửa
                        </button>

                    </div>


                </article>


            </c:forEach>


        </div>


    </c:otherwise>


</c:choose>





