package com.autoatelier.service;

import com.autoatelier.model.TuningService;
import com.autoatelier.util.HttpUtil;
import com.autoatelier.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogService {

    private static CatalogService instance;

    public static CatalogService getInstance() {
        if (instance == null) instance = new CatalogService();
        return instance;
    }

    public List<TuningService> getActiveServices() throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpResponse<String> res = HttpUtil.get(
                "/tuning_services",
                "active=eq.true&order=category,name",
                token
        );
        return HttpUtil.MAPPER.readValue(res.body(), new TypeReference<>() {});
    }

    public List<TuningService> getAllServices() throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpResponse<String> res = HttpUtil.get(
                "/tuning_services", "order=category,name", token
        );
        return HttpUtil.MAPPER.readValue(res.body(), new TypeReference<>() {});
    }

    public List<TuningService> getByCategory(String category) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpResponse<String> res = HttpUtil.get(
                "/tuning_services",
                "category=eq." + category + "&active=eq.true",
                token
        );
        return HttpUtil.MAPPER.readValue(res.body(), new TypeReference<>() {});
    }

    public TuningService createService(String name, String description,
                                       Double price, String category,
                                       String imageUrl) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);
        body.put("price", price);
        body.put("category", category);
        body.put("image_url", imageUrl);
        body.put("active", true);

        HttpResponse<String> res = HttpUtil.post("/tuning_services", HttpUtil.toJson(body), token);
        if (!HttpUtil.isSuccess(res.statusCode())) {
            throw new Exception("Не удалось создать услугу");
        }

        JsonNode arr = HttpUtil.MAPPER.readTree(res.body());
        return HttpUtil.MAPPER.treeToValue(arr.get(0), TuningService.class);
    }

    public void updateService(TuningService service) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("name", service.getName());
        body.put("description", service.getDescription());
        body.put("price", service.getPrice());
        body.put("category", service.getCategory());
        body.put("image_url", service.getImageUrl());
        body.put("active", service.getActive());

        HttpUtil.patch("/tuning_services", "id=eq." + service.getId(), HttpUtil.toJson(body), token);
    }

    public void toggleActive(Long serviceId, boolean active) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("active", active);
        HttpUtil.patch("/tuning_services", "id=eq." + serviceId, HttpUtil.toJson(body), token);
    }

    public void deleteService(Long serviceId) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpUtil.delete("/tuning_services", "id=eq." + serviceId, token);
    }

    public List<String> getCategories() throws Exception {
        List<TuningService> services = getActiveServices();
        return services.stream()
                .map(TuningService::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .toList();
    }
}
