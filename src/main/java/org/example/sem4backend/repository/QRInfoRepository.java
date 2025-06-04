package org.example.sem4backend.repository;

import org.example.sem4backend.entity.QRInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface QRInfoRepository extends JpaRepository<QRInfo, String>, JpaSpecificationExecutor<QRInfo> {
    List<QRInfo> findByStatus(QRInfo.Status status);
    List<QRInfo> findByQrCodeContainingIgnoreCaseOrLocationNameContainingIgnoreCaseOrShiftOrStatus(
            String qrCode, String locationName, QRInfo.Shift shift, QRInfo.Status status
    );

    Optional<QRInfo> findById(String qrInfoId);
}
