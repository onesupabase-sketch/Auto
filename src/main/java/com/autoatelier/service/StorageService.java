package com.autoatelier.service;

import com.autoatelier.config.SupabaseConfig;
import com.autoatelier.util.HttpUtil;
import com.autoatelier.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StorageService {

    private static StorageService instance;

    public static StorageService getInstance() {
        if (instance == null) instance = new StorageService();
        return instance;
    }

    public String uploadPhoto(File file, String bucket) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        byte[] data = Files.readAllBytes(file.toPath());
        String ext = getExtension(file.getName());
        String mimeType = getMimeType(ext);
        String remotePath = UUID.randomUUID() + "." + ext;
        HttpResponse<String> res = HttpUtil.uploadFile(bucket, remotePath, data, mimeType, token);
        if (!HttpUtil.isSuccess(res.statusCode()))
            throw new Exception("Не удалось загрузить фото: " + res.body());
        return HttpUtil.getPublicUrl(bucket, remotePath);
    }

    public List<com.autoatelier.model.User> getAllUsers() throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpResponse<String> res = HttpUtil.get("/profiles", "order=created_at.desc", token);
        return HttpUtil.MAPPER.readValue(
                res.body(), new TypeReference<List<com.autoatelier.model.User>>() {});
    }

    public void setUserBlocked(String userId, boolean blocked) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("blocked", blocked);
        var res = HttpUtil.patch("/profiles", "id=eq." + userId, HttpUtil.toJson(body), token);
        if (!HttpUtil.isSuccess(res.statusCode())) {
            throw new Exception("Ошибка сервера (" + res.statusCode() + "): " + res.body());
        }

        if ("[]".equals(res.body().trim()) || res.body().trim().equals("")) {
            throw new Exception(
                "Суpabase не обновил запись. Убедитесь что колонка 'blocked' существует в таблице profiles.\n" +
                "Выполните SQL: ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS blocked BOOLEAN DEFAULT FALSE;"
            );
        }
    }

    public void updateUserRole(String userId, String role) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("role", role);
        var res = HttpUtil.patch("/profiles", "id=eq." + userId, HttpUtil.toJson(body), token);
        if (!HttpUtil.isSuccess(res.statusCode())) {
            throw new Exception("Ошибка сервера (" + res.statusCode() + "): " + res.body());
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot + 1).toLowerCase() : "jpg";
    }

    private String getMimeType(String ext) {
        return switch (ext) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
