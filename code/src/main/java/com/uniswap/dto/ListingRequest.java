package com.uniswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ListingRequest {

    @NotBlank(message = "Item name is required.")
    private String name;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotNull(message = "Please choose a category.")
    private Long categoryId;

    // Only required when transactionType is SELL.
    private Double price;

    @NotBlank(message = "Please choose a condition.")
    private String condition;

    @NotBlank(message = "Please choose a transaction type.")
    private String transactionType;

    @NotBlank(message = "Location is required.")
    private String location;

    // Optional. URL returned by POST /api/listings/upload-image. Null on update = keep current image.
    private String imageUrl;

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
