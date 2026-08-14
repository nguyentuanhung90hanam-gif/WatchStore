package com.watchstore.service;

import com.watchstore.model.OtpRecord;
import com.watchstore.repository.OtpRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

public class OtpService {
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int MAX_RESEND_LIMIT = 5;

    private final OtpRepository repository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public static class OtpResult {
        private final boolean success;
        private final String message;
        private final int remainingSeconds;

        public OtpResult(boolean success, String message) {
            this(success, message, 0);
        }

        public OtpResult(boolean success, String message, int remainingSeconds) {
            this.success = success;
            this.message = message;
            this.remainingSeconds = remainingSeconds;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getRemainingSeconds() { return remainingSeconds; }
    }

    public OtpResult generateAndSendOtp(String email, String purpose) {
        if (email == null || email.isBlank()) {
            return new OtpResult(false, "Email không hợp lệ.");
        }

        String cleanEmail = email.trim().toLowerCase();
        String cleanPurpose = purpose == null ? "REGISTER" : purpose.trim().toUpperCase();

        OtpRecord existing = repository.findLatest(cleanEmail, cleanPurpose);
        if (existing != null && !existing.isVerified()) {
            LocalDateTime now = LocalDateTime.now();
            long secondsSinceLastSent = Duration.between(existing.getLastSentAt(), now).getSeconds();
            if (secondsSinceLastSent < RESEND_COOLDOWN_SECONDS) {
                int wait = (int) (RESEND_COOLDOWN_SECONDS - secondsSinceLastSent);
                return new OtpResult(false, "Vui lòng đợi " + wait + " giây trước khi thử gửi lại mã OTP.", wait);
            }
            if (existing.getResendCount() >= MAX_RESEND_LIMIT) {
                return new OtpResult(false, "Bạn đã vượt quá số lần yêu cầu gửi mã OTP. Vui lòng thử lại sau.");
            }
        }

        // Invalidate old OTPs for this email and purpose
        repository.invalidate(cleanEmail, cleanPurpose);

        // Generate 6-digit random code
        int code = 100000 + random.nextInt(900000);
        String otpStr = String.valueOf(code);

        OtpRecord newRecord = new OtpRecord();
        newRecord.setEmail(cleanEmail);
        newRecord.setPurpose(cleanPurpose);
        newRecord.setOtpHash(repository.hashOtp(otpStr));
        LocalDateTime now = LocalDateTime.now();
        newRecord.setExpiresAt(now.plusMinutes(OTP_EXPIRE_MINUTES));
        newRecord.setAttempts(0);
        newRecord.setVerified(false);
        newRecord.setLastSentAt(now);
        newRecord.setResendCount(existing == null ? 1 : existing.getResendCount() + 1);

        repository.save(newRecord);

        boolean sent = emailService.sendOtpEmail(cleanEmail, otpStr, cleanPurpose);
        if (!sent) {
            return new OtpResult(false, "Không thể gửi email OTP đến Gmail: " + cleanEmail + ". Vui lòng kiểm tra lại kết nối mạng hoặc cấu hình biến môi trường MAIL_USERNAME & MAIL_PASSWORD (App Password) trên Server.");
        }

        return new OtpResult(true, "Mã OTP đã được gửi đến email " + cleanEmail);
    }

    public OtpResult verifyOtp(String email, String purpose, String inputOtp) {
        if (email == null || email.isBlank()) {
            return new OtpResult(false, "Email không hợp lệ.");
        }
        if (inputOtp == null || !inputOtp.trim().matches("\\d{6}")) {
            return new OtpResult(false, "Mã OTP phải gồm đúng 6 chữ số.");
        }

        String cleanEmail = email.trim().toLowerCase();
        String cleanPurpose = purpose == null ? "REGISTER" : purpose.trim().toUpperCase();

        OtpRecord record = repository.findLatest(cleanEmail, cleanPurpose);
        if (record == null || record.isVerified()) {
            return new OtpResult(false, "Mã OTP không tồn tại hoặc đã được sử dụng. Vui lòng gửi mã mới.");
        }

        if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
            return new OtpResult(false, "Mã OTP đã hết hạn. Vui lòng bấm 'Gửi lại OTP' để nhận mã mới.");
        }

        if (record.getAttempts() >= MAX_VERIFY_ATTEMPTS) {
            repository.invalidate(cleanEmail, cleanPurpose);
            return new OtpResult(false, "Bạn đã nhập sai quá 5 lần. Mã OTP này đã bị vô hiệu hóa. Vui lòng yêu cầu mã mới.");
        }

        String inputHash = repository.hashOtp(inputOtp.trim());
        if (!inputHash.equalsIgnoreCase(record.getOtpHash())) {
            record.setAttempts(record.getAttempts() + 1);
            repository.update(record);
            int remain = MAX_VERIFY_ATTEMPTS - record.getAttempts();
            return new OtpResult(false, "Mã OTP không chính xác. Bạn còn " + remain + " lần thử.");
        }

        // OTP is correct! Mark verified and invalidate to prevent reuse.
        record.setVerified(true);
        repository.update(record);
        repository.invalidate(cleanEmail, cleanPurpose);

        return new OtpResult(true, "Xác thực OTP thành công!");
    }
}
