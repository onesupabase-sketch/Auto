package com.autoatelier.service;

import com.autoatelier.model.Order;
import com.autoatelier.util.HttpUtil;
import com.autoatelier.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {

    private static OrderService instance;
    private static final String SELECT = "select=*,service:tuning_services(*),client:profiles!orders_client_id_fkey(*),manager:profiles!orders_manager_id_fkey(*)";

    public static OrderService getInstance() {
        if (instance == null) instance = new OrderService();
        return instance;
    }

    public List<Order> getMyOrders() throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        String userId = SessionManager.getInstance().getCurrentUser().getId();

        HttpResponse<String> res = HttpUtil.get(
                "/orders",
                SELECT + "&client_id=eq." + userId + "&order=created_at.desc",
                token
        );
        return HttpUtil.MAPPER.readValue(res.body(), new TypeReference<>() {});
    }

    public List<Order> getAllOrders() throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpResponse<String> res = HttpUtil.get(
                "/orders",
                SELECT + "&order=created_at.desc",
                token
        );
        return HttpUtil.MAPPER.readValue(res.body(), new TypeReference<>() {});
    }

    public List<Order> getOrdersByStatus(String status) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpResponse<String> res = HttpUtil.get(
                "/orders",
                SELECT + "&status=eq." + status + "&order=created_at.desc",
                token
        );
        return HttpUtil.MAPPER.readValue(res.body(), new TypeReference<>() {});
    }

    public Order createOrder(Long serviceId, String carModel, Integer carYear,
                             String description, Double totalPrice) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        String clientId = SessionManager.getInstance().getCurrentUser().getId();

        Map<String, Object> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("service_id", serviceId);
        body.put("car_model", carModel);
        body.put("car_year", carYear);
        body.put("description", description);
        body.put("total_price", totalPrice);
        body.put("status", Order.Status.NEW.key);

        HttpResponse<String> res = HttpUtil.post("/orders", HttpUtil.toJson(body), token);

        if (!HttpUtil.isSuccess(res.statusCode())) {
            throw new Exception("Не удалось создать заявку");
        }

        JsonNode arr = HttpUtil.MAPPER.readTree(res.body());
        return HttpUtil.MAPPER.treeToValue(arr.get(0), Order.class);
    }

    public void updateStatus(Long orderId, String status, String comment) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        String managerId = SessionManager.getInstance().getCurrentUser().getId();

        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("manager_id", managerId);
        if (comment != null && !comment.isBlank()) {
            body.put("manager_comment", comment);
        }

        java.net.http.HttpResponse<String> res =
                HttpUtil.patch("/orders", "id=eq." + orderId, HttpUtil.toJson(body), token);
        if (!HttpUtil.isSuccess(res.statusCode())) {
            throw new Exception("Не удалось обновить статус (HTTP " + res.statusCode() + "): " + res.body());
        }
    }

    public void updatePrice(Long orderId, Double price) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("total_price", price);
        HttpUtil.patch("/orders", "id=eq." + orderId, HttpUtil.toJson(body), token);
    }

    public void deleteOrder(Long orderId) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpUtil.delete("/orders", "id=eq." + orderId, token);
    }

    public Map<String, Long> getStats() throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpResponse<String> res = HttpUtil.get("/orders", "select=status", token);
        JsonNode arr = HttpUtil.MAPPER.readTree(res.body());

        Map<String, Long> stats = new HashMap<>();
        for (Order.Status s : Order.Status.values()) stats.put(s.key, 0L);

        for (JsonNode node : arr) {
            String status = node.path("status").asText();
            stats.merge(status, 1L, Long::sum);
        }
        return stats;
    }
    public void cancelOrder(Long orderId) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        String clientId = SessionManager.getInstance().getCurrentUser().getId();
        Map<String, Object> body = new HashMap<>();
        body.put("status", Order.Status.CANCELLED.key);

        java.net.http.HttpResponse<String> res =
                HttpUtil.patch("/orders",
                        "id=eq." + orderId + "&client_id=eq." + clientId,
                        HttpUtil.toJson(body), token);
        if (!HttpUtil.isSuccess(res.statusCode())) {
            throw new Exception("Не удалось отменить заявку (HTTP " + res.statusCode() + "): " + res.body());
        }
    }

}
