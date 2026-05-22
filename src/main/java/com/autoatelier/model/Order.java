package com.autoatelier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    public enum Status {
        NEW("new", "Новый", "#4A90D9"),
        IN_PROGRESS("in_progress", "В работе", "#F5A623"),
        COMPLETED("completed", "Завершён", "#27AE60"),
        CANCELLED("cancelled", "Отменён", "#E74C3C");

        public final String key;
        public final String display;
        public final String color;

        Status(String key, String display, String color) {
            this.key = key;
            this.display = display;
            this.color = color;
        }

        public static Status fromKey(String key) {
            for (Status s : values()) {
                if (s.key.equals(key)) return s;
            }
            return NEW;
        }
    }

    private Long id;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("manager_id")
    private String managerId;

    @JsonProperty("service_id")
    private Long serviceId;

    @JsonProperty("car_model")
    private String carModel;

    @JsonProperty("car_year")
    private Integer carYear;

    private String description;
    private String status;

    @JsonProperty("manager_comment")
    private String managerComment;

    @JsonProperty("total_price")
    private Double totalPrice;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private TuningService service;

    @JsonProperty("client")
    private User client;

    @JsonProperty("manager")
    private User manager;

    public Order() {}

    public Long getId()               { return id; }
    public String getClientId()       { return clientId; }
    public String getManagerId()      { return managerId; }
    public Long getServiceId()        { return serviceId; }
    public String getCarModel()       { return carModel; }
    public Integer getCarYear()       { return carYear; }
    public String getDescription()    { return description; }
    public String getStatus()         { return status; }
    public String getManagerComment() { return managerComment; }
    public Double getTotalPrice()     { return totalPrice; }
    public String getCreatedAt()      { return createdAt; }
    public String getUpdatedAt()      { return updatedAt; }
    public TuningService getService() { return service; }
    public User getClient()           { return client; }
    public User getManager()          { return manager; }

    public void setId(Long id)                      { this.id = id; }
    public void setClientId(String clientId)        { this.clientId = clientId; }
    public void setManagerId(String managerId)      { this.managerId = managerId; }
    public void setServiceId(Long serviceId)        { this.serviceId = serviceId; }
    public void setCarModel(String carModel)        { this.carModel = carModel; }
    public void setCarYear(Integer carYear)         { this.carYear = carYear; }
    public void setDescription(String description)  { this.description = description; }
    public void setStatus(String status)            { this.status = status; }
    public void setManagerComment(String c)         { this.managerComment = c; }
    public void setTotalPrice(Double totalPrice)    { this.totalPrice = totalPrice; }
    public void setCreatedAt(String createdAt)      { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt)      { this.updatedAt = updatedAt; }
    public void setService(TuningService service)   { this.service = service; }
    public void setClient(User client)              { this.client = client; }
    public void setManager(User manager)            { this.manager = manager; }

    public Status getStatusEnum() { return Status.fromKey(status); }

    public String getStatusDisplay() { return getStatusEnum().display; }

    public String getPriceFormatted() {
        if (totalPrice == null) return "—";
        return String.format("%.0f ₽", totalPrice);
    }

    public String getCarInfo() {
        if (carModel == null) return "Не указано";
        return carYear != null ? carModel + " (" + carYear + ")" : carModel;
    }
}
