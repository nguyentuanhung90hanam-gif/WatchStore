package com.watchstore.repository;

import com.watchstore.enums.OrderStatus;
import com.watchstore.model.Order;
import com.watchstore.model.Product;
import com.watchstore.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockDataStore {

    private static final List<Product> PRODUCTS = new ArrayList<>();
    private static final List<Order> ORDERS = new ArrayList<>();
    private static final List<User> USERS = new ArrayList<>();

    static {

        // =========================
        // DỮ LIỆU SẢN PHẨM
        // =========================

        PRODUCTS.add(new Product(
                1,
                "Rolex Submariner",
                BigDecimal.valueOf(250000000), // Sửa ép kiểu BigDecimal
                5,
                "rolex-submariner.jpg",
                "Đồng hồ Rolex Submariner cao cấp"
        ));

        PRODUCTS.add(new Product(
                2,
                "Casio G-Shock",
                BigDecimal.valueOf(3500000), // Sửa ép kiểu BigDecimal
                20,
                "casio-gshock.jpg",
                "Đồng hồ Casio G-Shock thể thao"
        ));

        PRODUCTS.add(new Product(
                3,
                "Seiko 5 Sports",
                BigDecimal.valueOf(7500000), // Sửa ép kiểu BigDecimal
                15,
                "seiko-5.jpg",
                "Đồng hồ Seiko 5 Sports"
        ));

        PRODUCTS.add(new Product(
                4,
                "Citizen Eco-Drive",
                BigDecimal.valueOf(9000000), // Sửa ép kiểu BigDecimal
                10,
                "citizen-eco-drive.jpg",
                "Đồng hồ Citizen Eco-Drive"
        ));

        PRODUCTS.add(new Product(
                5,
                "Orient Bambino",
                BigDecimal.valueOf(6500000), // Sửa ép kiểu BigDecimal
                12,
                "orient-bambino.jpg",
                "Đồng hồ Orient Bambino"
        ));


        // =========================
        // DỮ LIỆU KHÁCH HÀNG
        // =========================

        USERS.add(new User(
                1,
                "nguyenvana",
                "123456",
                "Nguyễn Văn A",
                "nguyenvana@gmail.com",
                "0901234567",
                "Hà Nam",
                "CUSTOMER"
        ));

        USERS.add(new User(
                2,
                "tranthib",
                "123456",
                "Trần Thị B",
                "tranthib@gmail.com",
                "0912345678",
                "Hà Nội",
                "CUSTOMER"
        ));

        USERS.add(new User(
                3,
                "leminhc",
                "123456",
                "Lê Minh C",
                "leminhc@gmail.com",
                "0923456789",
                "Nam Định",
                "CUSTOMER"
        ));


        // =========================
        // DỮ LIỆU ĐƠN HÀNG
        // =========================

        ORDERS.add(new Order(
                1001,
                1,
                "Nguyễn Văn A",
                "0901234567",
                3500000,
                "Đang xử lý",
                new Date()
        ));

        ORDERS.add(new Order(
                1002,
                2,
                "Trần Thị B",
                "0912345678",
                7500000,
                "Đang giao",
                new Date()
        ));

        ORDERS.add(new Order(
                1003,
                3,
                "Lê Minh C",
                "0923456789",
                9000000,
                "Hoàn thành",
                new Date()
        ));
    }


    // =====================================================
    // PRODUCT
    // =====================================================

    public static List<Product> products() {
        return PRODUCTS;
    }


    // =====================================================
    // ORDER
    // =====================================================

    public static List<Order> orders() {
        return ORDERS;
    }


    /**
     * Tìm đơn hàng theo mã đơn hàng.
     */
    public static Order findOrder(String code) {

        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        for (Order order : ORDERS) {

            if (code.equalsIgnoreCase(order.getCode())) {
                return order;
            }
        }

        return null;
    }


    /**
     * Thêm đơn hàng mới.
     */
    public static void addOrder(Order order) {

        if (order == null) {
            return;
        }

        ORDERS.add(order);
    }


    // =====================================================
    // USER / CUSTOMER
    // =====================================================

    public static List<User> users() {
        return USERS;
    }
}