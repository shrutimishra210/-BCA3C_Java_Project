package com.uniswap.controller;

import com.uniswap.dto.ListingResponse;
import com.uniswap.service.FavouriteService;
import com.uniswap.util.AuthHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favourites")
public class FavouriteController {

    private final FavouriteService favouriteService;
    private final AuthHelper authHelper;

    public FavouriteController(FavouriteService favouriteService, AuthHelper authHelper) {
        this.favouriteService = favouriteService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public List<ListingResponse> list(@RequestHeader("Authorization") String authHeader) {
        return favouriteService.getFavourites(authHelper.requireUserId(authHeader));
    }

    @PostMapping("/{listingId}")
    public void add(@RequestHeader("Authorization") String authHeader, @PathVariable Long listingId) {
        favouriteService.addFavourite(authHelper.requireUserId(authHeader), listingId);
    }

    @DeleteMapping("/{listingId}")
    public void remove(@RequestHeader("Authorization") String authHeader, @PathVariable Long listingId) {
        favouriteService.removeFavourite(authHelper.requireUserId(authHeader), listingId);
    }
}
