<%@ page contentType="text/html;charset=UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thương hiệu</title>

    <style>
        body{
            font-family:Arial;
            margin:30px;
        }

        input,textarea,select{
            width:400px;
            padding:8px;
            margin-bottom:10px;
        }

        button{
            padding:8px 20px;
        }
    </style>

</head>
<body>

<h2>${empty brand ? "Thêm thương hiệu" : "Cập nhật thương hiệu"}</h2>

<form method="post"
      action="${pageContext.request.contextPath}/manage/admin/brands">

    <input type="hidden"
           name="id"
           value="${brand.brandID}">

    <p>Mã thương hiệu</p>

    <input
            name="brandCode"
            value="${brand.brandCode}"
            required>

    <p>Tên thương hiệu</p>

    <input
            name="brandName"
            value="${brand.brandName}"
            required>

    <p>Slug</p>

    <input
            name="slug"
            value="${brand.slug}">

    <p>Quốc gia</p>

    <input
            name="originCountry"
            value="${brand.originCountry}">

    <p>Logo URL</p>

    <input
            name="logoUrl"
            value="${brand.logoUrl}">

    <p>Mô tả</p>

    <textarea
            name="description">${brand.description}</textarea>

    <p>Trạng thái</p>

    <select name="status">

        <option value="ACTIVE"
            ${brand.status=="ACTIVE"?"selected":""}>
            ACTIVE
        </option>

        <option value="INACTIVE"
            ${brand.status=="INACTIVE"?"selected":""}>
            INACTIVE
        </option>

    </select>

    <br><br>

    <button>Lưu</button>

</form>

</body>
</html>