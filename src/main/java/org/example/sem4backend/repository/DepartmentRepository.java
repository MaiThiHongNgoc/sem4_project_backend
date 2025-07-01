package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    @Query(value = "SELECT * FROM departments", nativeQuery = true)
    List<Department> findAllDepartmentNative();
}