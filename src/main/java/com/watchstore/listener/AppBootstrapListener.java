package com.watchstore.listener;

import com.watchstore.repository.BrandRepositoryImpl;
import com.watchstore.repository.CategoryRepositoryImpl;
import com.watchstore.repository.ProductRepositoryImpl;
import com.watchstore.repository.UserRepositoryImpl;
import com.watchstore.repository.RoleRepositoryImpl;
import com.watchstore.repository.VoucherRepositoryImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {


        sce.getServletContext().setAttribute(
                "productRepository",
                new ProductRepositoryImpl()
        );


        sce.getServletContext().setAttribute(
                "brandRepository",
                new BrandRepositoryImpl()
        );


        sce.getServletContext().setAttribute(
                "userRepository",
                new UserRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "roleRepository",
                new RoleRepositoryImpl()
        );
        sce.getServletContext().setAttribute(
                "voucherRepository",
                new VoucherRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "categoryRepository",
                new CategoryRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "bannerRepository",
                new com.watchstore.repository.BannerRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "postRepository",
                new com.watchstore.repository.PostRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "notificationRepository",
                new com.watchstore.repository.NotificationRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "permissionRepository",
                new com.watchstore.repository.PermissionRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "statisticRepository",
                new com.watchstore.repository.StatisticRepositoryImpl()
        );

        sce.getServletContext().setAttribute(
                "orderRepository",
                new com.watchstore.repository.OrderRepository()
        );
        sce.getServletContext().setAttribute(
                "customerRepository",
                new com.watchstore.repository.CustomerRepository()
        );
        sce.getServletContext().setAttribute(
                "warrantyRepository",
                new com.watchstore.repository.WarrantyRepository()
        );
        sce.getServletContext().setAttribute(
                "reviewRepository",
                new com.watchstore.repository.ReviewRepository()
        );

        sce.getServletContext().setAttribute(
                "appName",
                "WatchStore"
        );
    }
}