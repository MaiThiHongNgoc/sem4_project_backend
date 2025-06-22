package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    List<Attendance> findByActiveStatus(Attendance.ActiveStatus status);
}