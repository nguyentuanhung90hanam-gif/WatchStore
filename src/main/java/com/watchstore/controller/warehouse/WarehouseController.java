package com.watchstore.controller.warehouse;

import com.watchstore.repository.ProductRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/manage/warehouse/*")
public class WarehouseController extends HttpServlet {
    private ProductRepository products;
    private static final Map<String, String[]> PAGES = Map.ofEntries(
        Map.entry("/dashboard", new String[]{"dashboard", "Tổng quan kho"}),
        Map.entry("/receipts", new String[]{"receipt-list", "Phiếu nhập kho"}),
        Map.entry("/receipt-create", new String[]{"receipt-create", "Tạo phiếu nhập"}),
        Map.entry("/exports", new String[]{"export-list", "Phiếu xuất kho"}),
        Map.entry("/export-create", new String[]{"export-create", "Tạo phiếu xuất"}),
        Map.entry("/inventory", new String[]{"inventory", "Tồn kho"}),
        Map.entry("/stocktake", new String[]{"stocktake", "Kiểm kê"}),
        Map.entry("/variants", new String[]{"variant", "Biến thể sản phẩm"}),
        Map.entry("/alerts", new String[]{"stock-alert", "Cảnh báo tồn kho"})
    );
    @Override public void init() { products = (ProductRepository) getServletContext().getAttribute("productRepository"); }
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/dashboard" : req.getPathInfo();
        String[] page = PAGES.getOrDefault(path, PAGES.get("/dashboard"));
        req.setAttribute("products", products.findAll());
        req.setAttribute("moduleTitle", page[1]);
        ViewRouter.admin(req, resp, "warehouse/" + page[0], page[1], "warehouse");
    }
}
