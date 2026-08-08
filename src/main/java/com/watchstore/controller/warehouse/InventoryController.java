package com.watchstore.controller.warehouse;

import com.watchstore.repository.InventoryRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {
        "/manage/warehouse/inventory",
        "/manage/warehouse/transactions",
        "/manage/warehouse/alerts"
})
public class InventoryController extends HttpServlet {

    private InventoryRepository inventoryRepo;

    @Override
    public void init() {
        inventoryRepo = new InventoryRepository();
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getServletPath();

        try {

            switch (path) {

                case "/manage/warehouse/inventory":

                    handleInventory(req);

                    render(
                            req,
                            resp,
                            "inventory",
                            "Tồn kho"
                    );

                    return;

                case "/manage/warehouse/transactions":

                    handleTransactions(req);

                    render(
                            req,
                            resp,
                            "transaction-list",
                            "Lịch sử nhập xuất"
                    );

                    return;

                case "/manage/warehouse/alerts":

                    handleAlerts(req);

                    render(
                            req,
                            resp,
                            "stock-alert",
                            "Cảnh báo tồn kho"
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
                            + "/manage/warehouse/inventory"
            );
        }
    }

    private void handleInventory(
            HttpServletRequest req
    ) {

        req.setAttribute(
                "inventoryItems",
                inventoryRepo.findAll()
        );
    }

    private void handleTransactions(
            HttpServletRequest req
    ) {

        req.setAttribute(
                "transactions",
                inventoryRepo.findAllTransactions()
        );
    }

    private void handleAlerts(
            HttpServletRequest req
    ) {

        req.setAttribute(
                "lowStockItems",
                inventoryRepo.findLowStock()
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

