package com.autoatelier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String id;

    @JsonProperty("full_name")
    private String fullName;

    private String phone;
    private String role;
    private String email;
    private Boolean blocked;

    @JsonProperty("created_at")
    private String createdAt;

    public User() {}

    public String getId()        { return id; }
    public String getFullName()  { return fullName; }
    public String getPhone()     { return phone; }
    public String getRole()      { return role; }
    public String getEmail()     { return email; }
    public Boolean getBlocked()  { return blocked; }
    public String getCreatedAt() { return createdAt; }

    public void setId(String id)             { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone)       { this.phone = phone; }
    public void setRole(String role)         { this.role = role; }
    public void setEmail(String email)       { this.email = email; }
    public void setBlocked(Boolean blocked)  { this.blocked = blocked; }
    public void setCreatedAt(String v)       { this.createdAt = v; }

    public boolean isAdmin()   { return "admin".equals(role); }
    public boolean isManager() { return "manager".equals(role); }
    public boolean isClient()  { return "client".equals(role); }
    public boolean isBlocked() { return Boolean.TRUE.equals(blocked); }

    public String getRoleDisplay() {
        if (role == null) return "Клиент";
        return switch (role) {
            case "admin"   -> "Администратор";
            case "manager" -> "Менеджер";
            default        -> "Клиент";
        };
    }
}
