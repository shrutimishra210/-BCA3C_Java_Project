package com.uniswap.service;

import com.uniswap.dto.CategoryResponse;
import com.uniswap.dto.ListingRequest;
import com.uniswap.dto.ListingResponse;
import com.uniswap.exception.ApiException;
import com.uniswap.model.*;
import com.uniswap.repository.CategoryRepository;
import com.uniswap.repository.FavouriteRepository;
import com.uniswap.repository.ListingRepository;
import com.uniswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class ListingService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FavouriteRepository favouriteRepository;
    private final FileStorageService fileStorage;

    public ListingService(ListingRepository listingRepository, CategoryRepository categoryRepository,
                           UserRepository userRepository, FavouriteRepository favouriteRepository,
                           FileStorageService fileStorage) {
        this.fileStorage = fileStorage;
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.favouriteRepository = favouriteRepository;
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    public List<ListingResponse> search(String keyword, Long categoryId, String type, Long viewerUserId) {
        TransactionType txType = null;
        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL")) {
            txType = TransactionType.valueOf(type.toUpperCase());
        }
        List<Listing> results = listingRepository.search(
                (keyword == null ? "" : keyword.trim()), categoryId, txType);
        return results.stream().map(l -> toResponse(l, viewerUserId)).toList();
    }

    public ListingResponse getById(Long id, Long viewerUserId) {
        Listing l = findListingOrThrow(id);
        return toResponse(l, viewerUserId);
    }

    public List<ListingResponse> getMine(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please log in again."));
        return listingRepository.findByOwnerOrderByCreatedAtDesc(owner).stream()
                .map(l -> toResponse(l, userId)).toList();
    }

    public ListingResponse create(Long ownerId, ListingRequest req) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> ApiException.unauthorized("Please log in again."));

        validateListingRequest(req);

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> ApiException.badRequest("Please choose a valid category."));

        Listing listing = new Listing();
        listing.setName(req.getName().trim());
        listing.setDescription(req.getDescription().trim());
        listing.setCategory(category);
        listing.setCondition(parseCondition(req.getCondition()));
        listing.setTransactionType(parseType(req.getTransactionType()));
        listing.setPrice(listing.getTransactionType() == TransactionType.SELL ? req.getPrice() : 0.0);
        listing.setLocation(req.getLocation().trim());
        listing.setOwner(owner);
        listing.setStatus(ListingStatus.ACTIVE);
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {
            listing.setImageUrl(fileStorage.requireStored(req.getImageUrl().trim()));
        }

        listingRepository.save(listing);
        return toResponse(listing, ownerId);
    }

    public ListingResponse update(Long ownerId, Long listingId, ListingRequest req) {
        Listing listing = findListingOrThrow(listingId);
        requireOwnership(listing, ownerId);
        validateListingRequest(req);

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> ApiException.badRequest("Please choose a valid category."));

        listing.setName(req.getName().trim());
        listing.setDescription(req.getDescription().trim());
        listing.setCategory(category);
        listing.setCondition(parseCondition(req.getCondition()));
        listing.setTransactionType(parseType(req.getTransactionType()));
        listing.setPrice(listing.getTransactionType() == TransactionType.SELL ? req.getPrice() : 0.0);
        listing.setLocation(req.getLocation().trim());

        // null / blank = keep the current image; otherwise swap to the newly uploaded one.
        String oldImage = listing.getImageUrl();
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {
            listing.setImageUrl(fileStorage.requireStored(req.getImageUrl().trim()));
        }

        listingRepository.save(listing);
        if (!Objects.equals(oldImage, listing.getImageUrl())) {
            deleteImageIfUnused(oldImage);
        }
        return toResponse(listing, ownerId);
    }

    public void delete(Long ownerId, Long listingId) {
        Listing listing = findListingOrThrow(listingId);
        requireOwnership(listing, ownerId);
        listingRepository.delete(listing);
        deleteImageIfUnused(listing.getImageUrl());
    }

    public ListingResponse updateStatus(Long ownerId, Long listingId, String status) {
        Listing listing = findListingOrThrow(listingId);
        requireOwnership(listing, ownerId);
        try {
            listing.setStatus(ListingStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid listing status.");
        }
        listingRepository.save(listing);
        return toResponse(listing, ownerId);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    Listing findListingOrThrow(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("This listing no longer exists."));
    }

    /** Removes an uploaded file once no listing points at it any more (never touches other products' images). */
    private void deleteImageIfUnused(String imageUrl) {
        if (imageUrl != null && listingRepository.countByImageUrl(imageUrl) == 0) {
            fileStorage.delete(imageUrl);
        }
    }

    private void requireOwnership(Listing listing, Long userId) {
        if (!listing.getOwner().getId().equals(userId)) {
            throw ApiException.forbidden("You can only manage your own listings.");
        }
    }

    private void validateListingRequest(ListingRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw ApiException.badRequest("Item name is required.");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw ApiException.badRequest("Description is required.");
        }
        if (req.getLocation() == null || req.getLocation().isBlank()) {
            throw ApiException.badRequest("Campus location is required.");
        }
        TransactionType type = parseType(req.getTransactionType());
        if (type == TransactionType.SELL) {
            if (req.getPrice() == null || req.getPrice() <= 0) {
                throw ApiException.badRequest("Please enter a valid price greater than zero.");
            }
        }
    }

    private ItemCondition parseCondition(String value) {
        try {
            return ItemCondition.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (Exception e) {
            throw ApiException.badRequest("Please choose a valid condition.");
        }
    }

    private TransactionType parseType(String value) {
        try {
            return TransactionType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw ApiException.badRequest("Please choose a valid transaction type.");
        }
    }

    ListingResponse toResponse(Listing l, Long viewerUserId) {
        ListingResponse r = new ListingResponse();
        r.setId(l.getId());
        r.setName(l.getName());
        r.setDescription(l.getDescription());
        r.setCategoryName(l.getCategory().getName());
        r.setCategoryId(l.getCategory().getId());
        r.setPrice(l.getPrice());
        r.setCondition(l.getCondition().name());
        r.setTransactionType(l.getTransactionType().name());
        r.setStatus(l.getStatus().name());
        r.setLocation(l.getLocation());
        r.setImageUrl(l.getImageUrl());
        r.setOwnerName(l.getOwner().getFullName());
        r.setOwnerId(l.getOwner().getId());
        r.setCreatedAt(l.getCreatedAt().format(FMT));
        if (viewerUserId != null) {
            User viewer = userRepository.findById(viewerUserId).orElse(null);
            if (viewer != null) {
                r.setFavourite(favouriteRepository.existsByUserAndListing(viewer, l));
            }
        }
        return r;
    }
}
