package com.uniswap.dto;

import java.util.List;

public class DashboardResponse {
    private String welcomeName;
    private long totalActiveListings;
    private long totalFavourites;
    private long pendingRequests;
    private List<ListingResponse> recentListings;
    private List<String> popularCategories;

    public String getWelcomeName() { return welcomeName; }
    public void setWelcomeName(String welcomeName) { this.welcomeName = welcomeName; }
    public long getTotalActiveListings() { return totalActiveListings; }
    public void setTotalActiveListings(long totalActiveListings) { this.totalActiveListings = totalActiveListings; }
    public long getTotalFavourites() { return totalFavourites; }
    public void setTotalFavourites(long totalFavourites) { this.totalFavourites = totalFavourites; }
    public long getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }
    public List<ListingResponse> getRecentListings() { return recentListings; }
    public void setRecentListings(List<ListingResponse> recentListings) { this.recentListings = recentListings; }
    public List<String> getPopularCategories() { return popularCategories; }
    public void setPopularCategories(List<String> popularCategories) { this.popularCategories = popularCategories; }
}
