package org.example.sem4backend.controller;


import jakarta.validation.Valid;
import org.example.sem4backend.dto.request.PositionRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.PositionResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.PositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private static final Logger logger = LoggerFactory.getLogger(PositionController.class);

    @Autowired
    private PositionService positionService;

    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getPositions(
            @RequestParam(required = false) String status
    ) {
        List<PositionResponse> positions = positionService.getPositions(status);
        return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, positions), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponse>> addPosition(
            @RequestBody @Valid PositionRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        ApiResponse<PositionResponse> response = positionService.addPosition(request);
        HttpStatus status = response.getErrorCode() == ErrorCode.SUCCESS ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @PutMapping("/{positionId}")
    public ResponseEntity<ApiResponse<PositionResponse>> updatePosition(
            @PathVariable UUID positionId,
            @RequestBody @Valid PositionRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        ApiResponse<PositionResponse> response = positionService.updatePosition(positionId, request);
        HttpStatus status = response.getErrorCode() == ErrorCode.SUCCESS ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @DeleteMapping("/{positionId}")
    public ResponseEntity<ApiResponse<Void>> deletePosition(@PathVariable UUID positionId) {
           ApiResponse<Void> response = positionService.deletePosition(positionId);
        HttpStatus status = response.getErrorCode() == ErrorCode.SUCCESS ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }
}