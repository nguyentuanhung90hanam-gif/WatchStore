package com.watchstore.controller.sales;

import com.watchstore.repository.MockDataStore;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/manage/sales/*")
public class SalesController extends HttpServlet {
    private static final Map<String, String[]> PAGES = Map.ofEntries(
        Map.entry("/dashboard", new String[]{"dashboard", "Tổng quan bán hàng"}),
        Map.entry("/orders", new String[]{"order-list", "Quản lý đơn hàng"}),
        Map.entry("/order-detail", new String[]{"order-detail", "Chi tiết đơn hàng"}),
        Map.entry("/customers", new String[]{"customer-list", "Danh sách khách hàng"}),
        Map.entry("/customer-detail", new String[]{"customer-detail", "Chi tiết khách hàng"}),
        Map.entry("/reviews", new String[]{"review", "Kiểm duyệt đánh giá"}),
        Map.entry("/comments", new String[]{"comment", "Bình luận"}),
        Map.entry("/delivery", new String[]{"delivery", "Vận chuyển"}),
        Map.entry("/returns", new String[]{"return", "Yêu cầu đổi trả"}),
        Map.entry("/report", new String[]{"report", "Báo cáo bán hàng"})
    );
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/dashboard" : req.getPathInfo();
        String[] page = PAGES.getOrDefault(path, PAGES.get("/dashboard"));
        req.setAttribute("orders", MockDataStore.orders());
        req.setAttribute("moduleTitle", page[1]);
        ViewRouter.admin(req, resp, "sales/" + page[0], page[1], "sales");
    }
}
