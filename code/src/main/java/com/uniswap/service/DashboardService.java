package com.uniswap.service;

import com.uniswap.dto.DashboardResponse;
import com.uniswap.dto.ListingResponse;
import com.uniswap.exception.ApiException;
import com.uniswap.model.Listing;
import com.uniswap.model.ListingStatus;
import com.uniswap.model.RequestStatus;
import com.uniswap.model.User;
import com.uniswap.repository.FavouriteRepository;
import com.uniswap.repository.ListingRepository;
import com.uniswap.repository.RequestRepository;
import com.uniswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final FavouriteRepository favouriteRepository;
    private final RequestRepository requestRepository;
    private final ListingService listingService;

    public DashboardService(UserRepository userRepository, ListingRepository listingRepository,
                             FavouriteRepository favouriteRepository, RequestRepository requestRepository,
                             ListingService listingService) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.favouriteRepository = favouriteRepository;
        this.requestRepository = requestRepository;
        this.listingService = listingService;
    }

    public DashboardResponse build(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please log in again."));

        DashboardResponse dto = new DashboardResponse();
        dto.setWelcomeName(user.getFullName());
        dto.setTotalActiveListings(listingRepository.countByOwnerAndStatus(user, ListingStatus.ACTIVE));
        dto.setTotalFavourites(favouriteRepository.countByUser(user));
        dto.setPendingRequests(requestRepository.countByListing_OwnerAndStatus(user, RequestStatus.PENDING));

        List<Listing> recent = listingRepository.findTop8ByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE);
        List<ListingResponse> recentDtos = recent.stream()
                .map(l -> listingService.toResponse(l, userId)).toList();
        dto.setRecentListings(recentDtos);

        // "Popular" here = categories with the most active listings right now -
        // a simple, honest way to compute this without extra tracking tables.
        List<Listing> allActive = listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE);
        Map<String, Long> counts = allActive.stream()
                .collect(Collectors.groupingBy(l -> l.getCategory().getName(), Collectors.counting()));
        List<String> popular = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
        dto.setPopularCategories(popular);

        return dto;
    }
}
