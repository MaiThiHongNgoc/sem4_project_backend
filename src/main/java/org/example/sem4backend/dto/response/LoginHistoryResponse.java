package org.example.sem4backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginHistoryResponse {
    String loginTime;
    String ipAddress;
    String deviceInfo;
    String status;
}
