package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    List<Attendance> findByActiveStatus(Attendance.ActiveStatus status);
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee WHERE a.activeStatus = :status")
    List<Attendance> findActiveWithEmployee(Attendance.ActiveStatus status);
}