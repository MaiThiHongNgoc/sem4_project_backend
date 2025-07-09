package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    List<Attendance> findByActiveStatus(Attendance.ActiveStatus status);
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee WHERE a.activeStatus = :status")
    List<Attendance> findActiveWithEmployee(Attendance.ActiveStatus status);
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE e.employeeId = :employeeId AND a.activeStatus = 'Active'")
    List<Attendance> findByEmployeeIdWithEmployee(String employeeId);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.employee.employeeId = :employeeId " +
            "AND a.attendanceDate BETWEEN :fromDate AND :toDate")
    List<Attendance> findByEmployeeIdAndDateRange(
            @Param("employeeId") String employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.employee.employeeId = :employeeId " +
            "AND a.attendanceDate BETWEEN :fromDate AND :toDate " +
            "AND a.status = :status")
    List<Attendance> findByEmployeeIdAndDateRangeAndStatus(
            @Param("employeeId") String employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

}