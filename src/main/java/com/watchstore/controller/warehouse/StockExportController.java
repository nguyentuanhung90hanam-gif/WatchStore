package com.watchstore.controller.warehouse;

import com.watchstore.model.StockExport;
import com.watchstore.model.StockExportItem;
import com.watchstore.model.User;
import com.watchstore.repository.InventoryRepository;
import com.watchstore.repository.StockExportRepository;
import com.watchstore.repository.VariantRepository;
import com.watchstore.util.PdfGenerator;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {
        "/manage/warehouse/exports",
        "/manage/warehouse/export-create",
        "/manage/warehouse/export-detail",
        "/manage/warehouse/export-pdf",
        "/manage/warehouse/export-add-item",
        "/manage/warehouse/export-update-item",
        "/manage/warehouse/export-delete-item",
        "/manage/warehouse/export-submit",
        "/manage/warehouse/export-approve",
        "/manage/warehouse/export-cancel"
})
public class StockExportController extends HttpServlet {

    private StockExportRepository exportRepo;
    private InventoryRepository inventoryRepo;
    private VariantRepository variantRepo;

    @Override
    public void init() {
        exportRepo = new StockExportRepository();
        inventoryRepo = new InventoryRepository();
        variantRepo = new VariantRepository();
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getServletPath();

        try {

            switch (path) {

                case "/manage/warehouse/exports":

                    handleExportList(req);

                    render(
                            req,
                            resp,
                            "export-list",
                            "Phiếu xuất kho"
                    );

                    return;

                case "/manage/warehouse/export-create":

                    handleExportCreateForm(req);

                    render(
                            req,
                            resp,
                            "export-create",
                            "Tạo phiếu xuất"
                    );

                    return;

                case "/manage/warehouse/export-detail":

                    handleExportDetail(req);

                    render(
                            req,
                            resp,
                            "export-detail",
                            "Chi tiết phiếu xuất"
                    );

                    return;

                case "/manage/warehouse/export-pdf":

                    handleExportPdf(
                            req,
                            resp
                    );

                    return;

                default:

                    resp.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (Exception e) {

            e.printStackTrace();

            req.getSession().setAttribute(
                    "errorMsg",
                    getErrorMessage(e)
            );

            resp.sendRedirect(
                    req.getContextPath()
                            + "/manage/warehouse/exports"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getServletPath();

        try {

            int userId =
                    getCurrentUserId(req);

            switch (path) {

                case "/manage/warehouse/export-create":

                    handleExportCreate(
                            req,
                            userId,
                            resp
                    );

                    return;

                case "/manage/warehouse/export-add-item":

                    handleExportAddItem(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/export-update-item":

                    handleExportUpdateItem(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/export-delete-item":

                    handleExportDeleteItem(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/export-submit":

                    handleExportSubmit(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/export-approve":

                    handleExportApprove(
                            req,
                            userId,
                            resp
                    );

                    return;

                case "/manage/warehouse/export-cancel":

                    handleExportCancel(
                            req,
                            resp
                    );

                    return;

                default:

                    resp.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (Exception e) {

            e.printStackTrace();

            req.getSession().setAttribute(
                    "errorMsg",
                    getErrorMessage(e)
            );

            String exportId =
                    req.getParameter("exportId");

            if (exportId != null &&
                    !exportId.trim().isEmpty()) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/export-detail?id="
                                + exportId
                );

            } else {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/exports"
                );
            }
        }
    }

    private void handleExportList(
            HttpServletRequest req
    ) {

        req.setAttribute(
                "exports",
                exportRepo.findAll()
        );
    }

    private void handleExportCreateForm(
            HttpServletRequest req
    ) {

        req.setAttribute(
                "warehouses",
                inventoryRepo.findAllWarehouses()
        );

        req.setAttribute(
                "variants",
                variantRepo.findAll()
        );
    }

    private void handleExportDetail(
            HttpServletRequest req
    ) throws Exception {

        String idParam =
                req.getParameter("id");

        if (idParam == null ||
                idParam.trim().isEmpty()) {

            throw new Exception(
                    "Thiếu mã phiếu xuất."
            );
        }

        long id =
                parsePositiveLong(
                        idParam,
                        "Mã phiếu xuất không hợp lệ."
                );

        StockExport export =
                exportRepo.findById(id);

        if (export == null) {

            throw new Exception(
                    "Không tìm thấy phiếu xuất."
            );
        }

        req.setAttribute(
                "export",
                export
        );

        req.setAttribute(
                "variants",
                variantRepo.findAll()
        );
    }

    private void handleExportCreate(
            HttpServletRequest req,
            int userId,
            HttpServletResponse resp
    ) throws Exception {

        int warehouseId =
                parsePositiveInt(
                        req.getParameter("warehouseId"),
                        "Kho không hợp lệ."
                );

        String[] variantIds =
                req.getParameterValues(
                        "variantIds"
                );

        String[] quantities =
                req.getParameterValues(
                        "quantities"
                );

        if (variantIds == null ||
                variantIds.length == 0) {

            throw new Exception(
                    "Phiếu xuất phải có ít nhất một sản phẩm."
            );
        }

        StockExport export =
                new StockExport();

        export.setExportCode(
                "EXP-" + System.currentTimeMillis()
        );

        export.setWarehouseId(
                warehouseId
        );

        export.setExportType(
                optionalString(
                        req.getParameter(
                                "exportType"
                        )
                )
        );

        export.setReceiverName(
                optionalString(
                        req.getParameter(
                                "receiverName"
                        )
                )
        );

        export.setNote(
                optionalString(
                        req.getParameter(
                                "note"
                        )
                )
        );

        export.setCreatedBy(
                userId
        );

        String orderIdStr =
                optionalString(
                        req.getParameter(
                                "orderId"
                        )
                );

        if (orderIdStr != null) {

            export.setOrderId(
                    parsePositiveLong(
                            orderIdStr,
                            "Mã đơn hàng không hợp lệ."
                    )
            );
        }

        List<StockExportItem> items =
                new ArrayList<>();

        for (int i = 0;
             i < variantIds.length;
             i++) {

            if (variantIds[i] == null ||
                    variantIds[i].trim().isEmpty()) {

                continue;
            }

            if (quantities == null ||
                    i >= quantities.length) {

                throw new Exception(
                        "Thiếu số lượng sản phẩm."
                );
            }

            int variantId =
                    parsePositiveInt(
                            variantIds[i],
                            "Biến thể không hợp lệ."
                    );

            int quantity =
                    parsePositiveInt(
                            quantities[i],
                            "Số lượng xuất phải lớn hơn 0."
                    );

            StockExportItem item =
                    new StockExportItem();

            item.setVariantId(
                    variantId
            );

            item.setQuantity(
                    quantity
            );

            items.add(item);
        }

        if (items.isEmpty()) {

            throw new Exception(
                    "Phiếu xuất phải có ít nhất một sản phẩm hợp lệ."
            );
        }

        export.setItems(
                items
        );

        long exportId =
                exportRepo.createDraft(
                        export
                );

        req.getSession().setAttribute(
                "successMsg",
                "Tạo phiếu xuất nháp thành công!"
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/export-detail?id="
                        + exportId
        );
    }

    private void handleExportAddItem(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long exportId =
                parsePositiveLong(
                        req.getParameter("exportId"),
                        "Mã phiếu xuất không hợp lệ."
                );

        int variantId =
                parsePositiveInt(
                        req.getParameter("variantId"),
                        "Phải chọn biến thể."
                );

        int quantity =
                parsePositiveInt(
                        req.getParameter("quantity"),
                        "Số lượng phải lớn hơn 0."
                );

        StockExportItem item =
                new StockExportItem();

        item.setVariantId(
                variantId
        );

        item.setQuantity(
                quantity
        );

        exportRepo.addItem(
                exportId,
                item
        );

        req.getSession().setAttribute(
                "successMsg",
                "Thêm sản phẩm thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/export-detail?id="
                        + exportId
        );
    }

    private void handleExportUpdateItem(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long exportId =
                parsePositiveLong(
                        req.getParameter("exportId"),
                        "Mã phiếu xuất không hợp lệ."
                );

        long itemId =
                parsePositiveLong(
                        req.getParameter("itemId"),
                        "Mã sản phẩm trong phiếu không hợp lệ."
                );

        int quantity =
                parsePositiveInt(
                        req.getParameter("quantity"),
                        "Số lượng phải lớn hơn 0."
                );

        exportRepo.updateItem(
                itemId,
                quantity
        );

        req.getSession().setAttribute(
                "successMsg",
                "Cập nhật thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/export-detail?id="
                        + exportId
        );
    }

    private void handleExportDeleteItem(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long exportId =
                parsePositiveLong(
                        req.getParameter("exportId"),
                        "Mã phiếu xuất không hợp lệ."
                );

        long itemId =
                parsePositiveLong(
                        req.getParameter("itemId"),
                        "Mã sản phẩm trong phiếu không hợp lệ."
                );

        exportRepo.deleteItem(
                itemId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Đã xóa sản phẩm."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/export-detail?id="
                        + exportId
        );
    }

    private void handleExportSubmit(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long exportId =
                parsePositiveLong(
                        req.getParameter("exportId"),
                        "Mã phiếu xuất không hợp lệ."
                );

        exportRepo.submitForApproval(
                exportId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Đã gửi phiếu chờ duyệt."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/export-detail?id="
                        + exportId
        );
    }

    private void handleExportApprove(
            HttpServletRequest req,
            int userId,
            HttpServletResponse resp
    ) throws Exception {

        long exportId =
                parsePositiveLong(
                        req.getParameter("exportId"),
                        "Mã phiếu xuất không hợp lệ."
                );

        exportRepo.approve(
                exportId,
                userId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Phiếu xuất đã được duyệt và tồn kho đã cập nhật!"
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/export-detail?id="
                        + exportId
        );
    }

    private void handleExportCancel(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long exportId =
                parsePositiveLong(
                        req.getParameter("exportId"),
                        "Mã phiếu xuất không hợp lệ."
                );

        exportRepo.cancel(
                exportId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Phiếu đã bị hủy."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/export-detail?id="
                        + exportId
        );
    }

    private void handleExportPdf(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            String idParam =
                    req.getParameter("id");

            if (idParam == null ||
                    idParam.trim().isEmpty()) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/exports"
                );

                return;
            }

            long id =
                    parsePositiveLong(
                            idParam,
                            "Mã phiếu xuất không hợp lệ."
                    );

            StockExport export =
                    exportRepo.findById(id);

            if (export == null) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/exports"
                );

                return;
            }

            resp.setContentType(
                    "application/pdf"
            );

            resp.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"Export_"
                            + sanitizeFileName(
                            export.getExportCode()
                    )
                            + ".pdf\""
            );

            PdfGenerator.generateExportPdf(
                    export,
                    resp.getOutputStream()
            );

        } catch (Exception e) {

            e.printStackTrace();

            if (!resp.isCommitted()) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/exports"
                );
            }
        }
    }

    private void render(
            HttpServletRequest req,
            HttpServletResponse resp,
            String page,
            String title
    ) throws ServletException, IOException {

        req.setAttribute(
                "cp",
                req.getContextPath()
        );

        req.setAttribute(
                "moduleTitle",
                title
        );

        ViewRouter.admin(
                req,
                resp,
                "warehouse/" + page,
                title,
                "warehouse"
        );
    }

    private int getCurrentUserId(
            HttpServletRequest req
    ) throws Exception {

        Object userObj =
                req.getSession().getAttribute(
                        "user"
                );

        if (userObj instanceof User) {

            return ((User) userObj).getId();
        }

        throw new Exception(
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
        );
    }

    private int parsePositiveInt(
            String value,
            String message
    ) throws Exception {

        try {

            if (value == null ||
                    value.trim().isEmpty()) {

                throw new Exception(message);
            }

            int result =
                    Integer.parseInt(
                            value.trim()
                    );

            if (result <= 0) {

                throw new Exception(message);
            }

            return result;

        } catch (NumberFormatException e) {

            throw new Exception(message);
        }
    }

    private long parsePositiveLong(
            String value,
            String message
    ) throws Exception {

        try {

            if (value == null ||
                    value.trim().isEmpty()) {

                throw new Exception(message);
            }

            long result =
                    Long.parseLong(
                            value.trim()
                    );

            if (result <= 0) {

                throw new Exception(message);
            }

            return result;

        } catch (NumberFormatException e) {

            throw new Exception(message);
        }
    }

    private String optionalString(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

    private String sanitizeFileName(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "document";
        }

        return value.replaceAll(
                "[\\\\/:*?\"<>|]",
                "_"
        );
    }

    private String getErrorMessage(
            Exception e
    ) {

        if (e.getMessage() == null ||
                e.getMessage().trim().isEmpty()) {

            return "Có lỗi xảy ra trong quá trình xử lý.";
        }

        return e.getMessage();
    }
}