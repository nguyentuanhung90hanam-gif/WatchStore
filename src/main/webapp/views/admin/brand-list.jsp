<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">

    <title>Quản lý thương hiệu</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/admin.css">

</head>


<body>


<div class="container">


    <div class="card brand-card">


        <!-- HEADER -->

        <div class="page-header">


            <div>

                <div class="brand-title">

                    <div class="brand-icon">
                        ⌚
                    </div>


                    <div>

                        <h2>
                            Quản lý thương hiệu
                        </h2>


                        <p style="color:#777;margin-top:5px">
                            Quản lý các thương hiệu đồng hồ trong hệ thống
                        </p>


                    </div>


                </div>


            </div>



            <a class="btn btn-add"
               href="${pageContext.request.contextPath}/manage/admin/brands/add">

                + Thêm thương hiệu

            </a>


        </div>




        <!-- TOOLBAR -->


        <div class="toolbar">


            <form class="search-form"
                  action="${pageContext.request.contextPath}/manage/admin/brands/search"
                  method="get">


                <div class="search-box">


                    <input
                            type="text"
                            name="keyword"
                            placeholder="🔍 Nhập tên hoặc mã thương hiệu">


                </div>


                <button class="search-btn">
                    Tìm kiếm
                </button>


            </form>



        </div>





        <!-- TABLE -->


        <div class="table-wrapper">


            <table class="admin-table">


                <thead>

                <tr>

                    <th>ID</th>

                    <th>Mã thương hiệu</th>

                    <th>Tên thương hiệu</th>

                    <th>Quốc gia</th>

                    <th>Trạng thái</th>

                    <th>Thao tác</th>

                </tr>

                </thead>



                <tbody>



                <c:forEach items="${brands}" var="b">


                    <tr>


                        <td class="center">
                            #${b.brandID}
                        </td>


                        <td>
                            <b>${b.brandCode}</b>
                        </td>


                        <td>
                            ${b.brandName}
                        </td>


                        <td>
                            🌎 ${b.originCountry}
                        </td>



                        <td class="center">


                            <c:choose>


                                <c:when test="${b.status == 'ACTIVE'}">

                                    <span class="status status-active">
                                        ● Hoạt động
                                    </span>

                                </c:when>


                                <c:otherwise>

                                    <span class="status status-inactive">
                                        ● Ngừng
                                    </span>


                                </c:otherwise>


                            </c:choose>


                        </td>




                        <td>


                            <div class="action-group">


                                <a class="action-btn action-edit"

                                   href="${pageContext.request.contextPath}/manage/admin/brands/edit?id=${b.brandID}">

                                    ✏ Sửa

                                </a>




                                <a class="action-btn action-delete"

                                   onclick="return confirm('Bạn có chắc muốn xóa thương hiệu này?')"

                                   href="${pageContext.request.contextPath}/manage/admin/brands/delete?id=${b.brandID}">

                                    🗑 Xóa

                                </a>


                            </div>


                        </td>


                    </tr>



                </c:forEach>



                </tbody>


            </table>


        </div>



    </div>


</div>



</body>

</html>