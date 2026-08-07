<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">

    <title>Quản lý danh mục</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/admin.css">

</head>


<body>


<div class="content">


    <h1 class="page-title">
        Quản lý danh mục
    </h1>



    <!-- TOOLBAR -->

    <div class="toolbar">


        <form action="${pageContext.request.contextPath}/manage/admin/categories/search"
              method="get">


            <input type="text"
                   name="keyword"
                   placeholder="Nhập tên hoặc mã danh mục">


            <button type="submit">
                Tìm kiếm
            </button>


        </form>



        <a class="btn-add"
           href="${pageContext.request.contextPath}/manage/admin/categories/add">

            + Thêm danh mục

        </a>


    </div>





    <!-- TABLE -->


    <div class="table-container">


        <table class="admin-table">


            <thead>

            <tr>

                <th>ID</th>

                <th>Mã</th>

                <th>Tên danh mục</th>

                <th>Mô tả</th>

                <th>Thứ tự</th>

                <th>Trạng thái</th>

                <th>Thao tác</th>


            </tr>

            </thead>



            <tbody>


            <c:forEach items="${categories}" var="c">


                <tr>


                    <td>
                        ${c.categoryId}
                    </td>



                    <td>
                        ${c.categoryCode}
                    </td>



                    <td>
                        ${c.categoryName}
                    </td>



                    <td>
                        ${c.description}
                    </td>



                    <td>
                        ${c.sortOrder}
                    </td>



                    <td>

                        <c:choose>

                            <c:when test="${c.status == 'ACTIVE'}">

                                <span class="status active">
                                    Hoạt động
                                </span>

                            </c:when>


                            <c:otherwise>

                                <span class="status inactive">
                                    Ngừng hoạt động
                                </span>

                            </c:otherwise>


                        </c:choose>


                    </td>





                    <td>


                        <a class="btn-edit"
                           href="${pageContext.request.contextPath}/manage/admin/categories/edit?id=${c.categoryId}">

                            Sửa

                        </a>



                        <a class="btn-delete"
                           onclick="return confirm('Bạn có chắc muốn xóa?')"
                           href="${pageContext.request.contextPath}/manage/admin/categories/delete?id=${c.categoryId}">

                            Xóa

                        </a>


                    </td>


                </tr>


            </c:forEach>



            </tbody>


        </table>


    </div>


</div>


</body>
</html>