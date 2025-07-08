package org.example.sem4backend.repository;

import org.example.sem4backend.entity.AttendanceAppeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceAppealRepository extends JpaRepository<AttendanceAppeal, String> {
    List<AttendanceAppeal> findByEmployee_EmployeeId(String employeeId);
}