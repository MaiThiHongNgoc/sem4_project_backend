package org.example.sem4backend.repository;

import jakarta.persistence.LockModeType;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {
       Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username")
    Optional<User> findByUsernameWithRole(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findByEmailWithLock(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    int updatePassword(@Param("email") String email, @Param("password") String password);

    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName AND u.status = 'Active' ORDER BY u.createdAt DESC")
    List<User> findByRoleNameOrderByCreatedAtDesc(@Param("roleName") String roleName, Pageable pageable);

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllUserNative();

}