package com.uniswap.controller;

import com.uniswap.dto.TransactionResponse;
import com.uniswap.service.TransactionService;
import com.uniswap.util.AuthHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthHelper authHelper;

    public TransactionController(TransactionService transactionService, AuthHelper authHelper) {
        this.transactionService = transactionService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public List<TransactionResponse> history(@RequestHeader("Authorization") String authHeader) {
        return transactionService.getHistory(authHelper.requireUserId(authHeader));
    }
}
