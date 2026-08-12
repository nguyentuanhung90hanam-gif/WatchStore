package com.watchstore.controller.admin;

import com.watchstore.model.User;
import com.watchstore.model.Voucher;
import com.watchstore.repository.VoucherRepository;
import com.watchstore.repository.VoucherRepositoryImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@WebServlet(urlPatterns = {"/manage/admin/vouchers", "/manage/admin/vouchers/*"})
public class VoucherController extends HttpServlet {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private VoucherRepository voucherRepository;

    @Override
    public void init() {
        voucherRepository = (VoucherRepository) getServletContext().getAttribute("voucherRepository");
        if (voucherRepository == null) {
            voucherRepository = new VoucherRepositoryImpl();
            getServletContext().setAttribute("voucherRepository", voucherRepository);
        }
    }

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("pageTitle", "Quản lý voucher");
        req.setAttribute("tableKind", "vouchers");
        req.setAttribute("moduleTitle", "Voucher");
        req.setAttribute("moduleKicker", "KHUYẾN MẠI");
        req.setAttribute("moduleDescription", "Quản lý mã giảm giá và chương trình khuyến mãi.");
        req.setAttribute("primaryAction", "Tạo voucher");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getPathInfo();
        if (action == null || action.equals("/")) {
            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.isBlank()) {
                req.setAttribute("vouchers", voucherRepository.search(keyword));
            } else {
                req.setAttribute("vouchers", voucherRepository.findAll());
            }
            setCommonAttributes(req);
            req.setAttribute("contentPage", "/views/shared/management-module.jsp");
            req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
            return;
        }

        switch (action) {
            case "/add": {
                req.setAttribute("adminArea", "admin");
                req.setAttribute("pageTitle", "Thêm voucher");
                
                LocalDateTime now = LocalDateTime.now();
                req.setAttribute("minDateTimeFormatted", now.format(DATE_TIME_FORMATTER));
                req.setAttribute("startAtFormatted", now.format(DATE_TIME_FORMATTER));
                req.setAttribute("endAtFormatted", now.plusMonths(1).format(DATE_TIME_FORMATTER));
                
                req.setAttribute("contentPage", "/views/admin/voucher-form.jsp");
                req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
                break;
            }
            case "/edit": {
                String idStr = req.getParameter("id");
                LocalDateTime now = LocalDateTime.now();
                req.setAttribute("minDateTimeFormatted", now.format(DATE_TIME_FORMATTER));
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        Voucher voucher = voucherRepository.findById(id);
                        if (voucher != null) {
                            req.setAttribute("voucher", voucher);
                            if (voucher.getStartAt() != null) {
                                req.setAttribute("startAtFormatted", voucher.getStartAt().format(DATE_TIME_FORMATTER));
                            }
                            if (voucher.getEndAt() != null) {
                                req.setAttribute("endAtFormatted", voucher.getEndAt().format(DATE_TIME_FORMATTER));
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                req.setAttribute("adminArea", "admin");
                req.setAttribute("pageTitle", "Sửa voucher");
                req.setAttribute("contentPage", "/views/admin/voucher-form.jsp");
                req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
                break;
            }
            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        if (voucherRepository.isVoucherInUse(id)) {
                            req.getSession().setAttribute("errorMessage", "Không thể xóa voucher này vì đã có dữ liệu sử dụng.");
                        } else {
                            boolean deleted = voucherRepository.delete(id);
                            if (deleted) {
                                req.getSession().setAttribute("successMessage", "Xóa voucher thành công.");
                            } else {
                                req.getSession().setAttribute("errorMessage", "Không thể xóa voucher.");
                            }
                        }
                    } catch (Exception e) {
                        req.getSession().setAttribute("errorMessage", "Không thể xóa voucher.");
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/vouchers");
                break;
            }
            case "/search": {
                String keyword = req.getParameter("keyword");
                req.setAttribute("vouchers", voucherRepository.search(keyword));
                setCommonAttributes(req);
                req.setAttribute("contentPage", "/views/shared/management-module.jsp");
                req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
                break;
            }
            default:
                resp.sendRedirect(req.getContextPath() + "/manage/admin/vouchers");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String idStr = req.getParameter("id");
        Integer voucherId = (idStr != null && !idStr.isBlank()) ? Integer.parseInt(idStr) : null;

        String voucherCode = req.getParameter("voucherCode");
        String voucherName = req.getParameter("voucherName");
        String description = req.getParameter("description");
        String discountType = req.getParameter("discountType");
        String discountValueStr = req.getParameter("discountValue");
        String maximumDiscountStr = req.getParameter("maximumDiscount");
        String minimumOrderValueStr = req.getParameter("minimumOrderValue");
        String usageLimitStr = req.getParameter("usageLimit");
        String usageLimitPerUserStr = req.getParameter("usageLimitPerUser");
        String startAtStr = req.getParameter("startAt");
        String endAtStr = req.getParameter("endAt");
        String isPublicStr = req.getParameter("isPublic");
        String status = req.getParameter("status");

        Voucher voucher = new Voucher();
        if (voucherId != null) {
            voucher.setVoucherId(voucherId);
        }

        voucher.setVoucherCode(voucherCode != null ? voucherCode.trim().toUpperCase() : "");
        voucher.setVoucherName(voucherName != null ? voucherName.trim() : "");
        voucher.setDescription(description != null ? description.trim() : null);

        // Validate DiscountType (PERCENT, FIXED, FREESHIP)
        if ("FIXED".equalsIgnoreCase(discountType) || "AMOUNT".equalsIgnoreCase(discountType)) {
            voucher.setDiscountType("FIXED");
        } else if ("FREESHIP".equalsIgnoreCase(discountType)) {
            voucher.setDiscountType("FREESHIP");
        } else {
            voucher.setDiscountType("PERCENT");
        }

        String errorMessage = null;

        // Parse numbers
        try {
            if (discountValueStr != null && !discountValueStr.isBlank()) {
                voucher.setDiscountValue(new BigDecimal(discountValueStr.trim()));
            } else {
                voucher.setDiscountValue(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            errorMessage = "Giá trị giảm không hợp lệ.";
        }

        try {
            if (maximumDiscountStr != null && !maximumDiscountStr.isBlank()) {
                voucher.setMaximumDiscount(new BigDecimal(maximumDiscountStr.trim()));
            } else {
                voucher.setMaximumDiscount(null);
            }
        } catch (Exception e) {
            errorMessage = "Giá trị giảm tối đa không hợp lệ.";
        }

        try {
            if (minimumOrderValueStr != null && !minimumOrderValueStr.isBlank()) {
                voucher.setMinimumOrderValue(new BigDecimal(minimumOrderValueStr.trim()));
            } else {
                voucher.setMinimumOrderValue(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            errorMessage = "Đơn hàng tối thiểu không hợp lệ.";
        }

        try {
            if (usageLimitStr != null && !usageLimitStr.isBlank()) {
                voucher.setUsageLimit(Integer.parseInt(usageLimitStr.trim()));
            } else {
                voucher.setUsageLimit(null);
            }
        } catch (Exception e) {
            errorMessage = "Giới hạn sử dụng không hợp lệ.";
        }

        try {
            if (usageLimitPerUserStr != null && !usageLimitPerUserStr.isBlank()) {
                voucher.setUsageLimitPerUser(Integer.parseInt(usageLimitPerUserStr.trim()));
            } else {
                voucher.setUsageLimitPerUser(1);
            }
        } catch (Exception e) {
            errorMessage = "Lượt dùng / khách không hợp lệ.";
        }

        // Parse StartAt & EndAt
        LocalDateTime startAt = null;
        LocalDateTime endAt = null;

        if (startAtStr != null && !startAtStr.isBlank()) {
            try {
                startAt = LocalDateTime.parse(startAtStr.trim(), DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                try {
                    startAt = LocalDateTime.parse(startAtStr.trim());
                } catch (Exception ex) {
                    errorMessage = "Thời gian bắt đầu không đúng định dạng.";
                }
            }
        }
        if (endAtStr != null && !endAtStr.isBlank()) {
            try {
                endAt = LocalDateTime.parse(endAtStr.trim(), DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                try {
                    endAt = LocalDateTime.parse(endAtStr.trim());
                } catch (Exception ex) {
                    errorMessage = "Thời gian kết thúc không đúng định dạng.";
                }
            }
        }

        voucher.setStartAt(startAt);
        voucher.setEndAt(endAt);

        boolean isPublic = "true".equalsIgnoreCase(isPublicStr) || "on".equalsIgnoreCase(isPublicStr) || "1".equals(isPublicStr);
        voucher.setIsPublic(isPublic);

        voucher.setStatus(status != null && !status.isBlank() ? status : "ACTIVE");

        // Validate Required Fields & Rules
        if (errorMessage == null) {
            if (voucher.getVoucherCode().isBlank()) {
                errorMessage = "Vui lòng nhập Mã Voucher.";
            } else if (voucher.getVoucherCode().length() > 50) {
                errorMessage = "Mã Voucher không được vượt quá 50 ký tự.";
            } else if (voucher.getVoucherName().isBlank()) {
                errorMessage = "Vui lòng nhập Tên Voucher.";
            } else if (voucher.getVoucherName().length() > 200) {
                errorMessage = "Tên Voucher không được vượt quá 200 ký tự.";
            } else if (voucher.getDescription() != null && voucher.getDescription().length() > 1000) {
                errorMessage = "Mô tả Voucher không được vượt quá 1000 ký tự.";
            } else if ("PERCENT".equals(voucher.getDiscountType())) {
                if (voucher.getDiscountValue() == null || voucher.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0 || voucher.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
                    errorMessage = "Với loại giảm giá phần trăm (PERCENT), giá trị giảm phải lớn hơn 0 và nhỏ hơn hoặc bằng 100.";
                }
            } else if ("FIXED".equals(voucher.getDiscountType())) {
                if (voucher.getDiscountValue() == null || voucher.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage = "Với loại giảm giá cố định (FIXED), số tiền giảm phải lớn hơn 0.";
                }
            } else if (voucher.getDiscountValue() == null || voucher.getDiscountValue().compareTo(BigDecimal.ZERO) < 0) {
                errorMessage = "Giá trị giảm giá phải lớn hơn hoặc bằng 0.";
            }

            if (errorMessage == null) {
                if (voucher.getMaximumDiscount() != null && voucher.getMaximumDiscount().compareTo(BigDecimal.ZERO) < 0) {
                    errorMessage = "Giá trị giảm tối đa không được là số âm.";
                } else if (voucher.getMinimumOrderValue() != null && voucher.getMinimumOrderValue().compareTo(BigDecimal.ZERO) < 0) {
                    errorMessage = "Đơn hàng tối thiểu không được là số âm.";
                } else if (voucher.getUsageLimit() != null && voucher.getUsageLimit() < 0) {
                    errorMessage = "Giới hạn sử dụng không được là số âm.";
                } else if (voucher.getUsageLimitPerUser() != null && voucher.getUsageLimitPerUser() < 0) {
                    errorMessage = "Lượt dùng / khách không được là số âm.";
                } else if (voucher.getStartAt() == null) {
                    errorMessage = "Vui lòng chọn Thời gian bắt đầu.";
                } else if (voucherId == null && voucher.getStartAt().isBefore(LocalDateTime.now().minusMinutes(2))) {
                    errorMessage = "Thời gian bắt đầu không được nằm trong quá khứ.";
                } else if (voucher.getEndAt() == null) {
                    errorMessage = "Vui lòng chọn Thời gian kết thúc.";
                } else if (!voucher.getEndAt().isAfter(voucher.getStartAt())) {
                    errorMessage = "Thời gian kết thúc phải diễn ra sau thời gian bắt đầu.";
                } else if (voucherRepository.existsByCode(voucher.getVoucherCode(), voucherId)) {
                    errorMessage = "Mã Voucher '" + voucher.getVoucherCode() + "' đã tồn tại trong hệ thống. Vui lòng nhập mã khác.";
                }
            }
        }

        // If Error -> Re-render form with error message
        if (errorMessage != null) {
            req.setAttribute("errorMessage", errorMessage);
            req.setAttribute("voucher", voucher);
            req.setAttribute("startAtFormatted", startAtStr);
            req.setAttribute("endAtFormatted", endAtStr);
            req.setAttribute("minDateTimeFormatted", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            req.setAttribute("adminArea", "admin");
            req.setAttribute("pageTitle", voucherId == null ? "Thêm voucher" : "Sửa voucher");
            req.setAttribute("contentPage", "/views/admin/voucher-form.jsp");
            req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
            return;
        }

        // Determine CreatedBy and UsedCount
        if (voucherId == null) {
            voucher.setUsedCount(0);
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                Object userObj = session.getAttribute("user");
                if (userObj instanceof User u) {
                    voucher.setCreatedBy(u.getId());
                } else {
                    voucher.setCreatedBy(1);
                }
            } else {
                voucher.setCreatedBy(1);
            }
            voucherRepository.save(voucher);
        } else {
            Voucher existing = voucherRepository.findById(voucherId);
            if (existing != null) {
                voucher.setUsedCount(existing.getUsedCount());
                voucher.setCreatedBy(existing.getCreatedBy());
            }
            voucherRepository.update(voucher);
        }

        resp.sendRedirect(req.getContextPath() + "/manage/admin/vouchers");
    }
}