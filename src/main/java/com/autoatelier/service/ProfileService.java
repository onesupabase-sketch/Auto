package com.autoatelier.service;

import com.autoatelier.model.PaymentCard;
import com.autoatelier.model.User;
import com.autoatelier.util.HttpUtil;
import com.autoatelier.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileService {

    private static ProfileService instance;
    public static ProfileService getInstance() {
        if (instance == null) instance = new ProfileService();
        return instance;
    }

    public void updateProfile(String fullName, String phone) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        String id    = SessionManager.getInstance().getCurrentUser().getId();

        Map<String, Object> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("phone", phone);
        HttpUtil.patch("/profiles", "id=eq." + id, HttpUtil.toJson(body), token);

        User u = SessionManager.getInstance().getCurrentUser();
        u.setFullName(fullName);
        u.setPhone(phone);
    }

    public void changePassword(String newPassword) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("password", newPassword);
        HttpResponse<String> res = HttpUtil.authPatch("/user", HttpUtil.toJson(body), token);
        if (!HttpUtil.isSuccess(res.statusCode())) {
            JsonNode err = HttpUtil.MAPPER.readTree(res.body());
            throw new Exception(err.path("msg").asText("Ошибка смены пароля"));
        }
    }

    public void deleteAccount() throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        String id    = SessionManager.getInstance().getCurrentUser().getId();
        HttpUtil.delete("/profiles", "id=eq." + id, token);
        SessionManager.getInstance().clearSession();
    }

    public List<PaymentCard> getMyCards() throws Exception {
        String token  = SessionManager.getInstance().getAccessToken();
        String userId = SessionManager.getInstance().getCurrentUser().getId();
        HttpResponse<String> res = HttpUtil.get(
            "/payment_cards", "user_id=eq." + userId + "&order=created_at.desc", token);
        return HttpUtil.MAPPER.readValue(res.body(), new TypeReference<>() {});
    }

    public PaymentCard addCard(String rawNumber, String cardHolder,
                               String month, String year) throws Exception {
        String token  = SessionManager.getInstance().getAccessToken();
        String userId = SessionManager.getInstance().getCurrentUser().getId();

        String clean = rawNumber.replaceAll("[^0-9]", "");
        String last4 = clean.length() >= 4 ? clean.substring(clean.length() - 4) : clean;
        String masked = "**** **** **** " + last4;

        Map<String, Object> body = new HashMap<>();
        body.put("user_id",      userId);
        body.put("card_number",  masked);
        body.put("card_holder",  cardHolder.toUpperCase().trim());
        body.put("expiry_month", month);
        body.put("expiry_year",  year);

        HttpResponse<String> res = HttpUtil.post("/payment_cards", HttpUtil.toJson(body), token);
        if (!HttpUtil.isSuccess(res.statusCode()))
            throw new Exception("Не удалось сохранить карту");

        JsonNode arr = HttpUtil.MAPPER.readTree(res.body());
        return HttpUtil.MAPPER.treeToValue(arr.get(0), PaymentCard.class);
    }

    public void deleteCard(Long cardId) throws Exception {
        String token = SessionManager.getInstance().getAccessToken();
        HttpUtil.delete("/payment_cards", "id=eq." + cardId, token);
    }
}
