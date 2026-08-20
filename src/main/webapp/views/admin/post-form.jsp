<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card" style="max-width:900px;margin:0 auto;padding:24px;">

    <h2>
        ${empty post.postId || post.postId == 0 ? "Thêm bài viết" : "Sửa bài viết"}
    </h2>

    <c:if test="${not empty errorMessage}">
        <div style="background:#fff3cd;color:#856404;border:1px solid #ffc107;border-radius:8px;padding:12px 16px;margin-bottom:16px;font-size:0.95em;">
             ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/posts/${empty post.postId || post.postId == 0 ? 'save' : 'update'}">

        <c:if test="${not empty post.postId && post.postId > 0}">
            <input type="hidden" name="postId" value="${post.postId}">
        </c:if>

        <div style="display:grid;grid-template-columns:2fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Tiêu đề bài viết <span style="color:red;">*</span></label>
                <input type="text" name="title" value="${post.title}" placeholder="VD: Hướng dẫn chọn đồng hồ nam phù hợp cổ tay" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>Loại bài viết</label>
                <select name="postType" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="NEWS" ${empty post || post.postType == 'NEWS' ? 'selected' : ''}>NEWS (Tin tức)</option>
                    <option value="GUIDE" ${post.postType == 'GUIDE' ? 'selected' : ''}>GUIDE (Hướng dẫn)</option>
                    <option value="PROMOTION" ${post.postType == 'PROMOTION' ? 'selected' : ''}>PROMOTION (Khuyến mãi)</option>
                    <option value="POLICY" ${post.postType == 'POLICY' ? 'selected' : ''}>POLICY (Chính sách)</option>
                </select>
            </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Slug <span style="color:red;">*</span></label>
                <input type="text" name="slug" value="${post.slug}" placeholder="VD: huong-dan-chon-dong-ho-nam" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>URL Hình ảnh đại diện (Thumbnail)</label>
                <input type="text" name="thumbnailUrl" value="${post.thumbnailUrl}" placeholder="VD: /assets/images/posts/post-1.jpg" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
        </div>

        <div style="margin-bottom:16px;">
            <label>Tóm tắt ngắn</label>
            <textarea name="summary" rows="2" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;resize:vertical;" placeholder="Mô tả tóm tắt hiển thị ngoài danh sách bài viết...">${post.summary}</textarea>
        </div>

        <div style="margin-bottom:16px;">
            <label>Nội dung chi tiết <span style="color:red;">*</span></label>
            <textarea name="content" rows="8" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;resize:vertical;" required placeholder="Nội dung bài viết chi tiết...">${post.content}</textarea>
        </div>

        <div style="margin-bottom:16px;">
            <label>Trạng thái</label>
            <select name="status" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;max-width:300px;">
                <option value="DRAFT" ${empty post || post.status == 'DRAFT' ? 'selected' : ''}>DRAFT (Bản nháp)</option>
                <option value="PUBLISHED" ${post.status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED (Xuất bản)</option>
                <option value="HIDDEN" ${post.status == 'HIDDEN' ? 'selected' : ''}>HIDDEN (Ẩn)</option>
            </select>
        </div>

        <div style="display:flex;gap:12px;margin-top:24px;">
            <button type="submit" class="button button-gold">${empty post.postId || post.postId == 0 ? "Thêm mới" : "Lưu thay đổi"}</button>
            <a href="${pageContext.request.contextPath}/manage/admin/posts" class="button" style="background:#eee;color:#333;text-decoration:none;padding:10px 20px;border-radius:6px;">Hủy</a>
        </div>
    </form>
</div>
