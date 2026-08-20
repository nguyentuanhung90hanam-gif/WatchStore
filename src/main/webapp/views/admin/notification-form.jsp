<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card" style="max-width:800px;margin:0 auto;padding:24px;">

    <h2>
        ${empty notification.notificationId || notification.notificationId == 0 ? "Tạo thông báo" : "Sửa thông báo"}
    </h2>

    <c:if test="${not empty errorMessage}">
        <div style="background:#fff3cd;color:#856404;border:1px solid #ffc107;border-radius:8px;padding:12px 16px;margin-bottom:16px;font-size:0.95em;">
             ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/notifications/${empty notification.notificationId || notification.notificationId == 0 ? 'save' : 'update'}">

        <c:if test="${not empty notification.notificationId && notification.notificationId > 0}">
            <input type="hidden" name="notificationId" value="${notification.notificationId}">
        </c:if>

        <div style="display:grid;grid-template-columns:2fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Tiêu đề thông báo <span style="color:red;">*</span></label>
                <input type="text" name="title" value="${notification.title}" placeholder="VD: Khuyến mãi mùa hè 2026" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>Loại thông báo</label>
                <select name="notificationType" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="SYSTEM" ${empty notification || notification.notificationType == 'SYSTEM' ? 'selected' : ''}>SYSTEM (Hệ thống)</option>
                    <option value="PROMOTION" ${notification.notificationType == 'PROMOTION' ? 'selected' : ''}>PROMOTION (Khuyến mãi)</option>
                    <option value="ORDER" ${notification.notificationType == 'ORDER' ? 'selected' : ''}>ORDER (Đơn hàng)</option>
                </select>
            </div>
        </div>

        <div style="margin-bottom:16px;">
            <label>Nội dung thông báo <span style="color:red;">*</span></label>
            <textarea name="message" rows="4" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;resize:vertical;" required placeholder="Nội dung chi tiết của thông báo...">${notification.message}</textarea>
        </div>

        <div style="margin-bottom:16px;">
            <label>URL Liên kết (Target URL)</label>
            <input type="text" name="targetUrl" value="${notification.targetUrl}" placeholder="VD: /page/vouchers" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
        </div>

        <div style="display:flex;gap:12px;margin-top:24px;">
            <button type="submit" class="button button-gold">${empty notification.notificationId || notification.notificationId == 0 ? "Tạo mới" : "Lưu thay đổi"}</button>
            <a href="${pageContext.request.contextPath}/manage/admin/notifications" class="button" style="background:#eee;color:#333;text-decoration:none;padding:10px 20px;border-radius:6px;">Hủy</a>
        </div>
    </form>
</div>
