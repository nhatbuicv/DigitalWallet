package com.nhatbui.digital_wallet.service.notification.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.service.auth.OtpService;
import com.nhatbui.digital_wallet.service.notification.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {

    JavaMailSender javaMailSender;
    private final SecurityUtils securityUtils;
    private final OtpService otpService;
    private final UserRepository userRepository;
    // gửi link reset password

    @Override
    public void sendLinkResetPassword (String email , String token){
        String link = "http://localhost:3000/reset-password?token=" + token;

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(email);
            helper.setSubject("Yêu cầu đặt lại mật khẩu - StartupKit");

            // Nội dung HTML cho email
            String htmlContent = String.format(
                    "<h3>Chào bạn,</h3>" +
                            "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>" +
                            "<p>Vui lòng click vào nút bên dưới để tiến hành thay đổi:</p>" +
                            "<a href='%s' style='background-color: #1976d2; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Đặt lại mật khẩu</a>" +
                            "<p>Link này sẽ hết hạn sau 15 phút.</p>", link);

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        }
    }

    @Override
    public void sendToEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var otp = otpService.generateOtp(user.getEmail());

        sendOtpEmail(email, user.getFullName(), otp);
    }

    @Override
    public void sendOtpEmail(String to, String fullName, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Digital Wallet - OTP Verification");

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                    </head>
                    <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                    
                    <table width="100%%" cellspacing="0" cellpadding="20">
                        <tr>
                            <td align="center">
                    
                                <table width="600" style="background:#ffffff;border-radius:10px;padding:40px;">
                    
                                    <tr>
                                        <td align="center">
                                            <h2 style="color:#0d6efd;margin-bottom:20px;">
                                                Digital Wallet
                                            </h2>
                    
                                            <p style="font-size:16px;">
                                                Xin chào <strong>%s</strong>,
                                            </p>
                    
                                            <p style="font-size:15px;color:#555;">
                                                Bạn vừa yêu cầu xác thực tài khoản.
                                                Vui lòng sử dụng mã OTP bên dưới.
                                            </p>
                    
                                            <div style="
                                                display:inline-block;
                                                background:#0d6efd;
                                                color:white;
                                                padding:15px 35px;
                                                border-radius:8px;
                                                font-size:30px;
                                                font-weight:bold;
                                                letter-spacing:8px;
                                                margin:25px 0;">
                                                %s
                                            </div>
                    
                                            <p style="font-size:15px;">
                                                Mã OTP có hiệu lực trong
                                                <strong>5 phút</strong>.
                                            </p>
                    
                                            <p style="color:red;font-size:14px;">
                                                Không chia sẻ mã này với bất kỳ ai.
                                            </p>
                    
                                            <hr>
                    
                                            <p style="font-size:12px;color:#999;">
                                                Nếu bạn không thực hiện yêu cầu này,
                                                vui lòng bỏ qua email.
                                            </p>
                    
                                        </td>
                                    </tr>
                    
                                </table>
                    
                            </td>
                        </tr>
                    </table>
                    
                    </body>
                    </html>
                    """.formatted(fullName, otp);

            helper.setText(html, true);

            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP.", e);
        }
    }
}
