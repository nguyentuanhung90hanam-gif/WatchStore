package com.watchstore.listener;

import com.watchstore.repository.BrandRepositoryImpl;
import com.watchstore.repository.MockProductRepository;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

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
                "appName",
                "WatchStore"
        );
    }
}