package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    @Query(value = "SELECT * FROM employees", nativeQuery = true)
    List<Employee> findAllEmployeeNative();
}
