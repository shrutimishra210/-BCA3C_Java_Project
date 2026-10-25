package com.uniswap.dto;

public class TransactionResponse {
    private Long id;
    private String listingName;
    private String type;
    private Double amount;
    private String status;
    private String role; // "Bought"/"Sold"/etc. relative to the logged-in user
    private String otherPartyName;
    private String completedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getListingName() { return listingName; }
    public void setListingName(String listingName) { this.listingName = listingName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getOtherPartyName() { return otherPartyName; }
    public void setOtherPartyName(String otherPartyName) { this.otherPartyName = otherPartyName; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
}
