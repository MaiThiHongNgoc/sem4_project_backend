package org.example.sem4backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.sem4backend.exception.ErrorCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    int code;
    String message;
    T result;
    private ErrorCode errorCode;

    public static <T> ApiResponse<T> success(ErrorCode errorCode, T result) {
        return ApiResponse.<T>builder()
                .code(errorCode.getStatus().value())
                .message(errorCode.getMessage())
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success(ErrorCode code) {
        return success(code, null);
    }

    public boolean isSuccess() {
        return errorCode == ErrorCode.SUCCESS;
    }

    // Phương thức error
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String errorMessage) {
        return ApiResponse.<T>builder()
                .code(errorCode.getStatus().value())
                .message(errorMessage)
                .result(null)
                .build();
    }
}