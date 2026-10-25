package com.uniswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RequestCreateRequest {

    @NotNull(message = "Listing is required.")
    private Long listingId;

    @NotBlank(message = "Please choose a request type.")
    private String type; // SELL(=buy)/EXCHANGE/BORROW/DONATE

    private String message;

    // Exchange offer, or borrow duration - whichever applies.
    private String extraDetail;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getExtraDetail() { return extraDetail; }
    public void setExtraDetail(String extraDetail) { this.extraDetail = extraDetail; }
}
