package com.watchstore.listener;

import com.watchstore.repository.AddressRepository;
import com.watchstore.repository.BannerRepositoryImpl;
import com.watchstore.repository.BrandRepositoryImpl;
import com.watchstore.repository.CartRepository;
import com.watchstore.repository.CategoryRepositoryImpl;
import com.watchstore.repository.CommentRepository;
import com.watchstore.repository.CommentRepositoryImpl;
import com.watchstore.repository.CustomerRepository;
import com.watchstore.repository.NotificationRepositoryImpl;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.OtpRepository;
import com.watchstore.repository.PermissionRepositoryImpl;
import com.watchstore.repository.PostRepositoryImpl;
import com.watchstore.repository.ProductRepositoryImpl;
import com.watchstore.repository.ReviewRepository;
import com.watchstore.repository.ReviewRepositoryImpl;
import com.watchstore.repository.RoleRepositoryImpl;
import com.watchstore.repository.StatisticRepositoryImpl;
import com.watchstore.repository.UserRepositoryImpl;
import com.watchstore.repository.VoucherRepositoryImpl;
import com.watchstore.repository.WarrantyRepository;
import com.watchstore.repository.WishlistRepository;
import com.watchstore.service.EmailService;
import com.watchstore.service.OtpService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute("productRepository", new ProductRepositoryImpl());
        sce.getServletContext().setAttribute("brandRepository", new BrandRepositoryImpl());
        sce.getServletContext().setAttribute("userRepository", new UserRepositoryImpl());
        sce.getServletContext().setAttribute("roleRepository", new RoleRepositoryImpl());
        sce.getServletContext().setAttribute("voucherRepository", new VoucherRepositoryImpl());
        sce.getServletContext().setAttribute("categoryRepository", new CategoryRepositoryImpl());
        sce.getServletContext().setAttribute("bannerRepository", new BannerRepositoryImpl());
        sce.getServletContext().setAttribute("postRepository", new PostRepositoryImpl());
        sce.getServletContext().setAttribute("notificationRepository", new NotificationRepositoryImpl());
        sce.getServletContext().setAttribute("permissionRepository", new PermissionRepositoryImpl());
        sce.getServletContext().setAttribute("statisticRepository", new StatisticRepositoryImpl());
        sce.getServletContext().setAttribute("orderRepository", new OrderRepository());
        sce.getServletContext().setAttribute("customerRepository", new CustomerRepository());
        sce.getServletContext().setAttribute("warrantyRepository", new WarrantyRepository());
        sce.getServletContext().setAttribute("addressRepository", new AddressRepository());
        sce.getServletContext().setAttribute("wishlistRepository", new WishlistRepository());
        sce.getServletContext().setAttribute("cartRepository", new CartRepository());

        ReviewRepository reviewRepository = new ReviewRepositoryImpl();
        sce.getServletContext().setAttribute("reviewRepository", reviewRepository);

        CommentRepository commentRepository = new CommentRepositoryImpl();
        sce.getServletContext().setAttribute("commentRepository", commentRepository);

        sce.getServletContext().setAttribute("appName", "WatchStore");

        OtpRepository otpRepository = new OtpRepository();
        EmailService emailService = new EmailService();
        sce.getServletContext().setAttribute("otpRepository", otpRepository);
        sce.getServletContext().setAttribute("emailService", emailService);
        sce.getServletContext().setAttribute("otpService", new OtpService(otpRepository, emailService));
    }
}