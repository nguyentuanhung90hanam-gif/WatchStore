package com.watchstore.controller.warehouse;

import com.watchstore.model.StockReceipt;
import com.watchstore.model.StockReceiptItem;
import com.watchstore.model.User;
import com.watchstore.repository.InventoryRepository;
import com.watchstore.repository.StockReceiptRepository;
import com.watchstore.repository.VariantRepository;
import com.watchstore.util.PdfGenerator;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {
        "/manage/warehouse/receipts",
        "/manage/warehouse/receipt-create",
        "/manage/warehouse/receipt-detail",
        "/manage/warehouse/receipt-pdf",
        "/manage/warehouse/receipt-add-item",
        "/manage/warehouse/receipt-update-item",
        "/manage/warehouse/receipt-delete-item",
        "/manage/warehouse/receipt-submit",
        "/manage/warehouse/receipt-approve",
        "/manage/warehouse/receipt-cancel"
})
public class StockReceiptController extends HttpServlet {

    private StockReceiptRepository receiptRepo;
    private InventoryRepository inventoryRepo;
    private VariantRepository variantRepo;

    @Override
    public void init() {
        receiptRepo = new StockReceiptRepository();
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

                case "/manage/warehouse/receipts":
                    handleReceiptList(req);
                    render(
                            req,
                            resp,
                            "receipt-list",
                            "Phiếu nhập kho"
                    );
                    return;

                case "/manage/warehouse/receipt-create":
                    handleReceiptCreateForm(req);
                    render(
                            req,
                            resp,
                            "receipt-create",
                            "Tạo phiếu nhập"
                    );
                    return;

                case "/manage/warehouse/receipt-detail":
                    handleReceiptDetail(req);
                    render(
                            req,
                            resp,
                            "receipt-detail",
                            "Chi tiết phiếu nhập"
                    );
                    return;

                case "/manage/warehouse/receipt-pdf":
                    handleReceiptPdf(
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
                            + "/manage/warehouse/receipts"
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
            // Đặt int userId bên trong try-catch và chỉ lấy khi cần
            int userId;

            switch (path) {

                case "/manage/warehouse/receipt-create":
                    userId = getCurrentUserId(req);
                    handleReceiptCreate(
                            req,
                            userId,
                            resp
                    );
                    return;

                case "/manage/warehouse/receipt-add-item":
                    userId = getCurrentUserId(req);
                    handleReceiptAddItem(
                            req,
                            userId,
                            resp
                    );
                    return;

                case "/manage/warehouse/receipt-update-item":
                    handleReceiptUpdateItem(
                            req,
                            resp
                    );
                    return;

                case "/manage/warehouse/receipt-delete-item":
                    handleReceiptDeleteItem(
                            req,
                            resp
                    );
                    return;

                case "/manage/warehouse/receipt-submit":
                    handleReceiptSubmit(
                            req,
                            resp
                    );
                    return;

                case "/manage/warehouse/receipt-approve":
                    userId = getCurrentUserId(req);
                    handleReceiptApprove(
                            req,
                            userId,
                            resp
                    );
                    return;

                case "/manage/warehouse/receipt-cancel":
                    handleReceiptCancel(
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

            String receiptId = req.getParameter("receiptId");

            if (receiptId != null && !receiptId.trim().isEmpty()) {
                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/receipt-detail?id="
                                + receiptId
                );
            } else {
                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/receipts"
                );
            }
        }
    }

    private void handleReceiptList(
            HttpServletRequest req
    ) {

        req.setAttribute(
                "receipts",
                receiptRepo.findAll()
        );
    }

