package org.example.sem4backend.repository;

import org.example.sem4backend.entity.User;
import org.example.sem4backend.entity.UserLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, String> {
    List<UserLoginHistory> findByUser(User user);
}
