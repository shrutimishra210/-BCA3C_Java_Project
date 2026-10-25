package com.uniswap.service;

import com.uniswap.dto.RequestCreateRequest;
import com.uniswap.dto.RequestResponse;
import com.uniswap.exception.ApiException;
import com.uniswap.model.*;
import com.uniswap.repository.ListingRepository;
import com.uniswap.repository.RequestRepository;
import com.uniswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final RequestRepository requestRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public RequestService(RequestRepository requestRepository, ListingRepository listingRepository,
                           UserRepository userRepository, TransactionService transactionService) {
        this.requestRepository = requestRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    public RequestResponse create(Long requesterId, RequestCreateRequest req) {
        User requester = requireUser(requesterId);
        Listing listing = listingRepository.findById(req.getListingId())
                .orElseThrow(() -> ApiException.notFound("This listing no longer exists."));

        if (listing.getOwner().getId().equals(requesterId)) {
            throw ApiException.badRequest("You can't send a request on your own listing.");
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw ApiException.badRequest("This listing is no longer available.");
        }

        TransactionType type;
        try {
            type = TransactionType.valueOf(req.getType().toUpperCase());
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid request type.");
        }

        Request request = new Request();
        request.setListing(listing);
        request.setRequester(requester);
        request.setType(type);
        request.setMessage(req.getMessage());
        request.setExtraDetail(req.getExtraDetail());
        request.setStatus(RequestStatus.PENDING);
        requestRepository.save(request);

        return toResponse(request);
    }

    public List<RequestResponse> getIncoming(Long ownerId) {
        User owner = requireUser(ownerId);
        return requestRepository.findByListing_OwnerOrderByCreatedAtDesc(owner).stream()
                .map(this::toResponse).toList();
    }

    public List<RequestResponse> getOutgoing(Long requesterId) {
        User requester = requireUser(requesterId);
        return requestRepository.findByRequesterOrderByCreatedAtDesc(requester).stream()
                .map(this::toResponse).toList();
    }

    public RequestResponse accept(Long ownerId, Long requestId) {
        Request request = findRequestOrThrow(requestId);
        requireIsListingOwner(request, ownerId);
        requirePending(request);

        request.setStatus(RequestStatus.ACCEPTED);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);

        // Move the listing out of the active pool and record the transaction.
        Listing listing = request.getListing();
        listing.setStatus(mapToListingStatus(request.getType()));
        listingRepository.save(listing);

        transactionService.recordCompleted(listing, request.getRequester(), listing.getOwner(),
                request.getType(), listing.getPrice());

        request.setStatus(RequestStatus.COMPLETED);
        requestRepository.save(request);

        return toResponse(request);
    }

    public RequestResponse reject(Long ownerId, Long requestId) {
        Request request = findRequestOrThrow(requestId);
        requireIsListingOwner(request, ownerId);
        requirePending(request);
        request.setStatus(RequestStatus.REJECTED);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        return toResponse(request);
    }

    public RequestResponse cancel(Long requesterId, Long requestId) {
        Request request = findRequestOrThrow(requestId);
        if (!request.getRequester().getId().equals(requesterId)) {
            throw ApiException.forbidden("You can only cancel your own requests.");
        }
        requirePending(request);
        request.setStatus(RequestStatus.CANCELLED);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        return toResponse(request);
    }

    // ---------------------------------------------------------------

    private Request findRequestOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("This request no longer exists."));
    }

    private void requireIsListingOwner(Request request, Long userId) {
        if (!request.getListing().getOwner().getId().equals(userId)) {
            throw ApiException.forbidden("Only the listing owner can respond to this request.");
        }
    }

    private void requirePending(Request request) {
        if (request.getStatus() != RequestStatus.PENDING) {
            throw ApiException.badRequest("This request has already been " + request.getStatus().name().toLowerCase() + ".");
        }
    }

    private ListingStatus mapToListingStatus(TransactionType type) {
        return switch (type) {
            case SELL -> ListingStatus.SOLD;
            case EXCHANGE -> ListingStatus.EXCHANGED;
            case BORROW -> ListingStatus.BORROWED;
            case DONATE -> ListingStatus.DONATED;
        };
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please log in again."));
    }

    private RequestResponse toResponse(Request r) {
        RequestResponse dto = new RequestResponse();
        dto.setId(r.getId());
        dto.setListingId(r.getListing().getId());
        dto.setListingName(r.getListing().getName());
        dto.setListingPrice(r.getListing().getPrice());
        dto.setRequesterName(r.getRequester().getFullName());
        dto.setRequesterId(r.getRequester().getId());
        dto.setOwnerName(r.getListing().getOwner().getFullName());
        dto.setType(r.getType().name());
        dto.setMessage(r.getMessage());
        dto.setExtraDetail(r.getExtraDetail());
        dto.setStatus(r.getStatus().name());
        dto.setCreatedAt(r.getCreatedAt().format(FMT));
        return dto;
    }
}
