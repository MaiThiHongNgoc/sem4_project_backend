package org.example.sem4backend.service;

import org.example.sem4backend.dto.request.PositionRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.PositionResponse;
import org.example.sem4backend.entity.Position;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.PositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PositionService {

    private static final Logger logger = LoggerFactory.getLogger(PositionService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PositionRepository positionRepository;

    public List<PositionResponse> getPositions(String status) {
        logger.info("Fetching positions with status={}", status);

        List<Position> positions = positionRepository.findAllPositionNative();

        List<PositionResponse> allResponses = positions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        if (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive"))) {
            allResponses = allResponses.stream()
                    .filter(p -> p.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        logger.info("Total positions fetched: {}", allResponses.size());
        return allResponses;
    }

    public ApiResponse<PositionResponse> getPositionById(UUID positionId) {
        try {
            List<Position> positions = positionRepository.findAllPositionNative();
            Position position = positions.stream()
                    .filter(p -> p.getPositionId().equals(positionId))
                    .findFirst()
                    .orElse(null);

            if (position == null) {
                return ApiResponse.error(ErrorCode.NOT_FOUND, "Không tìm thấy vị trí với ID đã cho");
            }

            return ApiResponse.success(ErrorCode.SUCCESS, mapToResponse(position));
        } catch (Exception e) {
            logger.error("Error fetching position by id: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi khi tìm position: " + e.getMessage());
        }
    }


    public ApiResponse<PositionResponse> addPosition(PositionRequest request) {
        try {
            jdbcTemplate.update("CALL sp_add_position(?)", request.getPositionName());

            List<Position> positions = positionRepository.findAllPositionNative();
            Position latest = positions.stream()
                    .filter(p -> p.getPositionName().equalsIgnoreCase(request.getPositionName()))
                    .findFirst()
                    .orElse(null);

            if (latest == null) {
                throw new IllegalStateException("Không tìm thấy position sau khi thêm");
            }

            return ApiResponse.success(ErrorCode.SUCCESS, mapToResponse(latest));
        } catch (Exception e) {
            logger.error("Lỗi thêm position: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi thêm position: " + e.getMessage());
        }
    }

    public ApiResponse<PositionResponse> updatePosition(String positionId, PositionRequest request) {
        try {
            jdbcTemplate.update("CALL sp_update_position(?, ?)", positionId.toString(), request.getPositionName());

            List<Position> positions = positionRepository.findAllPositionNative();
            Position updated = positions.stream()
                    .filter(p -> p.getPositionId().equals(positionId))
                    .findFirst()
                    .orElse(null);

            if (updated == null) {
                throw new IllegalStateException("Không tìm thấy position sau khi cập nhật");
            }

            return ApiResponse.success(ErrorCode.SUCCESS, mapToResponse(updated));
        } catch (Exception e) {
            logger.error("Lỗi cập nhật position: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi cập nhật position: " + e.getMessage());
        }
    }

    public ApiResponse<Void> deletePosition(String positionId) {
        try {
            logger.info("Gọi stored procedure sp_delete_position với id: {}", positionId);
            jdbcTemplate.update("CALL sp_delete_position(?)", positionId.toString());

            return ApiResponse.success(ErrorCode.SUCCESS);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx && "45000".equals(sqlEx.getSQLState())) {
                logger.warn("Stored procedure báo lỗi logic: {}", sqlEx.getMessage());
                return ApiResponse.error(ErrorCode.NOT_FOUND, sqlEx.getMessage());
            }

            logger.error("Lỗi khi xóa position: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi xóa position: " + e.getMessage());
        }
    }

    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.builder()
                .positionId(position.getPositionId())
                .positionName(position.getPositionName())
                .status(String.valueOf(position.getStatus()))
                .build();
    }
}
