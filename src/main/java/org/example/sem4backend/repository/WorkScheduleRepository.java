package org.example.sem4backend.repository;

import org.example.sem4backend.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, String> {
    @Query("SELECT w FROM WorkSchedule w WHERE w.employee.employeeId = :empId AND w.workDay = :workDay AND w.status = 'Active'")
    List<WorkSchedule> findByEmployeeAndWorkDay(@Param("empId") String empId, @Param("workDay") LocalDate workDay);


    @Query("SELECT w FROM WorkSchedule w WHERE w.employee.employeeId = :empId AND w.scheduleInfo.scheduleInfoId = :infoId AND w.workDay = :workDay")
    Optional<WorkSchedule> findDuplicateSchedule(
            @Param("empId") String empId,
            @Param("infoId") String infoId,
            @Param("workDay") LocalDate workDay
    );


    @Query("SELECT ws FROM WorkSchedule ws JOIN FETCH ws.scheduleInfo WHERE ws.employee.employeeId = :empId AND ws.workDay = :date")
    List<WorkSchedule> findByEmployeeIdAndWorkDay(@Param("empId") String empId, @Param("date") LocalDate date);

    @Query("SELECT w FROM WorkSchedule w WHERE w.employee.employeeId = :empId AND w.workDay = :workDay AND w.isApproved = true")
    List<WorkSchedule> findApprovedSchedulesByEmployeeAndDate(@Param("empId") String empId, @Param("workDay") LocalDate workDay);

    @Query("SELECT w FROM WorkSchedule w " +
            "WHERE (:empId IS NULL OR w.employee.employeeId = :empId) " +
            "AND (:fromDate IS NULL OR w.workDay >= :fromDate) " +
            "AND (:toDate IS NULL OR w.workDay <= :toDate)")
    List<WorkSchedule> findByEmployeeAndDateRange(
            @Param("empId") String empId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("SELECT ws FROM WorkSchedule ws " +
            "WHERE ws.employee.employeeId = :employeeId " +
            "AND ws.workDay = :workDay " +
            "AND ws.status = 'Active'")
    List<WorkSchedule> findValidSchedulesForAttendance(@Param("employeeId") String employeeId,
                                                       @Param("workDay") LocalDate workDay);





}