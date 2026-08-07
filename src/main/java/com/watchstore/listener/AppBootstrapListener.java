package com.watchstore.listener;

import com.watchstore.repository.BrandRepositoryImpl;
import com.watchstore.repository.MockProductRepository;
import com.watchstore.repository.UserRepositoryImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.watchstore.repository.RoleRepositoryImpl;

@WebListener
public class AppBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {


        sce.getServletContext().setAttribute(
                "productRepository",
                new MockProductRepository()
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
                "appName",
                "WatchStore"
        );

    }
}