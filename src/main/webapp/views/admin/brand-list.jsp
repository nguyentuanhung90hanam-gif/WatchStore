<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="activeCount" value="0" />
<c:forEach items="${brands}" var="brandItem">
    <c:if test="${brandItem.status == 'ACTIVE'}">
        <c:set var="activeCount" value="${activeCount + 1}" />
    </c:if>
</c:forEach>

<div class="admin-page-head">
    <div>
        <p class="admin-eyebrow">DANH MỤC BÁN HÀNG</p>
        <h2>Thương hiệu đồng hồ</h2>
        <p>Quản lý nhận diện, xuất xứ và trạng thái kinh doanh của từng thương hiệu.</p>
    </div>
    <a class="admin-primary-button" href="${cp}/manage/admin/brands/add">
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>
        <span>Thêm thương hiệu</span>
    </a>
</div>

<div class="brand-summary-grid">
    <article>
        <span class="brand-summary-icon"><svg viewBox="0 0 24 24"><path d="m12 3 8 9-8 9-8-9z"/><circle cx="12" cy="12" r="2"/></svg></span>
        <div><small>TỔNG THƯƠNG HIỆU</small><b>${fn:length(brands)}</b><span>Đang hiển thị trong danh sách</span></div>
    </article>
    <article>
        <span class="brand-summary-icon success"><svg viewBox="0 0 24 24"><path d="M5 13 9 17 19 7"/></svg></span>
        <div><small>ĐANG HOẠT ĐỘNG</small><b>${activeCount}</b><span>Sẵn sàng bán trên cửa hàng</span></div>
    </article>
    <article>
        <span class="brand-summary-icon muted"><svg viewBox="0 0 24 24"><path d="M5 12h14"/></svg></span>
        <div><small>TẠM NGỪNG</small><b>${fn:length(brands) - activeCount}</b><span>Không hiển thị cho khách hàng</span></div>
    </article>
</div>

<section class="brand-panel">
    <div class="brand-toolbar">
        <form action="${cp}/manage/admin/brands/search" method="get" role="search">
            <label class="brand-search">
                <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg>
                <input type="search" name="keyword" value="${keyword}" placeholder="Tìm theo mã hoặc tên thương hiệu..." autocomplete="off">
            </label>
            <button type="submit">Tìm kiếm</button>
            <c:if test="${not empty keyword}">
                <a class="brand-reset" href="${cp}/manage/admin/brands">Xóa lọc</a>
            </c:if>
        </form>
        <div class="brand-toolbar-meta">
            <span>${fn:length(brands)} kết quả</span>
            <button type="button" data-demo-toast="Dữ liệu thương hiệu đã được làm mới">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 7v5h-5M4 17v-5h5M6 9a7 7 0 0 1 12-2l2 5M18 15a7 7 0 0 1-12 2l-2-5"/></svg>
                Làm mới
            </button>
        </div>
    </div>

    <div class="brand-table-wrap">
        <table class="brand-table">
            <thead>
                <tr>
                    <th>Thương hiệu</th>
                    <th>Mã</th>
                    <th>Đường dẫn</th>
                    <th>Xuất xứ</th>
                    <th>Trạng thái</th>
                    <th class="brand-actions-heading">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${brands}" var="b">
                    <tr>
                        <td>
                            <div class="brand-identity">
                                <c:choose>
                                    <c:when test="${not empty b.logoUrl}">
                                        <span class="brand-logo"><img src="${b.logoUrl}" alt="Logo ${b.brandName}" loading="lazy" onerror="this.parentElement.classList.add('is-fallback');this.remove();"><i>${fn:toUpperCase(fn:substring(b.brandName, 0, 1))}</i></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="brand-logo is-fallback"><i>${fn:toUpperCase(fn:substring(b.brandName, 0, 1))}</i></span>
                                    </c:otherwise>
                                </c:choose>
                                <span><b><c:out value="${b.brandName}" /></b><small>ID #${b.brandID}</small></span>
                            </div>
                        </td>
                        <td><code><c:out value="${b.brandCode}" /></code></td>
                        <td><span class="brand-slug">/<c:out value="${b.slug}" /></span></td>
                        <td>
                            <span class="brand-country">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a15 15 0 0 1 0 18M12 3a15 15 0 0 0 0 18"/></svg>
                                <c:out value="${empty b.originCountry ? 'Chưa cập nhật' : b.originCountry}" />
                            </span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${b.status == 'ACTIVE'}"><span class="admin-status active"><i></i>Đang hoạt động</span></c:when>
                                <c:otherwise><span class="admin-status inactive"><i></i>Tạm ngừng</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <div class="brand-actions">
                                <a href="${cp}/manage/admin/brands/edit?id=${b.brandID}" aria-label="Sửa ${b.brandName}" title="Chỉnh sửa">
                                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14 5 5 5M4 20l4-1 11-11-4-4L4 15z"/></svg>
                                </a>
                                <a class="danger" href="${cp}/manage/admin/brands/delete?id=${b.brandID}" aria-label="Xóa ${b.brandName}" title="Xóa" data-confirm-delete="${b.brandName}">
                                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M9 7V4h6v3M7 7l1 13h8l1-13M10 11v5M14 11v5"/></svg>
                                </a>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty brands}">
                    <tr>
                        <td colspan="6">
                            <div class="brand-empty-state">
                                <span><svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg></span>
                                <h3>Không tìm thấy thương hiệu</h3>
                                <p>Thử một từ khóa khác hoặc tạo thương hiệu mới cho hệ thống.</p>
                                <a href="${cp}/manage/admin/brands/add">Thêm thương hiệu</a>
                            </div>
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</section>
