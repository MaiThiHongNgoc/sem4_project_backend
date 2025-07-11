package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, String> {
    List<Leave> findByEmployee_EmployeeId(String employeeId);
    List<Leave> findByEmployee_EmployeeIdAndStatus(String employeeId, Leave.LeaveStatus status);
}
