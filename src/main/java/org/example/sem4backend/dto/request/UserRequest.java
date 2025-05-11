package org.example.sem4backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {

    String employeeId;

    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Pattern(
            regexp = "^(\\p{Lu}\\p{L}+\\s){1,}\\p{Lu}\\p{L}+$",
            message = "Tên đăng nhập phải viết hoa chữ cái đầu và phải có ít nhất hai từ (họ tên đầy đủ)"
    )
    String username;



    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất một chữ cái viết hoa, một chữ số và một ký tự đặc biệt"
    )
    String password;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Địa chỉ email phải hợp lệ")
    String email;

    String roleId;
}