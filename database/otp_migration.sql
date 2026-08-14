-- OTP Verifications table migration script for WatchStore database
IF OBJECT_ID('dbo.OtpVerifications', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.OtpVerifications (
        OtpID BIGINT IDENTITY(1,1) PRIMARY KEY,
        Email VARCHAR(255) NOT NULL,
        OtpHash VARCHAR(64) NOT NULL,
        Purpose VARCHAR(30) NOT NULL, -- 'REGISTER' or 'FORGOT_PASSWORD'
        ExpiresAt DATETIME2 NOT NULL,
        Attempts INT NOT NULL DEFAULT 0,
        Verified BIT NOT NULL DEFAULT 0,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        LastSentAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        ResendCount INT NOT NULL DEFAULT 0
    );

    CREATE INDEX IX_OtpVerifications_Email_Purpose ON dbo.OtpVerifications(Email, Purpose);
END;
GO
