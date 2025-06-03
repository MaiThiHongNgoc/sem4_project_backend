package org.example.sem4backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.User;
import org.example.sem4backend.entity.UserLoginHistory;
import org.example.sem4backend.exception.AppException;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.UserLoginHistoryRepository;
import org.example.sem4backend.repository.UserRepository;
import org.example.sem4backend.service.UserLoginHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLoginHistoryServiceImpl implements UserLoginHistoryService {

    private final UserLoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserLoginHistory> getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return loginHistoryRepository.findByUser(user);
    }
}
