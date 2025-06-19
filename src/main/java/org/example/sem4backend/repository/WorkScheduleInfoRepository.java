package org.example.sem4backend.repository;

import org.example.sem4backend.entity.WorkScheduleInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkScheduleInfoRepository extends JpaRepository<WorkScheduleInfo, String> {
    Optional<WorkScheduleInfo> findByName(String name);
}