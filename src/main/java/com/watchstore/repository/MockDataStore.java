package com.watchstore.repository;

import com.watchstore.model.Order;
import com.watchstore.model.Product;
import com.watchstore.model.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockDataStore {

    private static final List<Product> PRODUCTS = new ArrayList<>();
    private static final List<Order> ORDERS = new ArrayList<>();
    private static final List<User> USERS = new ArrayList<>();

    static {

        PRODUCTS.add(new Product(
                1,
                "CASIO",
                "Edifice Sapphire EFR-S108D",
                "CAS-EFR-108",
                new BigDecimal("3450000"),
                new BigDecimal("4290000"),
                "watch-1.png",
                "Bán chạy",
                18,
                4.8
        ));

        PRODUCTS.add(new Product(
                2,
                "ORIENT",
                "Bambino Open Heart Classic",
                "ORI-BAM-210",
                new BigDecimal("6790000"),
                new BigDecimal("7990000"),
                "watch-2.png",
                "-15%",
                9,
                4.9
        ));

        PRODUCTS.add(new Product(
                3,
                "SEIKO",
                "Prospex Diver Automatic",
                "SEI-PRO-510",
                new BigDecimal("9890000"),
                new BigDecimal("11200000"),
                "watch-3.png",
                "Mới",
                5,
                4.7
        ));

        PRODUCTS.add(new Product(
                4,
                "FOSSIL",
                "Minimalist Mesh Rose Gold",
                "FOS-MIN-330",
                new BigDecimal("4250000"),
                new BigDecimal("4990000"),
                "watch-4.png",
                "Độc quyền",
                22,
                4.8
        ));

        PRODUCTS.add(new Product(
                5,
                "CITIZEN",
                "Tsuyosa Automatic Blue",
                "CIT-TSU-040",
                new BigDecimal("8250000"),
                new BigDecimal("9200000"),
                "watch-1.png",
                "AUTOMATIC",
                7,
                4.9
        ));

        PRODUCTS.add(new Product(
                6,
                "TISSOT",
                "Le Locle Powermatic 80",
                "TIS-LEL-080",
                new BigDecimal("16800000"),
                new BigDecimal("18500000"),
                "watch-2.png",
                "CAO CẤP",
                3,
                5.0
        ));

        PRODUCTS.add(new Product(
                7,
                "G-SHOCK",
                "GA-B2100 Carbon Core",
                "GSH-GAB-210",
                new BigDecimal("3990000"),
                new BigDecimal("4590000"),
                "watch-3.png",
                "THỂ THAO",
                26,
                4.8
        ));

        PRODUCTS.add(new Product(
                8,
                "FOSSIL",
                "Machine Chronograph",
                "FOS-MAC-420",
                new BigDecimal("5190000"),
                new BigDecimal("5890000"),
                "watch-4.png",
                "ƯU ĐÃI",
                14,
                4.6
        ));

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

    public static List<Product> products() {
        return PRODUCTS;
    }

    public static List<Order> orders() {
        return ORDERS;
    }

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

    public static void addOrder(Order order) {
        if (order == null) {
            return;
        }

        ORDERS.add(order);
    }

    public static List<User> users() {
        return USERS;
    }
}