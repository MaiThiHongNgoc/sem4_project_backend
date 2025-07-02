package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.QRAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface QRAttendanceRepository extends JpaRepository<QRAttendance, String> {
    List<QRAttendance> findByActiveStatus(QRAttendance.ActiveStatus activeStatus);
    List<QRAttendance> findByEmployeeAndAttendanceDate(Employee employee, Date attendanceDate);

    List<QRAttendance> findByEmployee_EmployeeIdAndAttendanceDate(String employeeId, Date date);

    @Query("SELECT q FROM QRAttendance q WHERE q.attendanceDate BETWEEN :start AND :end AND q.activeStatus = 'Active'")
    List<QRAttendance> findAllByDateRange(Date start, Date end);

    @Query("SELECT q FROM QRAttendance q JOIN FETCH q.employee")
    List<QRAttendance> findAllWithEmployee();




}
