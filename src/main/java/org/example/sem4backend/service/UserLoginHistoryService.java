package org.example.sem4backend.service;

import org.example.sem4backend.entity.UserLoginHistory;

import java.util.List;

public interface UserLoginHistoryService {
    List<UserLoginHistory> getByUsername(String username);
}
