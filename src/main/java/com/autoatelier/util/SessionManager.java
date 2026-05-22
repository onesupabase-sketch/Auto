package com.autoatelier.util;

import com.autoatelier.model.User;

public class SessionManager {

    private static SessionManager instance;

    private User currentUser;
    private String accessToken;
    private String refreshToken;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setSession(User user, String accessToken, String refreshToken) {
        this.currentUser = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void clearSession() {
        this.currentUser = null;
        this.accessToken = null;
        this.refreshToken = null;
    }

    public User getCurrentUser() { return currentUser; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public boolean isLoggedIn() { return currentUser != null && accessToken != null; }
}
