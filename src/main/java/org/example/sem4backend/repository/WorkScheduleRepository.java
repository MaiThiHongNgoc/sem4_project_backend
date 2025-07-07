package org.example.sem4backend.repository;

import org.example.sem4backend.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, String> {
    @Query("SELECT w FROM WorkSchedule w WHERE w.employee.employeeId = :empId AND w.workDay = :workDay AND w.status = 'Active'")
    Optional<WorkSchedule> findByEmployeeAndWorkDay(@Param("empId") String empId, @Param("workDay") LocalDate workDay);

    @Query("SELECT w FROM WorkSchedule w WHERE w.employee.employeeId = :empId AND w.scheduleInfo.scheduleInfoId = :infoId AND w.workDay = :workDay")
    Optional<WorkSchedule> findDuplicateSchedule(
            @Param("empId") String empId,
            @Param("infoId") String infoId,
            @Param("workDay") java.util.Date workDay
    );

}