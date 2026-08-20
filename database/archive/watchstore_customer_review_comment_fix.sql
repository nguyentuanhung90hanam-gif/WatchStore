-- ==========================================================
-- Migration: WatchStore Customer Review & Comment Fix
-- Description: Idempotent script to support Comments table
-- ==========================================================

USE WatchStore;
GO

IF OBJECT_ID(N'dbo.ProductComments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ProductComments (
        CommentID       BIGINT IDENTITY(1,1) PRIMARY KEY,
        ProductID       INT NOT NULL,
        UserID          INT NOT NULL,
        Content         NVARCHAR(1000) NOT NULL,
        Status          VARCHAR(20) NOT NULL CONSTRAINT DF_ProductComments_Status DEFAULT 'APPROVED',
        CreatedAt       DATETIME2 NOT NULL CONSTRAINT DF_ProductComments_CreatedAt DEFAULT SYSDATETIME(),
        CONSTRAINT FK_ProductComments_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE,
        CONSTRAINT FK_ProductComments_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
    );
END
GO
