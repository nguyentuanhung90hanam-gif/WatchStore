package com.watchstore.util;

import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SessionCart {
    private SessionCart() {}
    @SuppressWarnings("unchecked")
    public static Map<Integer, Integer> get(HttpSession session) {
        Object value = session.getAttribute("cart");
        if (value instanceof Map<?, ?>) return (Map<Integer, Integer>) value;
        Map<Integer, Integer> cart = new LinkedHashMap<>();
        session.setAttribute("cart", cart);
        return cart;
    }
    public static int count(HttpSession session) { return get(session).values().stream().mapToInt(Integer::intValue).sum(); }
}
