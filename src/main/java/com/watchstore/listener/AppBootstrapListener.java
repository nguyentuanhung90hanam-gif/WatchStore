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
                "returnRepository",
                new com.watchstore.repository.ReturnRepository()
        );

        sce.getServletContext().setAttribute(
                "appName",
                "WatchStore"
        );

        com.watchstore.repository.OtpRepository otpRepository = new com.watchstore.repository.OtpRepository();
        com.watchstore.service.EmailService emailService = new com.watchstore.service.EmailService();
        sce.getServletContext().setAttribute("otpRepository", otpRepository);
        sce.getServletContext().setAttribute("emailService", emailService);
        sce.getServletContext().setAttribute("otpService", new com.watchstore.service.OtpService(otpRepository, emailService));

        com.watchstore.repository.AddressRepository addressRepository = new com.watchstore.repository.AddressRepository();
        sce.getServletContext().setAttribute("addressRepository", addressRepository);

        com.watchstore.repository.WishlistRepository wishlistRepository = new com.watchstore.repository.WishlistRepository();
        sce.getServletContext().setAttribute("wishlistRepository", wishlistRepository);

        com.watchstore.repository.CartRepository cartRepository = new com.watchstore.repository.CartRepository();
        sce.getServletContext().setAttribute("cartRepository", cartRepository);
    }
}