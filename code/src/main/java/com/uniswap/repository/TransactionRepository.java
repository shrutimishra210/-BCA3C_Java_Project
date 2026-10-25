package com.uniswap.repository;

import com.uniswap.model.Transaction;
import com.uniswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByRequesterOrOwnerOrderByCompletedAtDesc(User requester, User owner);
}
