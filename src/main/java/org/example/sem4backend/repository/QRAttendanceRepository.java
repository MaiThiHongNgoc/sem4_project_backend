package org.example.sem4backend.repository;

import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.QRAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface QRAttendanceRepository extends JpaRepository<QRAttendance, String> {
    List<QRAttendance> findByActiveStatus(QRAttendance.ActiveStatus activeStatus);
    List<QRAttendance> findByEmployeeAndAttendanceDate(Employee employee, Date attendanceDate);


}
