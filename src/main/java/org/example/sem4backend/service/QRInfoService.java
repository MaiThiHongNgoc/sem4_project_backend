package org.example.sem4backend.service;

import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.Location;
import org.example.sem4backend.entity.QRInfo;
import org.example.sem4backend.entity.User;
import org.example.sem4backend.repository.LocationRepository;
import org.example.sem4backend.repository.QRInfoRepository;
import org.example.sem4backend.repository.UserRepository;
import org.example.sem4backend.util.QRCodeGenerator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
@RequiredArgsConstructor
public class QRInfoService {

    private final QRInfoRepository qrInfoRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(QRInfoService.class);

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void rotateQRCode() {
        try {
            // 1. Đặt tất cả mã QR đang hoạt động thành INACTIVE
            List<QRInfo> activeQRCodes = qrInfoRepository.findByStatus(QRInfo.Status.ACTIVE);
            for (QRInfo qr : activeQRCodes) {
                qr.setStatus(QRInfo.Status.INACTIVE);
                qr.setActive(false);
            }
            qrInfoRepository.saveAll(activeQRCodes);
            logger.info("Deactivated {} active QR codes", activeQRCodes.size());

            // 2. Lấy danh sách location
            List<Location> locations = locationRepository.findAll();
            if (locations.isEmpty()) {
                logger.warn("No locations found to rotate QR codes");
                return;
            }

            // 3. Tạo mã QR mới cho từng location
            User admin = userRepository.findByUsername("Admin").orElse(null);
            if (admin == null) {
                logger.error("Admin user not found. Cannot proceed with QR rotation.");
                return;
            }

            for (Location location : locations) {
                UUID qrId = UUID.randomUUID();
                String qrCodeText = "QR-" + qrId + "-" + location.getName();

                QRInfo qrInfo = new QRInfo();
                qrInfo.setQrInfoId(qrId);
                qrInfo.setQrCode(qrCodeText);
                qrInfo.setCreatedAt(new Date());
                qrInfo.setStatus(QRInfo.Status.ACTIVE);
                qrInfo.setActive(true);
                qrInfo.setLocation(location);
                qrInfo.setShift(determineShift(new Date()));
                qrInfo.setCreatedBy(admin);

                qrInfoRepository.save(qrInfo);

                String filePath = "src/main/resources/static/uploads/qr/" + qrId + ".png";
                QRCodeGenerator.generateQRCodeImage(qrCodeText, filePath);

                logger.info("Generated QR code for location ID {}: {}", location.getLocationId(), qrCodeText);
            }

        } catch (Exception e) {
            logger.error("Error occurred while rotating QR code", e);
        }
    }

    private QRInfo.Shift determineShift(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return QRInfo.Shift.Morning;
        else if (hour >= 12 && hour < 17) return QRInfo.Shift.Afternoon;
        else if (hour >= 17 && hour < 21) return QRInfo.Shift.Evening;
        else return QRInfo.Shift.Night;
    }

    public List<QRInfo> findAll() {
        return qrInfoRepository.findAll();
    }

    public QRInfo findById(UUID id) {
        return qrInfoRepository.findById(id).orElse(null);
    }

    public QRInfo update(UUID id, QRInfo updatedQR) {
        QRInfo existing = findById(id);
        if (existing == null) return null;

        existing.setDescription(updatedQR.getDescription());
        existing.setLocation(updatedQR.getLocation());
        existing.setShift(updatedQR.getShift());
        existing.setExpiredAt(updatedQR.getExpiredAt());
        existing.setDeviceInfo(updatedQR.getDeviceInfo());
        existing.setStatus(updatedQR.getStatus());
        return qrInfoRepository.save(existing);
    }

    public boolean softDelete(UUID id) {
        QRInfo qr = findById(id);
        if (qr != null) {
            qr.setStatus(QRInfo.Status.INACTIVE);
            qrInfoRepository.save(qr);
            return true;
        }
        return false;
    }

    public List<QRInfo> search(String keyword) {
        return qrInfoRepository.findByQrCodeContainingIgnoreCaseOrLocationNameContainingIgnoreCaseOrShiftOrStatus(
                keyword, keyword, tryParseShift(keyword), tryParseStatus(keyword)
        );
    }

    private QRInfo.Shift tryParseShift(String keyword) {
        try {
            return QRInfo.Shift.valueOf(keyword.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private QRInfo.Status tryParseStatus(String keyword) {
        try {
            return QRInfo.Status.valueOf(keyword.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
