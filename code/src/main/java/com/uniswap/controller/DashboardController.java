package com.uniswap.controller;

import com.uniswap.dto.DashboardResponse;
import com.uniswap.service.DashboardService;
import com.uniswap.util.AuthHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthHelper authHelper;

    public DashboardController(DashboardService dashboardService, AuthHelper authHelper) {
        this.dashboardService = dashboardService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public DashboardResponse dashboard(@RequestHeader("Authorization") String authHeader) {
        return dashboardService.build(authHelper.requireUserId(authHeader));
    }
}
