package com.watchstore.controller.warehouse;

import com.watchstore.model.User;
import com.watchstore.repository.InventoryRepository;
import com.watchstore.repository.VariantRepository;
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
        "/manage/warehouse/alerts",
        "/manage/warehouse/adjust"
})
public class InventoryController extends HttpServlet {

    private InventoryRepository inventoryRepo;
    private VariantRepository variantRepo;

    @Override
    public void init() {
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
    ) throws Exception {

        String keyword=req.getParameter("keyword");
        Integer warehouseId=parseNullablePositiveInt(req.getParameter("warehouseId"));
        req.setAttribute(
                "inventoryItems",
                (keyword!=null && !keyword.isBlank()) || warehouseId!=null
                        ? inventoryRepo.search(keyword, warehouseId)
                        : inventoryRepo.findAll()
        );
        req.setAttribute("warehouses", inventoryRepo.findAllWarehouses());
        req.setAttribute("variants", variantRepo.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path=req.getServletPath();
        if(!"/manage/warehouse/adjust".equals(path)){ resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        try{
            int userId=getCurrentUserId(req);
            int warehouseId=parsePositiveInt(req.getParameter("warehouseId"),"Kho không hợp lệ.");
            int variantId=parsePositiveInt(req.getParameter("variantId"),"Biến thể không hợp lệ.");
            int quantityChange=parseInt(req.getParameter("quantityChange"),"Số lượng điều chỉnh không hợp lệ.");
            String note=req.getParameter("note");
            inventoryRepo.adjustStock(warehouseId,variantId,quantityChange,note,userId);
            req.getSession().setAttribute("successMsg","Điều chỉnh tồn kho thành công.");
        }catch(Exception e){ req.getSession().setAttribute("errorMsg",getErrorMessage(e)); }
        resp.sendRedirect(req.getContextPath()+"/manage/warehouse/inventory");
    }

    private void handleTransactions(
            HttpServletRequest req
    ) throws Exception {

        req.setAttribute(
                "transactions",
                inventoryRepo.findAllTransactions()
        );
    }

    private void handleAlerts(
            HttpServletRequest req
    ) throws Exception {

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

    private int getCurrentUserId(HttpServletRequest req) throws Exception {
        Object obj=req.getSession().getAttribute("user");
        if(!(obj instanceof User)) throw new Exception("Phiên đăng nhập đã hết. Vui lòng đăng nhập lại.");
        return ((User)obj).getId();
    }
    private int parsePositiveInt(String value,String message) throws Exception { int n=parseInt(value,message); if(n<=0)throw new Exception(message); return n; }
    private int parseInt(String value,String message) throws Exception { try{return Integer.parseInt(value);}catch(Exception e){throw new Exception(message);}}
    private Integer parseNullablePositiveInt(String value){ try{ if(value==null||value.isBlank())return null; int n=Integer.parseInt(value); return n>0?n:null;}catch(Exception e){return null;} }

    private String getErrorMessage(
            Exception e
    ) {

        if (e.getMessage() == null
                || e.getMessage().trim().isEmpty()) {

            return "Có lỗi xảy ra trong quá trình xử lý.";
        }

        return e.getMessage();
    }
}