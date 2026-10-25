package com.uniswap.repository;

import com.uniswap.model.Request;
import com.uniswap.model.RequestStatus;
import com.uniswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    // Incoming: requests on listings owned by this user.
    List<Request> findByListing_OwnerOrderByCreatedAtDesc(User owner);

    // Outgoing: requests this user sent to other owners.
    List<Request> findByRequesterOrderByCreatedAtDesc(User requester);

    long countByListing_OwnerAndStatus(User owner, RequestStatus status);
}