    private void handleReceiptCreateForm(
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

    private void handleReceiptDetail(
            HttpServletRequest req
    ) throws Exception {

        String idParam =
                req.getParameter("id");

        if (idParam == null ||
                idParam.trim().isEmpty()) {

            throw new Exception(
                    "Thiếu mã phiếu nhập."
            );
        }

        long id;

        try {

            id = Long.parseLong(
                    idParam.trim()
            );

        } catch (NumberFormatException e) {

            throw new Exception(
                    "Mã phiếu nhập không hợp lệ."
            );
        }

        if (id <= 0) {

            throw new Exception(
                    "Mã phiếu nhập không hợp lệ."
            );
        }

        StockReceipt receipt =
                receiptRepo.findById(id);

        if (receipt == null) {

            throw new Exception(
                    "Không tìm thấy phiếu nhập."
            );
        }

        req.setAttribute(
                "receipt",
                receipt
        );

        req.setAttribute(
                "variants",
                variantRepo.findAll()
        );
    }

    private void handleReceiptCreate(
            HttpServletRequest req,
            int userId,
            HttpServletResponse resp
    ) throws Exception {

        int warehouseId =
                parsePositiveInt(
                        req.getParameter("warehouseId"),
                        "Kho không hợp lệ."
                );

        String supplierName =
                req.getParameter("supplierName");

        String supplierPhone =
                req.getParameter("supplierPhone");

        String note =
                req.getParameter("note");

        String[] variantIds =
                req.getParameterValues("variantIds");

        String[] quantities =
                req.getParameterValues("quantities");

        String[] unitCosts =
                req.getParameterValues("unitCosts");

        if (variantIds == null ||
                variantIds.length == 0) {

            throw new Exception(
                    "Phiếu nhập phải có ít nhất một sản phẩm."
            );
        }

        StockReceipt receipt =
                new StockReceipt();

        receipt.setReceiptCode(
                "REC-" + System.currentTimeMillis()
        );

        receipt.setWarehouseId(
                warehouseId
        );

        receipt.setSupplierName(
                supplierName
        );

        receipt.setSupplierPhone(
                supplierPhone
        );

        receipt.setNote(
                note
        );

        receipt.setCreatedBy(
                userId
        );

        BigDecimal totalCost =
                BigDecimal.ZERO;

        List<StockReceiptItem> items =
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

            if (unitCosts == null ||
                    i >= unitCosts.length) {

                throw new Exception(
                        "Thiếu giá nhập sản phẩm."
                );
            }

            int quantity =
                    parsePositiveInt(
                            quantities[i],
                            "Số lượng nhập phải lớn hơn 0."
                    );

            BigDecimal unitCost =
                    parseMoney(
                            unitCosts[i],
                            "Giá nhập không hợp lệ."
                    );

            StockReceiptItem item =
                    new StockReceiptItem();

            item.setVariantId(
                    parsePositiveInt(
                            variantIds[i],
                            "Biến thể không hợp lệ."
                    )
            );

            item.setQuantity(
                    quantity
            );

            item.setUnitCost(
                    unitCost
            );

            totalCost =
                    totalCost.add(
                            unitCost.multiply(
                                    BigDecimal.valueOf(
                                            quantity
                                    )
                            )
                    );

            items.add(item);
        }

        if (items.isEmpty()) {

            throw new Exception(
                    "Phiếu nhập phải có ít nhất một sản phẩm hợp lệ."
            );
        }

        receipt.setTotalCost(
                totalCost
        );

        receipt.setItems(
                items
        );

        long receiptId =
                receiptRepo.createDraft(
                        receipt
                );

        req.getSession().setAttribute(
                "successMsg",
                "Tạo phiếu nhập nháp thành công!"
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/receipt-detail?id="
                        + receiptId
        );
    }

    private void handleReceiptAddItem(
            HttpServletRequest req,
            int userId,
            HttpServletResponse resp
    ) throws Exception {

        long receiptId =
                parsePositiveLong(
                        req.getParameter("receiptId"),
                        "Mã phiếu nhập không hợp lệ."
                );

        StockReceiptItem item =
                new StockReceiptItem();

        item.setVariantId(
                parsePositiveInt(
                        req.getParameter("variantId"),
                        "Phải chọn biến thể."
                )
        );

        item.setQuantity(
                parsePositiveInt(
                        req.getParameter("quantity"),
                        "Số lượng phải lớn hơn 0."
                )
        );

        item.setUnitCost(
                parseMoney(
                        req.getParameter("unitCost"),
                        "Giá nhập không hợp lệ."
                )
        );

        receiptRepo.addItem(
                receiptId,
                item
        );

        req.getSession().setAttribute(
                "successMsg",
                "Thêm sản phẩm thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/receipt-detail?id="
                        + receiptId
        );
    }

