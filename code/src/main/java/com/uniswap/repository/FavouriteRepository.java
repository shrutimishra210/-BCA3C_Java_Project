package com.uniswap.repository;

import com.uniswap.model.Favourite;
import com.uniswap.model.Listing;
import com.uniswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    List<Favourite> findByUserOrderByCreatedAtDesc(User user);
    Optional<Favourite> findByUserAndListing(User user, Listing listing);
    boolean existsByUserAndListing(User user, Listing listing);
    long countByUser(User user);
}
