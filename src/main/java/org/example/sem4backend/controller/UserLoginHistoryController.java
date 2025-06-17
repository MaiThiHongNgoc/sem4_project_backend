package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.LoginHistoryResponse;
import org.example.sem4backend.entity.UserLoginHistory;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.UserLoginHistoryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/login-history")
@RequiredArgsConstructor
public class UserLoginHistoryController {

    private final UserLoginHistoryService service;

    @GetMapping("/userid")
    public ApiResponse<List<LoginHistoryResponse>> getMyLoginHistory(Authentication authentication) {
        String username = authentication.getName();
        List<UserLoginHistory> historyList = service.getByUsername(username);

        List<LoginHistoryResponse> responseList = historyList.stream()
                .map(h -> LoginHistoryResponse.builder()
                        .loginTime(String.valueOf(h.getLoginTime()))
                        .ipAddress(h.getIpAddress())
                        .deviceInfo(h.getDeviceInfo())
                        .status(h.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(ErrorCode.SUCCESS, responseList);
    }
}
