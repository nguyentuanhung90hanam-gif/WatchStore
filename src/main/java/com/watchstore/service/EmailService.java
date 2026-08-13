package com.watchstore.service;

import com.watchstore.config.MailConfig;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;

public class EmailService {

    public boolean sendOtpEmail(String toEmail, String otpCode, String purpose) {
        if (!MailConfig.isConfigured()) {
            System.err.println("[WARN MailConfig] MAIL_USERNAME or MAIL_PASSWORD environment variables not set. Cannot send real OTP email.");
            return false;
        }

        try {
            Session session = MailConfig.createMailSession();
            MimeMessage msg = new MimeMessage(session);

            String fromAddress = MailConfig.FROM != null && !MailConfig.FROM.isBlank() ? MailConfig.FROM : MailConfig.USERNAME;
            msg.setFrom(new InternetAddress(fromAddress, "WatchStore Vietnam"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSentDate(new Date());

            String subject = "FORGOT_PASSWORD".equalsIgnoreCase(purpose) 
                    ? "Mã OTP khôi phục mật khẩu - WatchStore" 
                    : "Mã OTP xác thực đăng ký tài khoản - WatchStore";

            msg.setSubject(subject, "UTF-8");

            String actionName = "FORGOT_PASSWORD".equalsIgnoreCase(purpose) 
                    ? "khôi phục mật khẩu" 
                    : "đăng ký tài khoản WatchStore";

            String htmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; background-color: #ffffff;">
                    <div style="text-align: center; padding-bottom: 20px; border-bottom: 2px solid #d4af37;">
                        <h2 style="color: #111111; margin: 0; letter-spacing: 2px;">WATCHSTORE VIETNAM</h2>
                        <p style="color: #777777; font-size: 12px; margin-top: 5px; text-transform: uppercase;">Đồng Hồ Chính Hãng & Thời Trang</p>
                    </div>
                    <div style="padding: 20px 10px;">
                        <p style="font-size: 15px; color: #333333;">Xin chào,</p>
                        <p style="font-size: 15px; color: #333333; line-height: 1.5;">Bạn đang thực hiện yêu cầu <strong>%s</strong>. Mã OTP của bạn là:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #111111; background: #f7f7f7; padding: 12px 24px; border: 1px dashed #d4af37; border-radius: 6px; display: inline-block;">
                                %s
                            </span>
                        </div>
                        <p style="font-size: 14px; color: #e53935; font-weight: bold;">Mã OTP này có hiệu lực trong 5 phút và chỉ được dùng 1 lần.</p>
                        <p style="font-size: 13px; color: #666666;">Vì lý do bảo mật, tuyệt đối không chia sẻ mã này cho bất kỳ ai (kể cả nhân viên WatchStore).</p>
                    </div>
                    <div style="text-align: center; padding-top: 20px; border-top: 1px solid #eeeeee; color: #999999; font-size: 12px;">
                        <p style="margin: 0;">Trân trọng,<br>Đội ngũ hỗ trợ WatchStore</p>
                    </div>
                </div>
                """.formatted(actionName, otpCode);

            msg.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR EmailService] Failed sending OTP email to " + toEmail + ": " + e.getMessage());
            return false;
        }
    }
}
