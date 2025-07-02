package org.example.sem4backend.notification.repository;

import org.example.sem4backend.entity.User;
import org.example.sem4backend.notification.entity.UserFCMToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFCMTokenRepository extends JpaRepository<UserFCMToken, String> {

    @Query("SELECT t.fcmToken FROM UserFCMToken t " +
            "JOIN t.user u " +
            "JOIN u.role r " +
            "WHERE r.roleName IN :roleNames AND u.status = 'Active'")
    List<String> findFcmTokensByRoles(List<String> roleNames);

    @Query("SELECT t FROM UserFCMToken t WHERE t.user = :user AND t.fcmToken = :fcmToken")
    UserFCMToken findByUserAndFcmToken(@Param("user") User user, @Param("fcmToken") String fcmToken);

    @Query("SELECT t.fcmToken FROM UserFCMToken t WHERE t.user.userId = :userId")
    List<String> findFcmTokensByUserId(@Param("userId") String userId);
}