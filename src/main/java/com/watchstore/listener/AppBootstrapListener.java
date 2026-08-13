package com.watchstore.listener;

import com.watchstore.repository.BrandRepositoryImpl;
import com.watchstore.repository.AddressRepository;
import com.watchstore.repository.CartRepository;
import com.watchstore.repository.MockProductRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.SqlProductRepository;
import com.watchstore.repository.UserRepository;
import com.watchstore.repository.WishlistRepository;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        MockProductRepository mockProducts = new MockProductRepository();
        sce.getServletContext().setAttribute(
                "productRepository",
                new SqlProductRepository(mockProducts)
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
                "orderRepository",
                new OrderRepository()
        );

        sce.getServletContext().setAttribute(
                "cartRepository",
                new CartRepository()
        );

        sce.getServletContext().setAttribute(
                "addressRepository",
                new AddressRepository()
        );

        sce.getServletContext().setAttribute(
                "wishlistRepository",
                new WishlistRepository()
        );

        com.watchstore.repository.OtpRepository otpRepository = new com.watchstore.repository.OtpRepository();
        com.watchstore.service.EmailService emailService = new com.watchstore.service.EmailService();
        com.watchstore.service.OtpService otpService = new com.watchstore.service.OtpService(otpRepository, emailService);

        sce.getServletContext().setAttribute("otpRepository", otpRepository);
        sce.getServletContext().setAttribute("emailService", emailService);
        sce.getServletContext().setAttribute("otpService", otpService);

        sce.getServletContext().setAttribute(
                "appName",
                "WatchStore"
        );
    }
}
