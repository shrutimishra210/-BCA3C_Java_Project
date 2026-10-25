package com.uniswap.service;

import com.uniswap.dto.TransactionResponse;
import com.uniswap.exception.ApiException;
import com.uniswap.model.Listing;
import com.uniswap.model.Transaction;
import com.uniswap.model.TransactionType;
import com.uniswap.model.User;
import com.uniswap.repository.TransactionRepository;
import com.uniswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TransactionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /** Called by RequestService the moment an owner accepts a request. */
    public void recordCompleted(Listing listing, User requester, User owner, TransactionType type, Double amount) {
        Transaction t = new Transaction();
        t.setListing(listing);
        t.setRequester(requester);
        t.setOwner(owner);
        t.setType(type);
        t.setAmount(type == TransactionType.SELL ? amount : null);
        t.setStatus("COMPLETED");
        transactionRepository.save(t);
    }

    public List<TransactionResponse> getHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please log in again."));

        return transactionRepository.findByRequesterOrOwnerOrderByCompletedAtDesc(user, user).stream()
                .map(t -> toResponse(t, userId)).toList();
    }

    private TransactionResponse toResponse(Transaction t, Long viewerUserId) {
        TransactionResponse dto = new TransactionResponse();
        dto.setId(t.getId());
        dto.setListingName(t.getListing().getName());
        dto.setType(t.getType().name());
        dto.setAmount(t.getAmount());
        dto.setStatus(t.getStatus());
        dto.setCompletedAt(t.getCompletedAt().format(FMT));

        boolean viewerIsOwner = t.getOwner().getId().equals(viewerUserId);
        dto.setRole(viewerIsOwner ? "Listed by you" : "Requested by you");
        dto.setOtherPartyName(viewerIsOwner ? t.getRequester().getFullName() : t.getOwner().getFullName());
        return dto;
    }
}
