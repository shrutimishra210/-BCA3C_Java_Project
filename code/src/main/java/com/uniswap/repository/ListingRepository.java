package com.uniswap.repository;

import com.uniswap.model.Listing;
import com.uniswap.model.ListingStatus;
import com.uniswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByOwnerOrderByCreatedAtDesc(User owner);

    List<Listing> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    List<Listing> findTop8ByStatusOrderByCreatedAtDesc(ListingStatus status);

    long countByImageUrl(String imageUrl);

    long countByOwnerAndStatus(User owner, ListingStatus status);

    // Simple keyword + category search across name/description, used by the
    // Marketplace screen's search bar and category filter together.
    @Query("SELECT l FROM Listing l WHERE l.status = com.uniswap.model.ListingStatus.ACTIVE " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "     OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR l.category.id = :categoryId) " +
           "AND (:type IS NULL OR l.transactionType = :type) " +
           "ORDER BY l.createdAt DESC")
    List<Listing> search(@Param("keyword") String keyword,
                          @Param("categoryId") Long categoryId,
                          @Param("type") com.uniswap.model.TransactionType type);
}
