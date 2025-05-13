package org.example.sem4backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.example.sem4backend.dto.response.ApiResponse;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.repository.query.QueryCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, errorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKeyException(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, "Dữ liệu bị trùng lặp: " + ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException
                ? "Vi phạm ràng buộc dữ liệu: " + ex.getCause().getMessage()
                : "Dữ liệu không hợp lệ: " + ex.getMessage();
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(BeanCreationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBeanCreationException(BeanCreationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi khởi tạo bean: " + ex.getMessage()));
    }

    @ExceptionHandler(QueryCreationException.class)
    public ResponseEntity<ApiResponse<Void>> handleQueryCreationException(QueryCreationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi tạo truy vấn: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // nên tắt khi production
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi hệ thống không xác định"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng"));
    }


}
