<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<main class="page-shell account-page"><jsp:include page="/views/shared/account-nav.jsp"/>
<section class="account-content"><div class="account-heading"><div><p class="eyebrow dark">GIAO HÀNG</p><h1>Địa chỉ nhận hàng</h1></div></div>
<div class="address-list">
<c:forEach items="${addresses}" var="a"><article><div><b>${a.name}</b><c:if test="${a['default']}"><span class="status-badge success">Mặc định</span></c:if></div>
<p>${a.phone}</p><p>${a.line}, ${a.ward}, ${a.district}, ${a.province}</p>
<div>
<form action="${cp}/page/address" method="post" style="display:inline"><input type="hidden" name="action" value="default"><input type="hidden" name="id" value="${a.id}"><input type="hidden" name="name" value="${a.name}"><input type="hidden" name="phone" value="${a.phone}"><input type="hidden" name="province" value="${a.province}"><input type="hidden" name="district" value="${a.district}"><input type="hidden" name="ward" value="${a.ward}"><input type="hidden" name="line" value="${a.line}"><button>Đặt mặc định</button></form>
<form action="${cp}/page/address" method="post" style="display:inline"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${a.id}"><button>Xóa</button></form>
</div></article></c:forEach>
</div>
<details class="dashboard-card" style="margin-top:20px"><summary class="button button-dark" style="display:inline-block;cursor:pointer">+ Thêm địa chỉ</summary>
<form action="${cp}/page/address" method="post" class="form-grid two" style="padding-top:20px">
<input type="hidden" name="action" value="save"><label>Người nhận<input name="name" required value="${sessionScope.user.fullName}"></label>
<label>Số điện thoại<input name="phone" required inputmode="numeric" pattern="[0-9]{9,11}" maxlength="11" value="${sessionScope.user.phone}"></label>
<label>Tỉnh/Thành phố<input name="province" required></label><label>Quận/Huyện<input name="district" required></label><label>Phường/Xã<input name="ward" required></label><label>Địa chỉ chi tiết<input name="line" required></label>
<label>Loại<select name="type"><option value="HOME">Nhà riêng</option><option value="OFFICE">Văn phòng</option><option value="OTHER">Khác</option></select></label>
<label class="check"><input type="checkbox" name="default" value="1"> Đặt làm mặc định</label><div class="full-field"><button class="button button-gold">Lưu địa chỉ</button></div>
</form></details>
</section></main>