package org.example.sem4backend.repository;

import org.example.sem4backend.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, String> {}