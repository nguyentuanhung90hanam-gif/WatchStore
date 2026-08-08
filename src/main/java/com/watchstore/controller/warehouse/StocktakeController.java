package com.watchstore.controller.warehouse;

import com.watchstore.model.Stocktake;
import com.watchstore.model.StocktakeItem;
import com.watchstore.model.User;
import com.watchstore.repository.InventoryRepository;
import com.watchstore.repository.StocktakeRepository;
import com.watchstore.repository.VariantRepository;
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
        "/manage/warehouse/stocktake",
        "/manage/warehouse/stocktake-create",
        "/manage/warehouse/stocktake-detail",
        "/manage/warehouse/stocktake-add-item",
        "/manage/warehouse/stocktake-update-item",
        "/manage/warehouse/stocktake-delete-item",
        "/manage/warehouse/stocktake-submit",
        "/manage/warehouse/stocktake-approve",
        "/manage/warehouse/stocktake-cancel"
})
public class StocktakeController extends HttpServlet {

    private StocktakeRepository stocktakeRepo;
    private InventoryRepository inventoryRepo;
    private VariantRepository variantRepo;

    @Override
    public void init() {
        stocktakeRepo = new StocktakeRepository();
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

                case "/manage/warehouse/stocktake":

                    handleStocktakeList(req);

                    render(
                            req,
                            resp,
                            "stocktake",
                            "Kiểm kê kho"
                    );

                    return;

                case "/manage/warehouse/stocktake-create":

                    handleStocktakeCreateForm(req);

                    render(
                            req,
                            resp,
                            "stocktake-create",
                            "Tạo phiếu kiểm kê"
                    );

                    return;

                case "/manage/warehouse/stocktake-detail":

                    handleStocktakeDetail(req);

                    render(
                            req,
                            resp,
                            "stocktake-detail",
                            "Chi tiết phiếu kiểm kê"
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
                            + "/manage/warehouse/stocktake"
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

                case "/manage/warehouse/stocktake-create":

                    handleStocktakeCreate(
                            req,
                            userId,
                            resp
                    );

                    return;

                case "/manage/warehouse/stocktake-add-item":

                    handleStocktakeAddItem(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/stocktake-update-item":

                    handleStocktakeUpdateItem(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/stocktake-delete-item":

                    handleStocktakeDeleteItem(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/stocktake-submit":

                    handleStocktakeSubmit(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/stocktake-approve":

                    handleStocktakeApprove(
                            req,
                            userId,
                            resp
                    );

                    return;

                case "/manage/warehouse/stocktake-cancel":

                    handleStocktakeCancel(
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

            String stocktakeId =
                    req.getParameter("stocktakeId");

            if (stocktakeId != null &&
                    !stocktakeId.trim().isEmpty()) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/stocktake-detail?id="
                                + stocktakeId
                );

            } else {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/stocktake"
                );
            }
        }
    }

    private void handleStocktakeList(
            HttpServletRequest req
    ) {

        req.setAttribute(
                "stocktakes",
                stocktakeRepo.findAll()
        );
    }

    private void handleStocktakeCreateForm(
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

    private void handleStocktakeDetail(
            HttpServletRequest req
    ) throws Exception {

        String idParam =
                req.getParameter("id");

        long id =
                parsePositiveLong(
                        idParam,
                        "Mã phiếu kiểm kê không hợp lệ."
                );

        Stocktake stocktake =
                stocktakeRepo.findById(id);

        if (stocktake == null) {

            throw new Exception(
                    "Không tìm thấy phiếu kiểm kê."
            );
        }

        req.setAttribute(
                "stocktake",
                stocktake
        );

        req.setAttribute(
                "variants",
                variantRepo.findAll()
        );
    }

    private void handleStocktakeCreate(
            HttpServletRequest req,
            int userId,
            HttpServletResponse resp
    ) throws Exception {

        int warehouseId =
                parsePositiveInt(
                        req.getParameter("warehouseId"),
                        "Kho không hợp lệ."
                );

        String note =
                optionalString(
                        req.getParameter("note")
                );

        String[] variantIds =
                req.getParameterValues(
                        "variantIds"
                );

        String[] actualQuantities =
                req.getParameterValues(
                        "actualQuantities"
                );

        if (variantIds == null ||
                variantIds.length == 0) {

            throw new Exception(
                    "Phiếu kiểm kê phải có ít nhất một sản phẩm."
            );
        }

        if (actualQuantities == null ||
                actualQuantities.length == 0) {

            throw new Exception(
                    "Phiếu kiểm kê phải có số lượng thực tế."
            );
        }

        Stocktake stocktake =
                new Stocktake();

        stocktake.setStocktakeCode(
                "STK-" + System.currentTimeMillis()
        );

        stocktake.setWarehouseId(
                warehouseId
        );

        stocktake.setNote(
                note
        );

        stocktake.setCreatedBy(
                userId
        );

        List<StocktakeItem> items =
                new ArrayList<>();

        for (int i = 0;
             i < variantIds.length;
             i++) {

            if (variantIds[i] == null ||
                    variantIds[i].trim().isEmpty()) {

                continue;
            }

            if (i >= actualQuantities.length) {

                throw new Exception(
                        "Thiếu số lượng kiểm kê."
                );
            }

            int variantId =
                    parsePositiveInt(
                            variantIds[i],
                            "Biến thể không hợp lệ."
                    );

            int actualQuantity =
                    parsePositiveOrZeroInt(
                            actualQuantities[i],
                            "Số lượng kiểm kê không hợp lệ."
                    );

            StocktakeItem item =
                    new StocktakeItem();

            item.setVariantId(
                    variantId
            );

            item.setActualQuantity(
                    actualQuantity
            );

            items.add(item);
        }

        if (items.isEmpty()) {

            throw new Exception(
                    "Phiếu kiểm kê phải có ít nhất một sản phẩm hợp lệ."
            );
        }

        stocktake.setItems(
                items
        );

        long stocktakeId =
                stocktakeRepo.createDraft(
                        stocktake
                );

        req.getSession().setAttribute(
                "successMsg",
                "Tạo phiếu kiểm kê nháp thành công!"
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/stocktake-detail?id="
                        + stocktakeId
        );
    }

    private void handleStocktakeAddItem(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long stocktakeId =
                parsePositiveLong(
                        req.getParameter("stocktakeId"),
                        "Mã phiếu kiểm kê không hợp lệ."
                );

        int variantId =
                parsePositiveInt(
                        req.getParameter("variantId"),
                        "Phải chọn biến thể."
                );

        int actualQuantity =
                parsePositiveOrZeroInt(
                        req.getParameter("actualQuantity"),
                        "Số lượng kiểm kê không hợp lệ."
                );

        StocktakeItem item =
                new StocktakeItem();

        item.setVariantId(
                variantId
        );

        item.setActualQuantity(
                actualQuantity
        );

        stocktakeRepo.addItem(
                stocktakeId,
                item
        );

        req.getSession().setAttribute(
                "successMsg",
                "Thêm sản phẩm thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/stocktake-detail?id="
                        + stocktakeId
        );
    }

    private void handleStocktakeUpdateItem(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long stocktakeId =
                parsePositiveLong(
                        req.getParameter("stocktakeId"),
                        "Mã phiếu kiểm kê không hợp lệ."
                );

        long itemId =
                parsePositiveLong(
                        req.getParameter("itemId"),
                        "Mã sản phẩm trong phiếu không hợp lệ."
                );

        int actualQuantity =
                parsePositiveOrZeroInt(
                        req.getParameter("actualQuantity"),
                        "Số lượng kiểm kê không hợp lệ."
                );

        stocktakeRepo.updateItem(
                itemId,
                actualQuantity
        );

        req.getSession().setAttribute(
                "successMsg",
                "Cập nhật số lượng kiểm kê thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/stocktake-detail?id="
                        + stocktakeId
        );
    }

    private void handleStocktakeDeleteItem(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long stocktakeId =
                parsePositiveLong(
                        req.getParameter("stocktakeId"),
                        "Mã phiếu kiểm kê không hợp lệ."
                );

        long itemId =
                parsePositiveLong(
                        req.getParameter("itemId"),
                        "Mã sản phẩm trong phiếu không hợp lệ."
                );

        stocktakeRepo.deleteItem(
                itemId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Đã xóa sản phẩm khỏi phiếu."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/stocktake-detail?id="
                        + stocktakeId
        );
    }

    private void handleStocktakeSubmit(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long stocktakeId =
                parsePositiveLong(
                        req.getParameter("stocktakeId"),
                        "Mã phiếu kiểm kê không hợp lệ."
                );

        stocktakeRepo.submitForApproval(
                stocktakeId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Đã gửi phiếu kiểm kê chờ duyệt."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/stocktake-detail?id="
                        + stocktakeId
        );
    }

    private void handleStocktakeApprove(
            HttpServletRequest req,
            int userId,
            HttpServletResponse resp
    ) throws Exception {

        long stocktakeId =
                parsePositiveLong(
                        req.getParameter("stocktakeId"),
                        "Mã phiếu kiểm kê không hợp lệ."
                );

        stocktakeRepo.approve(
                stocktakeId,
                userId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Phiếu kiểm kê đã hoàn tất và tồn kho đã được điều chỉnh!"
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/stocktake-detail?id="
                        + stocktakeId
        );
    }

    private void handleStocktakeCancel(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        long stocktakeId =
                parsePositiveLong(
                        req.getParameter("stocktakeId"),
                        "Mã phiếu kiểm kê không hợp lệ."
                );

        stocktakeRepo.cancel(
                stocktakeId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Phiếu kiểm kê đã bị hủy."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/stocktake-detail?id="
                        + stocktakeId
        );
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

    private int parsePositiveOrZeroInt(
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

            if (result < 0) {

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