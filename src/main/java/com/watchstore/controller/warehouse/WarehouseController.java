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
        "/manage/warehouse",
        "/manage/warehouse/dashboard"
})
public class WarehouseController extends HttpServlet {

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

        try {

            req.setAttribute(
                    "totalQuantity",
                    inventoryRepo.getTotalQuantityOnHand()
            );

            req.setAttribute(
                    "lowStockCount",
                    inventoryRepo.getLowStockAlertCount()
            );

            req.setAttribute(
                    "cp",
                    req.getContextPath()
            );

            req.setAttribute(
                    "moduleTitle",
                    "Tổng quan kho"
            );

            ViewRouter.admin(
                    req,
                    resp,
                    "warehouse/dashboard",
                    "Tổng quan kho",
                    "warehouse"
            );

        } catch (Exception e) {

            e.printStackTrace();

            req.getSession().setAttribute(
                    "errorMsg",
                    getErrorMessage(e)
            );

            resp.sendRedirect(
                    req.getContextPath()
                            + "/manage/warehouse"
            );
        }
    }

    private String getErrorMessage(
            Exception e
    ) {

        if (e.getMessage() == null ||
                e.getMessage().trim().isEmpty()) {

            return "Có lỗi xảy ra khi tải tổng quan kho.";
        }

        return e.getMessage();
    }
}