package com.watchstore.model;

import java.time.LocalDateTime;

public class OtpRecord {
    private long id;
    private String email;
    private String purpose;
    private String otpHash;
    private LocalDateTime expiresAt;
    private int attempts;
    private boolean verified;
    private LocalDateTime lastSentAt;
    private int resendCount;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public LocalDateTime getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(LocalDateTime lastSentAt) { this.lastSentAt = lastSentAt; }
    public int getResendCount() { return resendCount; }
    public void setResendCount(int resendCount) { this.resendCount = resendCount; }
}
