package com.uniswap.controller;

import com.uniswap.dto.RequestCreateRequest;
import com.uniswap.dto.RequestResponse;
import com.uniswap.service.RequestService;
import com.uniswap.util.AuthHelper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;
    private final AuthHelper authHelper;

    public RequestController(RequestService requestService, AuthHelper authHelper) {
        this.requestService = requestService;
        this.authHelper = authHelper;
    }

    @PostMapping
    public RequestResponse create(@RequestHeader("Authorization") String authHeader,
                                   @Valid @RequestBody RequestCreateRequest request) {
        return requestService.create(authHelper.requireUserId(authHeader), request);
    }

    @GetMapping("/incoming")
    public List<RequestResponse> incoming(@RequestHeader("Authorization") String authHeader) {
        return requestService.getIncoming(authHelper.requireUserId(authHeader));
    }

    @GetMapping("/outgoing")
    public List<RequestResponse> outgoing(@RequestHeader("Authorization") String authHeader) {
        return requestService.getOutgoing(authHelper.requireUserId(authHeader));
    }

    @PutMapping("/{id}/accept")
    public RequestResponse accept(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return requestService.accept(authHelper.requireUserId(authHeader), id);
    }

    @PutMapping("/{id}/reject")
    public RequestResponse reject(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return requestService.reject(authHelper.requireUserId(authHeader), id);
    }

    @PutMapping("/{id}/cancel")
    public RequestResponse cancel(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return requestService.cancel(authHelper.requireUserId(authHeader), id);
    }
}
