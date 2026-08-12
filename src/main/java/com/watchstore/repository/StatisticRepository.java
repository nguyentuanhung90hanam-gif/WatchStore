package com.watchstore.repository;

import com.watchstore.model.Order;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StatisticRepository {
    BigDecimal getTotalRevenue();
    int getTotalOrdersCount();
    int getTotalProductsCount();
    int getTotalCustomersCount();
    int getTotalBrandsCount();
    int getTotalCategoriesCount();
    int getTotalVouchersCount();
    int getLowStockCount();

    int getPendingOrdersCount();
    int getActiveProductsCount();
    int getActiveCustomersCount();
    int getExpiringVouchersCount();

    Map<String, Integer> getOrderStatusCounts();
    Map<String, Integer> getProductsByBrandCounts();
    Map<String, Integer> getProductsByCategoryCounts();

    List<Map<String, Object>> getDailySalesTrend();
    List<Map<String, Object>> getLast7DaysSales();
    List<Map<String, Object>> getTopSellingProducts();
    List<Map<String, Object>> getTopCustomers();
    List<Map<String, Object>> getLowStockItems();
    List<Order> getRecentOrders(int limit);
}

