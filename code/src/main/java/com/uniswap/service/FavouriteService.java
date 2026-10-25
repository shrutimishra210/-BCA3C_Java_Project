package com.uniswap.service;

import com.uniswap.dto.ListingResponse;
import com.uniswap.exception.ApiException;
import com.uniswap.model.Favourite;
import com.uniswap.model.Listing;
import com.uniswap.model.User;
import com.uniswap.repository.FavouriteRepository;
import com.uniswap.repository.ListingRepository;
import com.uniswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingService listingService;

    public FavouriteService(FavouriteRepository favouriteRepository, ListingRepository listingRepository,
                             UserRepository userRepository, ListingService listingService) {
        this.favouriteRepository = favouriteRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.listingService = listingService;
    }

    public List<ListingResponse> getFavourites(Long userId) {
        User user = requireUser(userId);
        return favouriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(f -> listingService.toResponse(f.getListing(), userId))
                .toList();
    }

    public void addFavourite(Long userId, Long listingId) {
        User user = requireUser(userId);
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> ApiException.notFound("This listing no longer exists."));

        if (favouriteRepository.existsByUserAndListing(user, listing)) {
            throw ApiException.conflict("This item is already in your favourites.");
        }
        Favourite fav = new Favourite();
        fav.setUser(user);
        fav.setListing(listing);
        favouriteRepository.save(fav);
    }

    public void removeFavourite(Long userId, Long listingId) {
        User user = requireUser(userId);
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> ApiException.notFound("This listing no longer exists."));

        Favourite fav = favouriteRepository.findByUserAndListing(user, listing)
                .orElseThrow(() -> ApiException.notFound("This item isn't in your favourites."));
        favouriteRepository.delete(fav);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please log in again."));
    }
}
