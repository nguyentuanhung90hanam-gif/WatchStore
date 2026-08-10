package com.watchstore.listener;

import com.watchstore.repository.BrandRepositoryImpl;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.UserRepository;
import com.watchstore.repository.CustomerRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.WarrantyRepository;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        sce.getServletContext().setAttribute(
                "productRepository",
                new ProductRepository()
        );

        sce.getServletContext().setAttribute(
                "brandRepository",
                new BrandRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "userRepository",
                new UserRepository()
        );

        sce.getServletContext().setAttribute(
                "customerRepository",
                new CustomerRepository()
        );

        sce.getServletContext().setAttribute(
                "orderRepository",
                new OrderRepository()
        );

        WarrantyRepository warrantyRepository = new WarrantyRepository();
        warrantyRepository.ensureTable(); // tự tạo bảng Warranties nếu chưa có
        sce.getServletContext().setAttribute("warrantyRepository", warrantyRepository);

        sce.getServletContext().setAttribute(
                "appName",
                "WatchStore"
        );
    }
}