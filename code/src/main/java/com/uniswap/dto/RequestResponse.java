package com.uniswap.dto;

public class RequestResponse {
    private Long id;
    private Long listingId;
    private String listingName;
    private String requesterName;
    private Long requesterId;
    private String ownerName;
    private String type;
    private String message;
    private String extraDetail;
    private String status;
    private Double listingPrice;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getListingName() { return listingName; }
    public void setListingName(String listingName) { this.listingName = listingName; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getExtraDetail() { return extraDetail; }
    public void setExtraDetail(String extraDetail) { this.extraDetail = extraDetail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getListingPrice() { return listingPrice; }
    public void setListingPrice(Double listingPrice) { this.listingPrice = listingPrice; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
