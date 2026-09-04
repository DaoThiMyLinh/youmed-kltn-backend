package com.youmed.service.impl;

import com.youmed.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private String fromEmail = "hatuhy28102004@gmail.com";

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "YouMed - Xác nhận đăng ký tài khoản";
        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #f9f9f9;\">"
                + "<div style=\"text-align: center; margin-bottom: 20px;\">"
                + "<h1 style=\"color: #2563eb; margin: 0;\">YOUMED</h1>"
                + "<p style=\"color: #64748b; margin-top: 5px;\">Nền tảng Đặt khám Trực tuyến</p>"
                + "</div>"
                + "<div style=\"background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05);\">"
                + "<h2 style=\"color: #1e293b; margin-top: 0;\">Xin chào!</h2>"
                + "<p style=\"color: #334155; line-height: 1.6;\">Cảm ơn bạn đã đăng ký tài khoản tại YouMed. Để hoàn tất quá trình đăng ký, vui lòng sử dụng mã xác nhận gồm 6 chữ số dưới đây:</p>"
                + "<div style=\"text-align: center; margin: 30px 0;\">"
                + "<span style=\"display: inline-block; font-size: 32px; font-weight: bold; color: #2563eb; letter-spacing: 5px; padding: 15px 30px; background-color: #eff6ff; border-radius: 8px; border: 2px dashed #bfdbfe;\">"
                + otp
                + "</span>"
                + "</div>"
                + "<p style=\"color: #334155; line-height: 1.6;\">Mã này sẽ hết hạn sau <strong>5 phút</strong>. Vui lòng không chia sẻ mã này cho bất kỳ ai để bảo mật tài khoản của bạn.</p>"
                + "</div>"
                + "<div style=\"text-align: center; margin-top: 20px; color: #94a3b8; font-size: 12px;\">"
                + "<p>© 2026 YouMed Platform. All rights reserved.</p>"
                + "</div>"
                + "</div>";
                
        sendEmailViaBrevo(toEmail, subject, htmlContent);
    }

    @Override
    public void sendForgotPasswordEmail(String toEmail, String otp) {
        String subject = "YouMed - Lấy lại mật khẩu";
        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #f9f9f9;\">"
                + "<div style=\"text-align: center; margin-bottom: 20px;\">"
                + "<h1 style=\"color: #ef4444; margin: 0;\">YOUMED</h1>"
                + "<p style=\"color: #64748b; margin-top: 5px;\">Yêu cầu Khôi phục Mật khẩu</p>"
                + "</div>"
                + "<div style=\"background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05);\">"
                + "<h2 style=\"color: #1e293b; margin-top: 0;\">Xin chào!</h2>"
                + "<p style=\"color: #334155; line-height: 1.6;\">Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản YouMed của bạn. Vui lòng sử dụng mã xác nhận dưới đây để tiếp tục:</p>"
                + "<div style=\"text-align: center; margin: 30px 0;\">"
                + "<span style=\"display: inline-block; font-size: 32px; font-weight: bold; color: #ef4444; letter-spacing: 5px; padding: 15px 30px; background-color: #fef2f2; border-radius: 8px; border: 2px dashed #fca5a5;\">"
                + otp
                + "</span>"
                + "</div>"
                + "<p style=\"color: #334155; line-height: 1.6;\">Mã này sẽ hết hạn sau <strong>5 phút</strong>. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                + "</div>"
                + "<div style=\"text-align: center; margin-top: 20px; color: #94a3b8; font-size: 12px;\">"
                + "<p>© 2026 YouMed Platform. All rights reserved.</p>"
                + "</div>"
                + "</div>";
                
        sendEmailViaBrevo(toEmail, subject, htmlContent);
    }
    
    private void sendEmailViaBrevo(String toEmail, String subject, String htmlContent) {
        if (brevoApiKey == null || brevoApiKey.isEmpty()) {
            System.err.println("BREVO_API_KEY is not set. Cannot send email.");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = Map.of(
            "sender", Map.of("name", "YouMed System", "email", fromEmail),
            "to", List.of(Map.of("email", toEmail)),
            "subject", subject,
            "htmlContent", htmlContent
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Email sent successfully: " + response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email qua Brevo API: " + e.getMessage());
        }
    }
}
