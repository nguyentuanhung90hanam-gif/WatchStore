package com.watchstore.controller.customer;

import com.watchstore.model.User;
import com.watchstore.repository.CartRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/cart/*")
public class CartController extends HttpServlet {
    private CartRepository cart; private com.watchstore.repository.AddressRepository addresses;
    @Override public void init(){cart=(CartRepository)getServletContext().getAttribute("cartRepository");addresses=(com.watchstore.repository.AddressRepository)getServletContext().getAttribute("addressRepository");}

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=(User)req.getSession().getAttribute("user");
        if(u==null){resp.sendRedirect(req.getContextPath()+"/auth/login?required=1");return;}
        String path=req.getPathInfo()==null?"/view":req.getPathInfo();
        try{prepare(req,u.getId()); if("/checkout".equals(path)){ req.setAttribute("addresses",addresses.findAll(u.getId())); ViewRouter.customer(req,resp,"customer/checkout","Thanh toán"); } else ViewRouter.customer(req,resp,"customer/cart","Giỏ hàng");}
        catch(SQLException e){throw new ServletException("Không thể đọc giỏ hàng.",e);}
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User u=(User)req.getSession().getAttribute("user");
        if(u==null){resp.sendRedirect(req.getContextPath()+"/auth/login?required=1");return;}
        String path=req.getPathInfo()==null?"/add":req.getPathInfo();
        int variant=parse(req.getParameter("variantId"),0), product=parse(req.getParameter("id"),0), qty=parse(req.getParameter("quantity"),1);
        try{
            if("/add".equals(path)) {
                if(product<=0 || qty<1 || qty>99) throw new IllegalArgumentException("Số lượng sản phẩm phải từ 1 đến 99.");
                cart.addProduct(u.getId(),product,qty);
            } else if("/update".equals(path)) {
                if(variant<=0 && product<=0) throw new IllegalArgumentException("Sản phẩm trong giỏ không hợp lệ.");
                if(qty<0 || qty>99) throw new IllegalArgumentException("Số lượng phải từ 0 đến 99.");
                cart.update(u.getId(),variant>0?variant:product,Math.max(0,qty));
            } else if("/remove".equals(path)) {
                if(variant<=0 && product<=0) throw new IllegalArgumentException("Sản phẩm trong giỏ không hợp lệ.");
                cart.remove(u.getId(),variant>0?variant:product);
            }
            req.getSession().setAttribute("flash","Đã cập nhật giỏ hàng.");
        }catch(Exception e){req.getSession().setAttribute("flash",root(e));}
        resp.sendRedirect(req.getContextPath()+"/cart/view");
    }

    private void prepare(HttpServletRequest req,int userId)throws SQLException{
        List<Map<String,Object>> items=cart.items(userId);BigDecimal subtotal=cart.subtotal(userId);
        BigDecimal shipping=subtotal.signum()>0&&subtotal.compareTo(new BigDecimal("1000000"))<0?new BigDecimal("30000"):BigDecimal.ZERO;
        req.setAttribute("cartItems",items);req.setAttribute("subtotal",subtotal);req.setAttribute("discount",BigDecimal.ZERO);req.setAttribute("shipping",shipping);req.setAttribute("cartCount",cart.count(userId));
    }
    private int parse(String s,int d){try{return Integer.parseInt(s);}catch(Exception e){return d;}}
    private String root(Exception e){Throwable t=e;while(t.getCause()!=null)t=t.getCause();return t.getMessage()==null?"Thao tác thất bại.":t.getMessage();}
}
