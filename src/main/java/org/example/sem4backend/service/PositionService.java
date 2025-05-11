package org.example.sem4backend.service;

import org.example.sem4backend.dto.request.PositionRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.PositionResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PositionService {

    private static final Logger logger = LoggerFactory.getLogger(PositionService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<PositionResponse> getPositions(String status) {
        logger.info("Fetching positions with status={}", status);

        String query = "CALL sp_get_all_positions()";

        List<PositionResponse> allPositions = jdbcTemplate.query(
                query,
                (rs, rowNum) -> new PositionResponse(
                        UUID.fromString(rs.getString("position_id")),
                        rs.getString("position_name"),
                        rs.getString("status")
                )
        );

        if (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive"))) {
            allPositions = allPositions.stream()
                    .filter(p -> p.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        logger.info("Total positions fetched: {}", allPositions.size());
        return allPositions;
    }

    public ApiResponse<PositionResponse> addPosition(PositionRequest request) {
        try {
            jdbcTemplate.update(
                    "CALL sp_add_position(?)",
                    request.getPositionName()
            );
            PositionResponse response = new PositionResponse(UUID.randomUUID(), request.getPositionName(), "Active");
            return ApiResponse.success(ErrorCode.SUCCESS, response);
        } catch (Exception e) {
            logger.error("Error adding position: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi thêm position: " + e.getMessage());
        }
    }

    public ApiResponse<PositionResponse> updatePosition(UUID positionId, PositionRequest request) {
        try {
            jdbcTemplate.update(
                    "CALL sp_update_position(?, ?)",
                    positionId.toString(),
                    request.getPositionName()
            );
            PositionResponse response = new PositionResponse(positionId, request.getPositionName(), "Active");
            return ApiResponse.success(ErrorCode.SUCCESS, response);
        } catch (Exception e) {
            logger.error("Error updating position: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi cập nhật position: " + e.getMessage());
        }
    }

    public ApiResponse<Void> deletePosition(UUID positionId) {
        try {
            jdbcTemplate.update(
                    "CALL sp_delete_position(?)",
                    positionId.toString()
            );
            return ApiResponse.success(ErrorCode.SUCCESS);
        } catch (Exception e) {
            logger.error("Error deleting position: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi xóa position: " + e.getMessage());
        }
    }
}