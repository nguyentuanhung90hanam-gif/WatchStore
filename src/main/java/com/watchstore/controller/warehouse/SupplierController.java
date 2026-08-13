package com.watchstore.controller.warehouse;

import com.watchstore.model.User;
import com.watchstore.repository.StockReceiptRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/manage/warehouse/suppliers")
public class SupplierController extends HttpServlet {
    private StockReceiptRepository stockReceiptRepository;

    @Override
    public void init() throws ServletException {
        stockReceiptRepository = new StockReceiptRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null || !user.hasPermission("SUPPLIERS_VIEW")) {
            req.getSession().setAttribute("flash", "Bạn không có quyền truy cập Nhà cung cấp.");
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }

        req.setAttribute("suppliers", stockReceiptRepository.getSuppliers());
        req.setAttribute("moduleTitle", "Nhà cung cấp");
        req.setAttribute("moduleKicker", "QUẢN LÝ KHO");
        req.setAttribute("moduleDescription", "Quản lý dữ liệu đối tác nhà cung cấp");

        ViewRouter.admin(req, resp, "warehouse/suppliers", "Nhà cung cấp", "warehouse");
    }
}