    private void handleReceiptUpdateItem(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long itemId =
                parsePositiveLong(
                        req.getParameter("itemId"),
                        "Mã sản phẩm trong phiếu không hợp lệ."
                );

        long receiptId =
                parsePositiveLong(
                        req.getParameter("receiptId"),
                        "Mã phiếu nhập không hợp lệ."
                );

        int quantity =
                parsePositiveInt(
                        req.getParameter("quantity"),
                        "Số lượng phải lớn hơn 0."
                );

        BigDecimal unitCost =
                parseMoney(
                        req.getParameter("unitCost"),
                        "Giá nhập không hợp lệ."
                );

        receiptRepo.updateItem(
                itemId,
                quantity,
                unitCost
        );

        req.getSession().setAttribute(
                "successMsg",
                "Cập nhật thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/receipt-detail?id="
                        + receiptId
        );
    }

    private void handleReceiptDeleteItem(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long receiptId =
                parsePositiveLong(
                        req.getParameter("receiptId"),
                        "Mã phiếu nhập không hợp lệ."
                );

        long itemId =
                parsePositiveLong(
                        req.getParameter("itemId"),
                        "Mã sản phẩm trong phiếu không hợp lệ."
                );

        receiptRepo.deleteItem(
                itemId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Đã xóa sản phẩm."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/receipt-detail?id="
                        + receiptId
        );
    }

    private void handleReceiptSubmit(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long receiptId =
                parsePositiveLong(
                        req.getParameter("receiptId"),
                        "Mã phiếu nhập không hợp lệ."
                );

        receiptRepo.submitForApproval(
                receiptId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Đã gửi phiếu chờ duyệt."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/receipt-detail?id="
                        + receiptId
        );
    }

    private void handleReceiptApprove(
            HttpServletRequest req,
            int userId,
            HttpServletResponse resp
    ) throws Exception {

        long receiptId =
                parsePositiveLong(
                        req.getParameter("receiptId"),
                        "Mã phiếu nhập không hợp lệ."
                );

        receiptRepo.approve(
                receiptId,
                userId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Phiếu nhập đã được duyệt và tồn kho đã cập nhật!"
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/receipt-detail?id="
                        + receiptId
        );
    }

    private void handleReceiptCancel(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long receiptId =
                parsePositiveLong(
                        req.getParameter("receiptId"),
                        "Mã phiếu nhập không hợp lệ."
                );

        receiptRepo.cancel(
                receiptId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Phiếu đã bị hủy."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/receipt-detail?id="
                        + receiptId
        );
    }

    private void handleReceiptPdf(
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
                                + "/manage/warehouse/receipts"
                );

                return;
            }

            long id =
                    Long.parseLong(
                            idParam.trim()
                    );

            if (id <= 0) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/receipts"
                );

                return;
            }

            StockReceipt receipt =
                    receiptRepo.findById(id);

            if (receipt == null) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/receipts"
                );

                return;
            }

            resp.setContentType(
                    "application/pdf"
            );

            resp.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"Receipt_"
                            + sanitizeFileName(
                            receipt.getReceiptCode()
                    )
                            + ".pdf\""
            );

            PdfGenerator.generateReceiptPdf(
                    receipt,
                    resp.getOutputStream()
            );

        } catch (Exception e) {

            e.printStackTrace();

            if (!resp.isCommitted()) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/receipts"
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
                req.getSession().getAttribute("user");

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

    private BigDecimal parseMoney(
            String value,
            String message
    ) throws Exception {

        try {

            if (value == null ||
                    value.trim().isEmpty()) {

                throw new Exception(message);
            }

            BigDecimal result =
                    new BigDecimal(
                            value.trim()
                    );

            if (result.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                throw new Exception(message);
            }

            return result;

        } catch (NumberFormatException e) {

            throw new Exception(message);
        }
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