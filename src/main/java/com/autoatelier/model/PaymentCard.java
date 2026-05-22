package com.autoatelier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentCard {

    private Long id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("card_holder")
    private String cardHolder;

    @JsonProperty("expiry_month")
    private String expiryMonth;

    @JsonProperty("expiry_year")
    private String expiryYear;

    @JsonProperty("created_at")
    private String createdAt;

    public PaymentCard() {}

    public Long getId()             { return id; }
    public String getUserId()       { return userId; }
    public String getCardNumber()   { return cardNumber; }
    public String getCardHolder()   { return cardHolder; }
    public String getExpiryMonth()  { return expiryMonth; }
    public String getExpiryYear()   { return expiryYear; }
    public String getCreatedAt()    { return createdAt; }

    public void setId(Long id)                  { this.id = id; }
    public void setUserId(String userId)        { this.userId = userId; }
    public void setCardNumber(String v)         { this.cardNumber = v; }
    public void setCardHolder(String v)         { this.cardHolder = v; }
    public void setExpiryMonth(String v)        { this.expiryMonth = v; }
    public void setExpiryYear(String v)         { this.expiryYear = v; }
    public void setCreatedAt(String v)          { this.createdAt = v; }

    public String getMaskedNumber() {
        if (cardNumber == null || cardNumber.length() < 4) return "**** **** **** ****";
        String last4 = cardNumber.replaceAll("\\s", "");
        last4 = last4.substring(Math.max(0, last4.length() - 4));
        return "**** **** **** " + last4;
    }

    public String getExpiry() {
        return (expiryMonth != null ? expiryMonth : "??") + " / " + (expiryYear != null ? expiryYear : "??");
    }

    @Override
    public String toString() {
        return getMaskedNumber() + "  " + getExpiry();
    }
}
