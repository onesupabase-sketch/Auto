package com.autoatelier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TuningService {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;

    @JsonProperty("image_url")
    private String imageUrl;

    private Boolean active = true;

    @JsonProperty("created_at")
    private String createdAt;

    public TuningService() {}

    public Long getId()          { return id; }
    public String getName()      { return name; }
    public String getDescription(){ return description; }
    public Double getPrice()     { return price; }
    public String getCategory()  { return category; }
    public String getImageUrl()  { return imageUrl; }
    public Boolean getActive()   { return active; }
    public String getCreatedAt() { return createdAt; }

    public void setId(Long id)                  { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setDescription(String d)        { this.description = d; }
    public void setPrice(Double price)          { this.price = price; }
    public void setCategory(String category)    { this.category = category; }
    public void setImageUrl(String imageUrl)    { this.imageUrl = imageUrl; }
    public void setActive(Boolean active)       { this.active = active; }
    public void setCreatedAt(String createdAt)  { this.createdAt = createdAt; }

    public String getPriceFormatted() {
        if (price == null) return "По запросу";
        return String.format("%.0f ₽", price);
    }

    @Override
    public String toString() { return name != null ? name : "Услуга #" + id; }
}
