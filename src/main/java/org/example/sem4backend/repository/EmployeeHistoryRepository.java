package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.EmployeeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeHistoryRepository extends JpaRepository<EmployeeHistory, String> {
    List<EmployeeHistory> findByEmployee(Employee employee);

}
