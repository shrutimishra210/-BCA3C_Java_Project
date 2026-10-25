package com.uniswap.controller;

import com.uniswap.dto.CategoryResponse;
import com.uniswap.dto.ListingRequest;
import com.uniswap.dto.ListingResponse;
import com.uniswap.service.FileStorageService;
import com.uniswap.service.ListingService;
import com.uniswap.util.AuthHelper;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ListingController {

    private final ListingService listingService;
    private final AuthHelper authHelper;
    private final FileStorageService fileStorage;

    public ListingController(ListingService listingService, AuthHelper authHelper, FileStorageService fileStorage) {
        this.listingService = listingService;
        this.authHelper = authHelper;
        this.fileStorage = fileStorage;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return listingService.getCategories();
    }

    @GetMapping("/listings")
    public List<ListingResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long viewerId = safeUserId(authHeader);
        return listingService.search(keyword, categoryId, type, viewerId);
    }

    @GetMapping("/listings/mine")
    public List<ListingResponse> mine(@RequestHeader("Authorization") String authHeader) {
        Long userId = authHelper.requireUserId(authHeader);
        return listingService.getMine(userId);
    }

    @GetMapping("/listings/{id}")
    public ListingResponse getOne(@PathVariable Long id,
                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return listingService.getById(id, safeUserId(authHeader));
    }

    /** Step 1 of saving a photo: upload the file, get back the URL to send with the listing. */
    @PostMapping(value = "/listings/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadImage(@RequestHeader("Authorization") String authHeader,
                                           @RequestParam("file") MultipartFile file) {
        authHelper.requireUserId(authHeader);
        return Map.of("imageUrl", fileStorage.store(file));
    }

    @PostMapping("/listings")
    public ListingResponse create(@RequestHeader("Authorization") String authHeader,
                                   @Valid @RequestBody ListingRequest request) {
        Long userId = authHelper.requireUserId(authHeader);
        return listingService.create(userId, request);
    }

    @PutMapping("/listings/{id}")
    public ListingResponse update(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable Long id,
                                   @Valid @RequestBody ListingRequest request) {
        Long userId = authHelper.requireUserId(authHeader);
        return listingService.update(userId, id, request);
    }

    @DeleteMapping("/listings/{id}")
    public void delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        Long userId = authHelper.requireUserId(authHeader);
        listingService.delete(userId, id);
    }

    @PutMapping("/listings/{id}/status")
    public ListingResponse updateStatus(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable Long id,
                                         @RequestParam String status) {
        Long userId = authHelper.requireUserId(authHeader);
        return listingService.updateStatus(userId, id, status);
    }

    private Long safeUserId(String authHeader) {
        try {
            return authHelper.requireUserId(authHeader);
        } catch (Exception e) {
            return null; // Browsing the marketplace doesn't require login.
        }
    }
}
