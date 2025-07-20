package org.example.sem4backend.repository;

import org.example.sem4backend.entity.QRInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface QRInfoRepository extends JpaRepository<QRInfo, String>, JpaSpecificationExecutor<QRInfo> {
    List<QRInfo> findByStatus(QRInfo.Status status);
    List<QRInfo> findByQrCodeContainingIgnoreCaseOrLocationNameContainingIgnoreCaseOrShiftOrStatus(
            String qrCode, String locationName, QRInfo.Shift shift, QRInfo.Status status
    );

    Optional<QRInfo> findById(String qrInfoId);
    Optional<QRInfo> findTop1ByStatusOrderByCreatedAtDesc(QRInfo.Status status);
    Optional<QRInfo> findByQrCode(String qrCode);

    @Query("SELECT q FROM QRInfo q " +
            "WHERE (:status IS NULL OR q.status = :status) " +
            "AND (:startDate IS NULL OR q.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR q.createdAt <= :endDate)")
    List<QRInfo> filterByStatusAndDateRange(
            @Param("status") QRInfo.Status status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);




}