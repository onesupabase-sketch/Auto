package com.autoatelier.service;

import com.autoatelier.model.User;
import com.autoatelier.util.HttpUtil;
import com.autoatelier.util.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private static AuthService instance;

    public static AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public User register(String email, String password,
                         String fullName, String phone) throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        HttpResponse<String> res = HttpUtil.authPost("/signup", HttpUtil.toJson(body));

        if (!HttpUtil.isSuccess(res.statusCode())) {
            JsonNode err = HttpUtil.MAPPER.readTree(res.body());
            throw new Exception(err.path("msg").asText("Ошибка регистрации"));
        }

        JsonNode json = HttpUtil.MAPPER.readTree(res.body());
        String userId = json.path("user").path("id").asText();
        String accessToken = json.path("access_token").asText();
        String refreshToken = json.path("refresh_token").asText();

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", userId);
        profile.put("full_name", fullName);
        profile.put("phone", phone);
        profile.put("role", "client");

        HttpResponse<String> profileRes = HttpUtil.post(
                "/profiles", HttpUtil.toJson(profile), accessToken
        );

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole("client");

        SessionManager.getInstance().setSession(user, accessToken, refreshToken);
        return user;
    }

    public User login(String email, String password) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        HttpResponse<String> res = HttpUtil.authPost(
                "/token?grant_type=password", HttpUtil.toJson(body)
        );

        if (!HttpUtil.isSuccess(res.statusCode())) {
            JsonNode err = HttpUtil.MAPPER.readTree(res.body());
            throw new Exception(err.path("error_description").asText("Неверный логин или пароль"));
        }

        JsonNode json = HttpUtil.MAPPER.readTree(res.body());
        String accessToken = json.path("access_token").asText();
        String refreshToken = json.path("refresh_token").asText();
        String userId = json.path("user").path("id").asText();

        HttpResponse<String> profileRes = HttpUtil.get(
                "/profiles", "id=eq." + userId + "&select=*", accessToken
        );

        JsonNode profiles = HttpUtil.MAPPER.readTree(profileRes.body());
        if (!profiles.isArray() || profiles.isEmpty()) {
            throw new Exception("Профиль пользователя не найден");
        }

        User user = HttpUtil.MAPPER.treeToValue(profiles.get(0), User.class);
        user.setEmail(email);

        if (user.isBlocked()) {
            throw new Exception("Ваш аккаунт заблокирован");
        }

        SessionManager.getInstance().setSession(user, accessToken, refreshToken);
        return user;
    }

    public void logout() {
        SessionManager.getInstance().clearSession();
    }

    public void updateProfile(User user) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("full_name", user.getFullName());
        body.put("phone", user.getPhone());

        HttpUtil.patch("/profiles", "id=eq." + user.getId(), HttpUtil.toJson(body), token);
    }
}
