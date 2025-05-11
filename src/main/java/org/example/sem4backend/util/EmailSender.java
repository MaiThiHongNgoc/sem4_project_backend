package org.example.sem4backend.util;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {
    private final JavaMailSender javaMailSender;

    public EmailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xác thực tài khoản - Mã OTP của bạn");

            String content = """
                <html>
                <body style="margin: 0; padding: 0; background-color: #ffffff; font-family: Arial, sans-serif;">
                    <table width="100%%" style="background-color: #ffffff;">
                        <tr>
                            <td align="center" style="padding: 30px 0; background-color: #0d6efd;">
                                <h1 style="color: white; margin: 0;">HỆ THỐNG NHÂN SỰ</h1>
                            </td>
                        </tr>
                        <tr>
                            <td align="center">
                                <table width="600" style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 30px; background-color: #ffffff;">
                                    <tr>
                                        <td style="text-align: left; color: #333;">
                                            <h2 style="color: #0d6efd;">Mã xác thực OTP</h2>
                                            <p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu từ địa chỉ email sau:</p>
                                            <p style="font-weight: bold; color: #000;">%s</p>
                                            <p>Vui lòng sử dụng mã OTP bên dưới để tiếp tục:</p>
                                            <div style="text-align: center; margin: 30px 0;">
                                                <span style="font-size: 32px; font-weight: bold; color: #0d6efd;">%s</span>
                                            </div>
                                            <p>Mã OTP sẽ hết hạn sau <strong>10 phút</strong>.</p>
                                            <p>Nếu bạn không yêu cầu, vui lòng bỏ qua email này hoặc liên hệ với bộ phận hỗ trợ.</p>
                                            <hr style="margin: 30px 0;">
                                            <p style="font-size: 12px; color: #999;">Lưu ý: Không chia sẻ mã OTP này cho bất kỳ ai. Việc lộ mã OTP có thể dẫn đến rủi ro bảo mật tài khoản của bạn.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td align="center" style="padding: 20px 0; background-color: #f1f1f1; font-size: 12px; color: #777;">
                                © 2025 Hệ thống quản lý nhân sự. Mọi quyền được bảo lưu.
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(toEmail, otpCode);

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }

}
