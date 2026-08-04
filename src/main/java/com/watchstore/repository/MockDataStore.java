package com.watchstore.repository;

import com.watchstore.enums.OrderStatus;
import com.watchstore.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MockDataStore {
    private static final List<Order> ORDERS = Collections.synchronizedList(new ArrayList<>(List.of(
        new Order("WS8492", "Nguyễn Văn An", LocalDateTime.now().minusMinutes(12), new BigDecimal("10240000"), OrderStatus.PENDING),
        new Order("WS8491", "Trần Minh Đức", LocalDateTime.now().minusHours(2), new BigDecimal("6790000"), OrderStatus.CONFIRMED),
        new Order("WS8490", "Lê Thành Công", LocalDateTime.now().minusDays(1), new BigDecimal("4250000"), OrderStatus.SHIPPING),
        new Order("WS8489", "Phạm Gia Huy", LocalDateTime.now().minusDays(2), new BigDecimal("9890000"), OrderStatus.COMPLETED)
    )));
    private MockDataStore() {}
    public static List<Order> orders() { return ORDERS; }
    public static Order findOrder(String code) { return ORDERS.stream().filter(o -> o.getCode().equalsIgnoreCase(code)).findFirst().orElse(ORDERS.get(0)); }
    public static void addOrder(Order order) { ORDERS.add(0, order); }
}
