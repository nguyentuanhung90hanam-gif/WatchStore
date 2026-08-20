-- ==========================================================
-- Migration: WatchStore Warranty Module Enhancements
-- Description: Idempotent script for Warranty snapshots & Offline purchases
-- ==========================================================

USE WatchStore;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- 1. Ensure OrderItems has WarrantyMonths column for snapshot
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('dbo.OrderItems') AND name = 'WarrantyMonths'
)
BEGIN
    ALTER TABLE dbo.OrderItems ADD WarrantyMonths INT NOT NULL CONSTRAINT DF_OrderItems_WarrantyMonths DEFAULT 12;
END
GO

-- 2. Update existing OrderItems with WarrantyMonths from Products if available
UPDATE oi
SET oi.WarrantyMonths = ISNULL(p.WarrantyMonths, 12)
FROM dbo.OrderItems oi
JOIN dbo.ProductVariants pv ON pv.VariantID = oi.VariantID
JOIN dbo.Products p ON p.ProductID = pv.ProductID;
GO

-- 3. Enhance Warranties table with additional fields for Offline & Images
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'ImageUrl'
)
BEGIN
    ALTER TABLE dbo.Warranties ADD ImageUrl NVARCHAR(1000) NULL;
END
GO

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'CustomerName'
)
BEGIN
    ALTER TABLE dbo.Warranties ADD CustomerName NVARCHAR(150) NULL;
END
GO

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'CustomerPhone'
)
BEGIN
    ALTER TABLE dbo.Warranties ADD CustomerPhone VARCHAR(20) NULL;
END
GO

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'CustomerEmail'
)
BEGIN
    ALTER TABLE dbo.Warranties ADD CustomerEmail VARCHAR(150) NULL;
END
GO

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'WarrantyType'
)
BEGIN
    ALTER TABLE dbo.Warranties ADD WarrantyType VARCHAR(20) NOT NULL CONSTRAINT DF_Warranties_WarrantyType DEFAULT 'ONLINE';
END
GO

-- Make OrderID nullable in Warranties to allow offline walk-in purchases without an online OrderID
ALTER TABLE dbo.Warranties ALTER COLUMN OrderID BIGINT NULL;
GO
