/*
================================================================================
    WATCHSTORE - CANONICAL DATABASE MIGRATION SCRIPT
    Tương thích: SQL Server 2019 / SQL Server 2022 / SSMS
    Database Target: WatchStore
    Nguồn cấu trúc chuẩn (Canonical Target Schema) cho dự án WatchStore Java Web MVC
================================================================================
*/

IF DB_ID(N'WatchStore') IS NULL
BEGIN
    EXEC(N'CREATE DATABASE WatchStore COLLATE Vietnamese_CI_AS');
END;
GO

USE WatchStore;
GO

SET NOCOUNT ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

/* ============================================================================
   01. XÓA TẤT CẢ FOREIGN KEY VÀ OBJECT CŨ ĐỂ KHỞI TẠO CẤU TRÚC CANONICAL SẠCH SẼ
   ============================================================================ */
DECLARE @dropFkSql NVARCHAR(MAX) = N'';
SELECT @dropFkSql += N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + N'.' + QUOTENAME(OBJECT_NAME(parent_object_id)) + N' DROP CONSTRAINT ' + QUOTENAME(name) + N';' + CHAR(13)
FROM sys.foreign_keys;
IF @dropFkSql <> N'' EXEC sp_executesql @dropFkSql;
GO

DROP VIEW IF EXISTS dbo.vw_LowStock;
DROP VIEW IF EXISTS dbo.vw_TopSellingProducts;
DROP VIEW IF EXISTS dbo.vw_DashboardSalesDaily;
DROP VIEW IF EXISTS dbo.vw_CustomerSummary;
DROP VIEW IF EXISTS dbo.vw_InventoryOverview;
GO

DROP PROCEDURE IF EXISTS dbo.sp_CreateOrderFromCart;
DROP PROCEDURE IF EXISTS dbo.sp_ClaimVoucher;
DROP PROCEDURE IF EXISTS dbo.sp_UpdateOrderStatus;
DROP PROCEDURE IF EXISTS dbo.sp_RecordInventoryTransaction;
DROP PROCEDURE IF EXISTS dbo.sp_AddToCart;
GO

DROP FUNCTION IF EXISTS dbo.fn_CalculateVoucherDiscount;
GO

DROP TABLE IF EXISTS dbo.OtpVerifications;
DROP TABLE IF EXISTS dbo.Returns;
DROP TABLE IF EXISTS dbo.Warranties;
DROP TABLE IF EXISTS dbo.AuditLogs;
DROP TABLE IF EXISTS dbo.SystemSettings;
DROP TABLE IF EXISTS dbo.Notifications;
DROP TABLE IF EXISTS dbo.StockAlertRules;
DROP TABLE IF EXISTS dbo.InventoryTransactions;
DROP TABLE IF EXISTS dbo.StocktakeItems;
DROP TABLE IF EXISTS dbo.Stocktakes;
DROP TABLE IF EXISTS dbo.StockExportItems;
DROP TABLE IF EXISTS dbo.StockExports;
DROP TABLE IF EXISTS dbo.StockReceiptItems;
DROP TABLE IF EXISTS dbo.StockReceipts;
DROP TABLE IF EXISTS dbo.InventoryBalances;
DROP TABLE IF EXISTS dbo.ReturnItems;
DROP TABLE IF EXISTS dbo.ReturnRequests;
DROP TABLE IF EXISTS dbo.Deliveries;
DROP TABLE IF EXISTS dbo.Payments;
DROP TABLE IF EXISTS dbo.ReviewReplies;
DROP TABLE IF EXISTS dbo.ReviewMedia;
DROP TABLE IF EXISTS dbo.Reviews;
DROP TABLE IF EXISTS dbo.OrderStatusHistory;
DROP TABLE IF EXISTS dbo.OrderItems;
DROP TABLE IF EXISTS dbo.VoucherUsages;
DROP TABLE IF EXISTS dbo.Orders;
DROP TABLE IF EXISTS dbo.WishlistItems;
DROP TABLE IF EXISTS dbo.Wishlists;
DROP TABLE IF EXISTS dbo.CartItems;
DROP TABLE IF EXISTS dbo.Carts;
DROP TABLE IF EXISTS dbo.VoucherUsers;
DROP TABLE IF EXISTS dbo.VoucherCategories;
DROP TABLE IF EXISTS dbo.VoucherProducts;
DROP TABLE IF EXISTS dbo.Vouchers;
DROP TABLE IF EXISTS dbo.Posts;
DROP TABLE IF EXISTS dbo.Banners;
DROP TABLE IF EXISTS dbo.VariantAttributeValues;
DROP TABLE IF EXISTS dbo.ProductVariants;
DROP TABLE IF EXISTS dbo.ProductAttributeValues;
DROP TABLE IF EXISTS dbo.ProductAttributes;
DROP TABLE IF EXISTS dbo.ProductImages;
DROP TABLE IF EXISTS dbo.Products;
DROP TABLE IF EXISTS dbo.Categories;
DROP TABLE IF EXISTS dbo.Brands;
DROP TABLE IF EXISTS dbo.Warehouses;
DROP TABLE IF EXISTS dbo.CustomerNotes;
DROP TABLE IF EXISTS dbo.UserAddresses;
DROP TABLE IF EXISTS dbo.UserPermissions;
DROP TABLE IF EXISTS dbo.UserRoles;
DROP TABLE IF EXISTS dbo.RolePermissions;
DROP TABLE IF EXISTS dbo.Permissions;
DROP TABLE IF EXISTS dbo.Roles;
DROP TABLE IF EXISTS dbo.Users;
GO

DROP SEQUENCE IF EXISTS dbo.OrderNumberSequence;
GO

CREATE SEQUENCE dbo.OrderNumberSequence
    START WITH 1000
    INCREMENT BY 1;
GO

/* ============================================================================
   02. CẤU TRÚC BẢNG NGUYÊN BẢN CANONICAL (SCHEMA)
   ============================================================================ */

-- USERS
CREATE TABLE dbo.Users (
    UserID              INT IDENTITY(1,1) PRIMARY KEY,
    Email               VARCHAR(150) NOT NULL,
    PasswordHash        VARCHAR(255) NOT NULL,
    FullName            NVARCHAR(150) NOT NULL,
    Phone               VARCHAR(20) NULL,
    Gender              VARCHAR(10) NULL,
    DateOfBirth         DATE NULL,
    AvatarUrl           NVARCHAR(500) NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Users_Status DEFAULT 'ACTIVE',
    EmailVerified       BIT NOT NULL CONSTRAINT DF_Users_EmailVerified DEFAULT 1,
    EmailVerifiedAt     DATETIME2 NULL,
    LastLoginAt         DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Users_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Users_Email UNIQUE (Email)
);
GO

-- ROLES (ADMIN, EMPLOYEE, CUSTOMER)
CREATE TABLE dbo.Roles (
    RoleID              INT IDENTITY(1,1) PRIMARY KEY,
    RoleCode            VARCHAR(30) NOT NULL,
    RoleName            NVARCHAR(100) NOT NULL,
    Description         NVARCHAR(500) NULL,
    CONSTRAINT UQ_Roles_Code UNIQUE (RoleCode)
);
GO

-- PERMISSIONS
CREATE TABLE dbo.Permissions (
    PermissionID        INT IDENTITY(1,1) PRIMARY KEY,
    PermissionCode      VARCHAR(100) NOT NULL,
    PermissionName      NVARCHAR(150) NOT NULL,
    ModuleGroup         VARCHAR(50) NOT NULL,
    Description         NVARCHAR(300) NULL,
    CONSTRAINT UQ_Permissions_Code UNIQUE (PermissionCode)
);
GO

-- USER ROLES
CREATE TABLE dbo.UserRoles (
    UserID              INT NOT NULL,
    RoleID              INT NOT NULL,
    AssignedAt          DATETIME2 NOT NULL CONSTRAINT DF_UserRoles_AssignedAt DEFAULT SYSDATETIME(),
    CONSTRAINT PK_UserRoles PRIMARY KEY (UserID, RoleID),
    CONSTRAINT FK_UserRoles_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE,
    CONSTRAINT FK_UserRoles_Role FOREIGN KEY (RoleID) REFERENCES dbo.Roles(RoleID) ON DELETE CASCADE
);
GO

-- ROLE PERMISSIONS
CREATE TABLE dbo.RolePermissions (
    RoleID              INT NOT NULL,
    PermissionID        INT NOT NULL,
    GrantedAt           DATETIME2 NOT NULL CONSTRAINT DF_RolePermissions_GrantedAt DEFAULT SYSDATETIME(),
    CONSTRAINT PK_RolePermissions PRIMARY KEY (RoleID, PermissionID),
    CONSTRAINT FK_RolePermissions_Role FOREIGN KEY (RoleID) REFERENCES dbo.Roles(RoleID) ON DELETE CASCADE,
    CONSTRAINT FK_RolePermissions_Permission FOREIGN KEY (PermissionID) REFERENCES dbo.Permissions(PermissionID) ON DELETE CASCADE
);
GO

-- USER PERMISSIONS (Ma trận phân quyền nhân viên theo tài khoản)
CREATE TABLE dbo.UserPermissions (
    UserID              INT NOT NULL,
    PermissionID        INT NOT NULL,
    GrantedAt           DATETIME2 NOT NULL CONSTRAINT DF_UserPermissions_GrantedAt DEFAULT SYSDATETIME(),
    CONSTRAINT PK_UserPermissions PRIMARY KEY (UserID, PermissionID),
    CONSTRAINT FK_UserPermissions_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE,
    CONSTRAINT FK_UserPermissions_Permission FOREIGN KEY (PermissionID) REFERENCES dbo.Permissions(PermissionID) ON DELETE CASCADE
);
GO

-- USER ADDRESSES
CREATE TABLE dbo.UserAddresses (
    AddressID           INT IDENTITY(1,1) PRIMARY KEY,
    UserID              INT NOT NULL,
    RecipientName       NVARCHAR(150) NOT NULL,
    RecipientPhone      VARCHAR(20) NOT NULL,
    Province            NVARCHAR(100) NOT NULL,
    District            NVARCHAR(100) NOT NULL,
    Ward                NVARCHAR(100) NOT NULL,
    AddressLine         NVARCHAR(300) NOT NULL,
    AddressType         VARCHAR(20) NOT NULL CONSTRAINT DF_UserAddresses_Type DEFAULT 'HOME',
    IsDefault           BIT NOT NULL CONSTRAINT DF_UserAddresses_Default DEFAULT 0,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_UserAddresses_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_UserAddresses_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
);
GO

-- BRANDS
CREATE TABLE dbo.Brands (
    BrandID             INT IDENTITY(1,1) PRIMARY KEY,
    BrandCode           VARCHAR(40) NULL,
    BrandName           NVARCHAR(120) NOT NULL,
    BrandSlug           VARCHAR(150) NOT NULL,
    Country             NVARCHAR(100) NULL,
    LogoUrl             NVARCHAR(500) NULL,
    Description         NVARCHAR(MAX) NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Brands_Status DEFAULT 'ACTIVE',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Brands_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Brands_Slug UNIQUE (BrandSlug)
);
GO

-- CATEGORIES
CREATE TABLE dbo.Categories (
    CategoryID          INT IDENTITY(1,1) PRIMARY KEY,
    ParentCategoryID    INT NULL,
    CategoryCode        VARCHAR(40) NULL,
    CategoryName        NVARCHAR(120) NOT NULL,
    CategorySlug        VARCHAR(150) NOT NULL,
    Description         NVARCHAR(1000) NULL,
    ImageUrl            NVARCHAR(500) NULL,
    DisplayOrder        INT NOT NULL CONSTRAINT DF_Categories_Order DEFAULT 0,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Categories_Status DEFAULT 'ACTIVE',
    CONSTRAINT UQ_Categories_Slug UNIQUE (CategorySlug),
    CONSTRAINT FK_Categories_Parent FOREIGN KEY (ParentCategoryID) REFERENCES dbo.Categories(CategoryID)
);
GO

-- PRODUCTS
CREATE TABLE dbo.Products (
    ProductID           INT IDENTITY(1,1) PRIMARY KEY,
    ProductCode         VARCHAR(50) NOT NULL,
    ProductName         NVARCHAR(250) NOT NULL,
    ProductSlug         VARCHAR(300) NOT NULL,
    BrandID             INT NOT NULL,
    CategoryID          INT NOT NULL,
    MovementType        VARCHAR(30) NOT NULL,
    Gender              VARCHAR(20) NOT NULL CONSTRAINT DF_Products_Gender DEFAULT 'MEN',
    ShortDescription    NVARCHAR(1000) NULL,
    Description         NVARCHAR(MAX) NULL,
    CaseMaterial        NVARCHAR(120) NULL,
    GlassMaterial       NVARCHAR(120) NULL,
    StrapMaterial       NVARCHAR(120) NULL,
    WaterResistance     NVARCHAR(100) NULL,
    OriginCountry       NVARCHAR(100) NULL,
    WarrantyMonths      INT NOT NULL CONSTRAINT DF_Products_Warranty DEFAULT 24,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Products_Status DEFAULT 'ACTIVE',
    IsFeatured          BIT NOT NULL CONSTRAINT DF_Products_Featured DEFAULT 0,
    RatingAverage       DECIMAL(3,2) NOT NULL CONSTRAINT DF_Products_Rating DEFAULT 0,
    RatingCount         INT NOT NULL CONSTRAINT DF_Products_RatingCount DEFAULT 0,
    CreatedBy           INT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Products_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Products_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Products_Code UNIQUE (ProductCode),
    CONSTRAINT UQ_Products_Slug UNIQUE (ProductSlug),
    CONSTRAINT FK_Products_Brand FOREIGN KEY (BrandID) REFERENCES dbo.Brands(BrandID),
    CONSTRAINT FK_Products_Category FOREIGN KEY (CategoryID) REFERENCES dbo.Categories(CategoryID)
);
GO

-- PRODUCT IMAGES
CREATE TABLE dbo.ProductImages (
    ProductImageID      BIGINT IDENTITY(1,1) PRIMARY KEY,
    ProductID           INT NOT NULL,
    ImageUrl            NVARCHAR(500) NOT NULL,
    AltText             NVARCHAR(250) NULL,
    IsPrimary           BIT NOT NULL CONSTRAINT DF_ProductImages_Primary DEFAULT 0,
    DisplayOrder        INT NOT NULL CONSTRAINT DF_ProductImages_Order DEFAULT 0,
    CONSTRAINT FK_ProductImages_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE
);
GO

-- PRODUCT VARIANTS
CREATE TABLE dbo.ProductVariants (
    VariantID           INT IDENTITY(1,1) PRIMARY KEY,
    ProductID           INT NOT NULL,
    SKU                 VARCHAR(50) NOT NULL,
    Barcode             VARCHAR(50) NULL,
    VariantName         NVARCHAR(150) NOT NULL,
    CostPrice           DECIMAL(18,2) NOT NULL DEFAULT 0,
    SalePrice           DECIMAL(18,2) NOT NULL DEFAULT 0,
    CompareAtPrice      DECIMAL(18,2) NULL,
    WeightGram          INT NOT NULL DEFAULT 0,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_ProductVariants_Status DEFAULT 'ACTIVE',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_ProductVariants_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_ProductVariants_SKU UNIQUE (SKU),
    CONSTRAINT FK_ProductVariants_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE
);
GO

-- CARTS
CREATE TABLE dbo.Carts (
    CartID              BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID              INT NULL,
    GuestToken          VARCHAR(100) NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Carts_Status DEFAULT 'ACTIVE',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Carts_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Carts_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Carts_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
);
GO

-- CART ITEMS
CREATE TABLE dbo.CartItems (
    CartItemID          BIGINT IDENTITY(1,1) PRIMARY KEY,
    CartID              BIGINT NOT NULL,
    VariantID           INT NOT NULL,
    Quantity            INT NOT NULL DEFAULT 1,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_CartItems_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_CartItems_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_CartItems_Cart FOREIGN KEY (CartID) REFERENCES dbo.Carts(CartID) ON DELETE CASCADE,
    CONSTRAINT FK_CartItems_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID)
);
GO

-- WISHLISTS
CREATE TABLE dbo.Wishlists (
    WishlistID          INT IDENTITY(1,1) PRIMARY KEY,
    UserID              INT NOT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Wishlists_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Wishlists_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
);
GO

-- WISHLIST ITEMS
CREATE TABLE dbo.WishlistItems (
    WishlistItemID      INT IDENTITY(1,1) PRIMARY KEY,
    WishlistID          INT NOT NULL,
    ProductID           INT NOT NULL,
    AddedAt             DATETIME2 NOT NULL CONSTRAINT DF_WishlistItems_AddedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_WishlistItems_Wishlist FOREIGN KEY (WishlistID) REFERENCES dbo.Wishlists(WishlistID) ON DELETE CASCADE,
    CONSTRAINT FK_WishlistItems_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID)
);
GO

-- VOUCHERS
CREATE TABLE dbo.Vouchers (
    VoucherID           INT IDENTITY(1,1) PRIMARY KEY,
    VoucherCode         VARCHAR(50) NOT NULL,
    VoucherName         NVARCHAR(150) NOT NULL,
    Description         NVARCHAR(500) NULL,
    DiscountType        VARCHAR(20) NOT NULL,
    DiscountValue       DECIMAL(18,2) NOT NULL,
    MinimumOrderValue   DECIMAL(18,2) NOT NULL DEFAULT 0,
    MaximumDiscount     DECIMAL(18,2) NULL,
    UsageLimit          INT NOT NULL DEFAULT 100,
    UsageLimitPerUser   INT NOT NULL DEFAULT 1,
    UsedCount           INT NOT NULL DEFAULT 0,
    StartAt             DATETIME2 NOT NULL,
    EndAt               DATETIME2 NOT NULL,
    IsPublic            BIT NOT NULL DEFAULT 1,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Vouchers_Status DEFAULT 'ACTIVE',
    CreatedBy           INT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Vouchers_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Vouchers_Code UNIQUE (VoucherCode)
);
GO

-- ORDERS
CREATE TABLE dbo.Orders (
    OrderID             BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderCode           VARCHAR(30) NOT NULL,
    CustomerID          INT NOT NULL,
    VoucherID           INT NULL,
    RecipientName       NVARCHAR(150) NOT NULL,
    RecipientPhone      VARCHAR(20) NOT NULL,
    ShippingAddress     NVARCHAR(500) NOT NULL,
    CustomerNote        NVARCHAR(1000) NULL,
    OrderStatus         VARCHAR(30) NOT NULL CONSTRAINT DF_Orders_Status DEFAULT 'PENDING',
    PaymentStatus       VARCHAR(30) NOT NULL CONSTRAINT DF_Orders_Payment DEFAULT 'UNPAID',
    SubtotalAmount      DECIMAL(18,2) NOT NULL DEFAULT 0,
    DiscountAmount      DECIMAL(18,2) NOT NULL DEFAULT 0,
    ShippingFee         DECIMAL(18,2) NOT NULL DEFAULT 0,
    TotalAmount         DECIMAL(18,2) NOT NULL DEFAULT 0,
    ConfirmedAt         DATETIME2 NULL,
    CompletedAt         DATETIME2 NULL,
    CancelledAt         DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Orders_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Orders_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Orders_Code UNIQUE (OrderCode),
    CONSTRAINT FK_Orders_Customer FOREIGN KEY (CustomerID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Orders_Voucher FOREIGN KEY (VoucherID) REFERENCES dbo.Vouchers(VoucherID)
);
GO

-- ORDER ITEMS
CREATE TABLE dbo.OrderItems (
    OrderItemID         BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderID             BIGINT NOT NULL,
    VariantID           INT NOT NULL,
    ProductName         NVARCHAR(250) NOT NULL,
    VariantName         NVARCHAR(150) NOT NULL,
    SKU                 VARCHAR(50) NOT NULL,
    ImageUrl            NVARCHAR(500) NULL,
    UnitPrice           DECIMAL(18,2) NOT NULL,
    Quantity            INT NOT NULL,
    DiscountAmount      DECIMAL(18,2) NOT NULL DEFAULT 0,
    LineTotal           AS ((UnitPrice * Quantity) - DiscountAmount) PERSISTED,
    CONSTRAINT FK_OrderItems_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID) ON DELETE CASCADE,
    CONSTRAINT FK_OrderItems_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID)
);
GO

-- VOUCHER USAGES
CREATE TABLE dbo.VoucherUsages (
    VoucherUsageID      BIGINT IDENTITY(1,1) PRIMARY KEY,
    VoucherID           INT NOT NULL,
    UserID              INT NOT NULL,
    OrderID             BIGINT NOT NULL,
    DiscountAmount      DECIMAL(18,2) NOT NULL,
    UsedAt              DATETIME2 NOT NULL CONSTRAINT DF_VoucherUsages_UsedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_VoucherUsages_Voucher FOREIGN KEY (VoucherID) REFERENCES dbo.Vouchers(VoucherID),
    CONSTRAINT FK_VoucherUsages_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_VoucherUsages_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID)
);
GO

-- RETURNS
CREATE TABLE dbo.Returns (
    ReturnID            INT IDENTITY(1,1) PRIMARY KEY,
    OrderID             BIGINT NOT NULL,
    OrderItemID         BIGINT NULL,
    CustomerID          INT NOT NULL,
    ProductName         NVARCHAR(250) NOT NULL,
    Reason              NVARCHAR(500) NOT NULL,
    Status              VARCHAR(30) NOT NULL CONSTRAINT DF_Returns_Status DEFAULT 'PENDING',
    RefundStatus        VARCHAR(30) NOT NULL CONSTRAINT DF_Returns_Refund DEFAULT 'UNREFUNDED',
    EmployeeNote        NVARCHAR(500) NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Returns_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Returns_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Returns_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID),
    CONSTRAINT FK_Returns_Customer FOREIGN KEY (CustomerID) REFERENCES dbo.Users(UserID)
);
GO

-- WARRANTIES
CREATE TABLE dbo.Warranties (
    WarrantyID          INT IDENTITY(1,1) PRIMARY KEY,
    OrderID             BIGINT NOT NULL,
    ProductName         NVARCHAR(250) NOT NULL,
    SerialNumber        VARCHAR(100) NULL,
    WarrantyMonths      INT NOT NULL DEFAULT 12,
    StartDate           DATE NULL,
    EndDate             DATE NULL,
    Status              NVARCHAR(50) NOT NULL DEFAULT N'Đang bảo hành',
    Note                NVARCHAR(500) NULL,
    ReceiveDate         DATE NULL,
    ReceiveNote         NVARCHAR(500) NULL,
    RepairContent       NVARCHAR(500) NULL,
    ComponentReplaced   NVARCHAR(300) NULL,
    RepairNote          NVARCHAR(500) NULL,
    CompleteDate        DATE NULL,
    ReturnDate          DATE NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Warranties_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Warranties_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID)
);
GO

-- WAREHOUSES
CREATE TABLE dbo.Warehouses (
    WarehouseID         INT IDENTITY(1,1) PRIMARY KEY,
    WarehouseCode       VARCHAR(30) NOT NULL,
    WarehouseName       NVARCHAR(150) NOT NULL,
    Address             NVARCHAR(300) NOT NULL,
    ManagerID           INT NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Warehouses_Status DEFAULT 'ACTIVE',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Warehouses_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Warehouses_Code UNIQUE (WarehouseCode),
    CONSTRAINT FK_Warehouses_Manager FOREIGN KEY (ManagerID) REFERENCES dbo.Users(UserID)
);
GO

-- INVENTORY BALANCES
CREATE TABLE dbo.InventoryBalances (
    InventoryID         BIGINT IDENTITY(1,1) PRIMARY KEY,
    WarehouseID         INT NOT NULL,
    VariantID           INT NOT NULL,
    QuantityOnHand      INT NOT NULL DEFAULT 0,
    QuantityReserved    INT NOT NULL DEFAULT 0,
    ReorderLevel        INT NOT NULL DEFAULT 5,
    AvailableQuantity   AS (QuantityOnHand - QuantityReserved) PERSISTED,
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_InventoryBalances_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_InventoryBalances UNIQUE (WarehouseID, VariantID),
    CONSTRAINT FK_InventoryBalances_Warehouse FOREIGN KEY (WarehouseID) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_InventoryBalances_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID)
);
GO

-- INVENTORY TRANSACTIONS
CREATE TABLE dbo.InventoryTransactions (
    TransactionID       BIGINT IDENTITY(1,1) PRIMARY KEY,
    WarehouseID         INT NOT NULL,
    VariantID           INT NOT NULL,
    TransactionType     VARCHAR(30) NOT NULL,
    QuantityChange      INT NOT NULL,
    QuantityBefore      INT NOT NULL,
    QuantityAfter       INT NOT NULL,
    ReferenceType       VARCHAR(30) NULL,
    ReferenceID         BIGINT NULL,
    Note                NVARCHAR(500) NULL,
    CreatedBy           INT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_InventoryTransactions_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_InventoryTx_Warehouse FOREIGN KEY (WarehouseID) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_InventoryTx_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT FK_InventoryTx_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID)
);
GO

-- OTP VERIFICATIONS
CREATE TABLE dbo.OtpVerifications (
    OtpID               BIGINT IDENTITY(1,1) PRIMARY KEY,
    Email               VARCHAR(150) NOT NULL,
    OtpCode             VARCHAR(10) NOT NULL,
    OtpType             VARCHAR(30) NOT NULL,
    IsUsed              BIT NOT NULL DEFAULT 0,
    ExpiresAt           DATETIME2 NOT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Otp_CreatedAt DEFAULT SYSDATETIME()
);
GO

-- BANNERS (Bổ sung hỗ trợ BannerRepositoryImpl)
CREATE TABLE dbo.Banners (
    BannerID            INT IDENTITY(1,1) PRIMARY KEY,
    BannerName          NVARCHAR(150) NOT NULL,
    Title               NVARCHAR(200) NULL,
    Subtitle            NVARCHAR(300) NULL,
    ImageUrl            NVARCHAR(500) NOT NULL,
    TargetUrl           NVARCHAR(500) NULL,
    PositionCode        VARCHAR(50) NOT NULL DEFAULT 'HOME_HERO',
    DisplayOrder        INT NOT NULL DEFAULT 0,
    Status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

-- POSTS (Bổ sung hỗ trợ PostRepositoryImpl & UserRepositoryImpl)
CREATE TABLE dbo.Posts (
    PostID              INT IDENTITY(1,1) PRIMARY KEY,
    PostType            VARCHAR(30) NOT NULL DEFAULT 'ARTICLE',
    Title               NVARCHAR(250) NOT NULL,
    Slug                VARCHAR(300) NOT NULL,
    Summary             NVARCHAR(1000) NULL,
    Content             NVARCHAR(MAX) NOT NULL,
    ThumbnailUrl        NVARCHAR(500) NULL,
    Status              VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    AuthorID            INT NULL,
    PublishedAt         DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Posts_Slug UNIQUE (Slug),
    CONSTRAINT FK_Posts_Author FOREIGN KEY (AuthorID) REFERENCES dbo.Users(UserID)
);
GO

-- NOTIFICATIONS (Bổ sung hỗ trợ NotificationRepositoryImpl)
CREATE TABLE dbo.Notifications (
    NotificationID      BIGINT IDENTITY(1,1) PRIMARY KEY,
    Title               NVARCHAR(200) NOT NULL,
    Content             NVARCHAR(1000) NOT NULL,
    TargetType          VARCHAR(30) NOT NULL DEFAULT 'ALL',
    TargetUrl           NVARCHAR(500) NULL,
    CreatedBy           INT NULL,
    CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Notifications_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID)
);
GO

-- PRODUCT ATTRIBUTES & VALUES (Bổ sung hỗ trợ VariantRepository & InventoryRepository)
CREATE TABLE dbo.ProductAttributes (
    AttributeID         INT IDENTITY(1,1) PRIMARY KEY,
    AttributeCode       VARCHAR(50) NOT NULL,
    AttributeName       NVARCHAR(100) NOT NULL,
    CONSTRAINT UQ_ProductAttributes_Code UNIQUE (AttributeCode)
);
GO

CREATE TABLE dbo.ProductAttributeValues (
    AttributeValueID    INT IDENTITY(1,1) PRIMARY KEY,
    AttributeID          INT NOT NULL,
    ValueText            NVARCHAR(150) NOT NULL,
    DisplayOrder         INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_ProductAttrVal_Attr FOREIGN KEY (AttributeID) REFERENCES dbo.ProductAttributes(AttributeID) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.VariantAttributeValues (
    VariantID            INT NOT NULL,
    AttributeValueID     INT NOT NULL,
    CONSTRAINT PK_VariantAttributeValues PRIMARY KEY (VariantID, AttributeValueID),
    CONSTRAINT FK_VarAttrVal_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID) ON DELETE CASCADE,
    CONSTRAINT FK_VarAttrVal_Val FOREIGN KEY (AttributeValueID) REFERENCES dbo.ProductAttributeValues(AttributeValueID)
);
GO

-- STOCK RECEIPTS & ITEMS (Bổ sung hỗ trợ StockReceiptRepository)
CREATE TABLE dbo.StockReceipts (
    StockReceiptId      BIGINT IDENTITY(1,1) PRIMARY KEY,
    ReceiptCode         VARCHAR(40) NOT NULL,
    WarehouseId         INT NOT NULL,
    SupplierName        NVARCHAR(150) NULL,
    SupplierPhone       VARCHAR(20) NULL,
    Status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    TotalCost           DECIMAL(18,2) NOT NULL DEFAULT 0,
    Note                NVARCHAR(500) NULL,
    CreatedBy           INT NOT NULL,
    ApprovedBy          INT NULL,
    ReceiptDate         DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_StockReceipts_Code UNIQUE (ReceiptCode),
    CONSTRAINT FK_StockReceipts_Warehouse FOREIGN KEY (WarehouseId) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_StockReceipts_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_StockReceipts_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES dbo.Users(UserID)
);
GO

CREATE TABLE dbo.StockReceiptItems (
    StockReceiptItemId  BIGINT IDENTITY(1,1) PRIMARY KEY,
    StockReceiptId      BIGINT NOT NULL,
    VariantId           INT NOT NULL,
    Quantity            INT NOT NULL,
    UnitCost            DECIMAL(18,2) NOT NULL,
    TotalCost           AS (Quantity * UnitCost) PERSISTED,
    CONSTRAINT FK_StockReceiptItems_Receipt FOREIGN KEY (StockReceiptId) REFERENCES dbo.StockReceipts(StockReceiptId) ON DELETE CASCADE,
    CONSTRAINT FK_StockReceiptItems_Variant FOREIGN KEY (VariantId) REFERENCES dbo.ProductVariants(VariantID)
);
GO

-- STOCK EXPORTS & ITEMS (Bổ sung hỗ trợ StockExportRepository)
CREATE TABLE dbo.StockExports (
    StockExportId       BIGINT IDENTITY(1,1) PRIMARY KEY,
    ExportCode          VARCHAR(40) NOT NULL,
    WarehouseId         INT NOT NULL,
    ExportType          VARCHAR(30) NOT NULL DEFAULT 'SALE',
    Status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    Note                NVARCHAR(500) NULL,
    CreatedBy           INT NOT NULL,
    ApprovedBy          INT NULL,
    ExportDate          DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_StockExports_Code UNIQUE (ExportCode),
    CONSTRAINT FK_StockExports_Warehouse FOREIGN KEY (WarehouseId) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_StockExports_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_StockExports_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES dbo.Users(UserID)
);
GO

CREATE TABLE dbo.StockExportItems (
    StockExportItemId   BIGINT IDENTITY(1,1) PRIMARY KEY,
    StockExportId       BIGINT NOT NULL,
    VariantId           INT NOT NULL,
    Quantity            INT NOT NULL,
    CONSTRAINT FK_StockExportItems_Export FOREIGN KEY (StockExportId) REFERENCES dbo.StockExports(StockExportId) ON DELETE CASCADE,
    CONSTRAINT FK_StockExportItems_Variant FOREIGN KEY (VariantId) REFERENCES dbo.ProductVariants(VariantID)
);
GO

-- STOCKTAKES & ITEMS (Bổ sung hỗ trợ StocktakeRepository)
CREATE TABLE dbo.Stocktakes (
    StocktakeId         BIGINT IDENTITY(1,1) PRIMARY KEY,
    StocktakeCode       VARCHAR(40) NOT NULL,
    WarehouseId         INT NOT NULL,
    Status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    Note                NVARCHAR(500) NULL,
    CreatedBy           INT NOT NULL,
    ApprovedBy          INT NULL,
    StocktakeDate       DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Stocktakes_Code UNIQUE (StocktakeCode),
    CONSTRAINT FK_Stocktakes_Warehouse FOREIGN KEY (WarehouseId) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_Stocktakes_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Stocktakes_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES dbo.Users(UserID)
);
GO

CREATE TABLE dbo.StocktakeItems (
    StocktakeItemId     BIGINT IDENTITY(1,1) PRIMARY KEY,
    StocktakeId         BIGINT NOT NULL,
    VariantId           INT NOT NULL,
    SystemQuantity      INT NOT NULL,
    ActualQuantity      INT NOT NULL,
    Difference          AS (ActualQuantity - SystemQuantity) PERSISTED,
    Note                NVARCHAR(250) NULL,
    CONSTRAINT FK_StocktakeItems_Stocktake FOREIGN KEY (StocktakeId) REFERENCES dbo.Stocktakes(StocktakeId) ON DELETE CASCADE,
    CONSTRAINT FK_StocktakeItems_Variant FOREIGN KEY (VariantId) REFERENCES dbo.ProductVariants(VariantID)
);
GO

-- REVIEWS (Bổ sung hỗ trợ UserRepositoryImpl & ProductRepositoryImpl)
CREATE TABLE dbo.Reviews (
    ReviewID            BIGINT IDENTITY(1,1) PRIMARY KEY,
    ProductID           INT NOT NULL,
    UserID              INT NOT NULL,
    OrderID             BIGINT NULL,
    Rating              INT NOT NULL CHECK (Rating BETWEEN 1 AND 5),
    Title               NVARCHAR(200) NULL,
    Content             NVARCHAR(1000) NOT NULL,
    Status              VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Reviews_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID),
    CONSTRAINT FK_Reviews_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Reviews_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID)
);
GO

-- CUSTOMER NOTES (Bổ sung hỗ trợ UserRepositoryImpl)
CREATE TABLE dbo.CustomerNotes (
    NoteID              BIGINT IDENTITY(1,1) PRIMARY KEY,
    CustomerID          INT NOT NULL,
    StaffID             INT NOT NULL,
    NoteContent         NVARCHAR(1000) NOT NULL,
    CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_CustomerNotes_Customer FOREIGN KEY (CustomerID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_CustomerNotes_Staff FOREIGN KEY (StaffID) REFERENCES dbo.Users(UserID)
);
GO

-- VOUCHER RELATION TABLES (Bổ sung hỗ trợ VoucherRepositoryImpl & CategoryRepositoryImpl)
CREATE TABLE dbo.VoucherProducts (
    VoucherID           INT NOT NULL,
    ProductID           INT NOT NULL,
    CONSTRAINT PK_VoucherProducts PRIMARY KEY (VoucherID, ProductID),
    CONSTRAINT FK_VoucherProducts_Voucher FOREIGN KEY (VoucherID) REFERENCES dbo.Vouchers(VoucherID) ON DELETE CASCADE,
    CONSTRAINT FK_VoucherProducts_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.VoucherCategories (
    VoucherID           INT NOT NULL,
    CategoryID          INT NOT NULL,
    CONSTRAINT PK_VoucherCategories PRIMARY KEY (VoucherID, CategoryID),
    CONSTRAINT FK_VoucherCategories_Voucher FOREIGN KEY (VoucherID) REFERENCES dbo.Vouchers(VoucherID) ON DELETE CASCADE,
    CONSTRAINT FK_VoucherCategories_Category FOREIGN KEY (CategoryID) REFERENCES dbo.Categories(CategoryID) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.VoucherUsers (
    VoucherID           INT NOT NULL,
    UserID              INT NOT NULL,
    IsUsed              BIT NOT NULL DEFAULT 0,
    AssignedAt          DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT PK_VoucherUsers PRIMARY KEY (VoucherID, UserID),
    CONSTRAINT FK_VoucherUsers_Voucher FOREIGN KEY (VoucherID) REFERENCES dbo.Vouchers(VoucherID) ON DELETE CASCADE,
    CONSTRAINT FK_VoucherUsers_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
);
GO

/* ============================================================================
   03. CẤU TRÚC VIEWS NGHIỆP VỤ TRUY VẤN
   ============================================================================ */

CREATE OR ALTER VIEW dbo.vw_InventoryOverview AS
SELECT 
    w.WarehouseID,
    w.WarehouseName,
    p.ProductID,
    p.ProductName,
    pv.VariantID,
    pv.SKU,
    pv.VariantName,
    ib.QuantityOnHand,
    ib.QuantityReserved,
    ib.AvailableQuantity,
    ib.ReorderLevel,
    CASE 
        WHEN ib.AvailableQuantity <= 0 THEN N'HẾT HÀNG'
        WHEN ib.AvailableQuantity <= ib.ReorderLevel THEN N'CẢNH BÁO SẮP HẾT'
        ELSE N'AN TOÀN'
    END AS StockStatus
FROM dbo.InventoryBalances ib
INNER JOIN dbo.Warehouses w ON ib.WarehouseID = w.WarehouseID
INNER JOIN dbo.ProductVariants pv ON ib.VariantID = pv.VariantID
INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID;
GO

CREATE OR ALTER VIEW dbo.vw_LowStock AS
SELECT * FROM dbo.vw_InventoryOverview 
WHERE AvailableQuantity <= ReorderLevel;
GO

CREATE OR ALTER VIEW dbo.vw_TopSellingProducts AS
SELECT TOP 20
    p.ProductID,
    p.ProductName,
    pv.SKU,
    pv.VariantName,
    SUM(oi.Quantity) AS TotalQuantitySold,
    SUM(oi.LineTotal) AS TotalRevenue
FROM dbo.OrderItems oi
INNER JOIN dbo.Orders o ON oi.OrderID = o.OrderID
INNER JOIN dbo.ProductVariants pv ON oi.VariantID = pv.VariantID
INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID
WHERE o.OrderStatus IN ('DELIVERED', 'COMPLETED')
GROUP BY p.ProductID, p.ProductName, pv.SKU, pv.VariantName
ORDER BY TotalQuantitySold DESC;
GO

CREATE OR ALTER VIEW dbo.vw_DashboardSalesDaily AS
SELECT 
    CAST(o.CreatedAt AS DATE) AS SalesDate,
    COUNT(DISTINCT o.OrderID) AS TotalOrders,
    SUM(o.TotalAmount) AS GrossRevenue,
    SUM(o.DiscountAmount) AS TotalDiscounts,
    SUM(o.TotalAmount) AS NetRevenue
FROM dbo.Orders o
WHERE o.OrderStatus NOT IN ('CANCELLED', 'RETURNED')
GROUP BY CAST(o.CreatedAt AS DATE);
GO

CREATE OR ALTER VIEW dbo.vw_CustomerSummary AS
SELECT 
    u.UserID AS CustomerID,
    u.FullName,
    u.Email,
    u.Phone,
    u.Status,
    u.CreatedAt AS RegisteredAt,
    COUNT(o.OrderID) AS TotalOrdersCount,
    ISNULL(SUM(CASE WHEN o.OrderStatus IN ('DELIVERED','COMPLETED') THEN o.TotalAmount ELSE 0 END), 0) AS TotalSpentAmount,
    MAX(o.CreatedAt) AS LastOrderDate
FROM dbo.Users u
LEFT JOIN dbo.Orders o ON u.UserID = o.CustomerID
GROUP BY u.UserID, u.FullName, u.Email, u.Phone, u.Status, u.CreatedAt;
GO

/* ============================================================================
   04. DỮ LIỆU ĐỒNG BỘ NGUYÊN BẢN VÀ DỮ LIỆU BỔ SUNG TỪ LEGACY SOURCE
   ============================================================================ */

-- 1. ROLES
INSERT INTO dbo.Roles (RoleCode, RoleName, Description) VALUES
('ADMIN',     N'Quản trị viên',      N'Toàn quyền cấu hình và quản trị hệ thống'),
('EMPLOYEE',  N'Nhân viên vận hành', N'Gộp vai trò Bán hàng và Kho, phân quyền theo tài khoản'),
('CUSTOMER',  N'Khách hàng',         N'Mua hàng và quản lý tài khoản cá nhân');
GO

-- 2. PERMISSIONS
INSERT INTO dbo.Permissions (PermissionCode, PermissionName, ModuleGroup, Description) VALUES
('PRODUCT_VIEW',        N'Xem sản phẩm',            'PRODUCT',   N'Quyền xem danh sách và chi tiết sản phẩm, biến thể'),
('PRODUCT_CREATE',      N'Thêm sản phẩm',           'PRODUCT',   N'Quyền thêm mới sản phẩm và biến thể'),
('PRODUCT_EDIT',        N'Sửa sản phẩm',            'PRODUCT',   N'Quyền cập nhật thông tin sản phẩm và biến thể'),
('ORDER_VIEW',          N'Xem đơn hàng',            'ORDER',     N'Quyền xem danh sách và chi tiết đơn hàng'),
('ORDER_CREATE',        N'Tạo đơn hàng',            'ORDER',     N'Quyền tạo đơn hàng mới tại quầy POS'),
('ORDER_EDIT',          N'Sửa đơn hàng',            'ORDER',     N'Quyền cập nhật thông tin đơn hàng'),
('ORDER_APPROVE',       N'Duyệt đơn hàng',          'ORDER',     N'Quyền duyệt và xử lý trạng thái đơn hàng'),
('ORDER_EXPORT',        N'Xuất báo cáo đơn',        'ORDER',     N'Quyền xuất hóa đơn và danh sách đơn hàng'),
('CUSTOMER_VIEW',       N'Xem khách hàng',          'CUSTOMER',  N'Quyền xem danh sách và thông tin khách hàng'),
('CUSTOMER_CREATE',     N'Thêm khách hàng',         'CUSTOMER',  N'Quyền thêm mới thông tin khách hàng'),
('CUSTOMER_EDIT',       N'Sửa khách hàng',          'CUSTOMER',  N'Quyền cập nhật thông tin khách hàng'),
('INVENTORY_VIEW',      N'Xem tồn kho',             'INVENTORY', N'Quyền xem tồn kho, vị trí và lịch sử biến động'),
('INVENTORY_CREATE',    N'Tạo phiếu kho',           'INVENTORY', N'Quyền tạo phiếu nhập kho, xuất kho, kiểm kê'),
('INVENTORY_EDIT',      N'Sửa phiếu kho',           'INVENTORY', N'Quyền chỉnh sửa thông tin phiếu kho'),
('INVENTORY_APPROVE',   N'Duyệt phiếu kho',         'INVENTORY', N'Quyền duyệt phiếu nhập/xuất kho và cân bằng tồn'),
('INVENTORY_EXPORT',    N'Xuất dữ liệu kho',        'INVENTORY', N'Quyền xuất báo cáo xuất nhập tồn'),
('VOUCHER_VIEW',        N'Xem mã giảm giá',         'VOUCHER',   N'Quyền xem danh sách voucher và mã khuyến mãi'),
('VOUCHER_CREATE',      N'Thêm voucher',            'VOUCHER',   N'Quyền tạo mã giảm giá mới'),
('VOUCHER_EDIT',        N'Sửa voucher',             'VOUCHER',   N'Quyền chỉnh sửa thông tin và kích hoạt voucher'),
('REPORT_VIEW',         N'Xem báo cáo',             'REPORT',    N'Quyền xem báo cáo thống kê doanh thu và phân tích'),
('REPORT_EXPORT',       N'Xuất báo cáo',            'REPORT',    N'Quyền xuất file excel/pdf báo cáo doanh thu và vận hành'),
('SALES_DASHBOARD',     N'Xem tổng quan bán hàng',  'SALES',     N'Quyền xem trang tổng quan bán hàng'),
('SALES_ORDER',         N'Quản lý đơn hàng',        'SALES',     N'Quyền xem và cập nhật đơn hàng'),
('SALES_CUSTOMER',      N'Quản lý khách hàng',      'SALES',     N'Quyền xem danh sách và thông tin khách hàng'),
('SALES_DELIVERY',      N'Quản lý vận chuyển',      'SALES',     N'Quyền cập nhật thông tin giao hàng'),
('SALES_RETURN',        N'Quản lý đổi trả',         'SALES',     N'Quyền xem và xử lý yêu cầu đổi trả'),
('SALES_WARRANTY',      N'Quản lý bảo hành',        'SALES',     N'Quyền tiếp nhận và ghi nhận sửa chữa bảo hành'),
('SALES_REPORT',        N'Xem báo cáo bán hàng',    'SALES',     N'Quyền xem doanh thu và thống kê'),
('WAREHOUSE_DASHBOARD', N'Xem tổng quan kho',       'WAREHOUSE', N'Quyền xem dashboard tồn kho'),
('WAREHOUSE_INVENTORY', N'Quản lý tồn kho',         'WAREHOUSE', N'Quyền kiểm tra và điều chỉnh tồn kho'),
('WAREHOUSE_RECEIPT',   N'Quản lý nhập kho',        'WAREHOUSE', N'Quyền tạo và duyệt phiếu nhập kho'),
('WAREHOUSE_EXPORT',    N'Quản lý xuất kho',        'WAREHOUSE', N'Quyền tạo và duyệt phiếu xuất kho'),
('WAREHOUSE_STOCKTAKE', N'Quản lý kiểm kê',         'WAREHOUSE', N'Quyền lập biên bản kiểm kê'),
('WAREHOUSE_REPORT',    N'Xem báo cáo kho',         'WAREHOUSE', N'Quyền xem báo cáo xuất nhập tồn');
GO

-- 3. USERS (BẢO TOÀN VÀ BỔ SUNG KHÁCH HÀNG TỪ LEGACY BASED ON EMAIL KEY)
-- PasswordHash = SHA-256("123456") uppercase hex = 8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92
SET IDENTITY_INSERT dbo.Users ON;
INSERT INTO dbo.Users (UserID, Email, PasswordHash, FullName, Phone, Gender, DateOfBirth, Status, EmailVerified, EmailVerifiedAt, CreatedAt) VALUES
(1, 'admin@watchstore.vn',     '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', N'Tân Tân',       '0988000001', 'MALE',  '2007-01-15', 'ACTIVE', 1, SYSDATETIME(), SYSDATETIME()),
(2, 'employee@watchstore.vn',  '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', N'Nhân viên bán hàng 1', '0977777777', 'MALE',  '1995-05-05', 'ACTIVE', 1, SYSDATETIME(), SYSDATETIME()),
(3, 'sales@watchstore.vn',     '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', N'Nhân viên bán hàng 2', '0988000002', 'MALE',  '1999-06-10', 'ACTIVE', 1, SYSDATETIME(), SYSDATETIME()),
(4, 'sales3@watchstore.vn',    '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', N'Nhân viên bán hàng 3', '0988000003', 'MALE',  '1998-09-20', 'INACTIVE', 1, SYSDATETIME(), SYSDATETIME()),
(5, 'customer@watchstore.vn',  '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', N'Khách hàng WatchStore', '0988000004', 'OTHER', '2002-03-12', 'ACTIVE', 1, SYSDATETIME(), SYSDATETIME()),
(6, 'an.nguyen@example.com',   '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', N'Nguyễn Văn An', '0988000005', 'MALE',  '1995-04-18', 'ACTIVE', 1, SYSDATETIME(), SYSDATETIME()),
(7, 'duc.tran@example.com',    '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', N'Trần Minh Đức', '0988000006', 'MALE',  '1993-11-05', 'ACTIVE', 1, SYSDATETIME(), SYSDATETIME()),
(8, 'cong.le@example.com',     '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', N'Lê Thành Công', '0988000007', 'MALE',  '1997-08-22', 'ACTIVE', 1, SYSDATETIME(), SYSDATETIME());
SET IDENTITY_INSERT dbo.Users OFF;
GO

-- 4. USER ROLES
INSERT INTO dbo.UserRoles (UserID, RoleID) VALUES
(1, 1), -- admin@watchstore.vn -> ADMIN
(2, 2), -- employee@watchstore.vn -> EMPLOYEE
(3, 2), -- sales@watchstore.vn -> EMPLOYEE
(4, 2), -- sales2@watchstore.vn -> EMPLOYEE
(5, 3), -- customer@watchstore.vn -> CUSTOMER
(6, 3), -- an.nguyen@example.com -> CUSTOMER
(7, 3), -- duc.tran@example.com -> CUSTOMER
(8, 3); -- cong.le@example.com -> CUSTOMER
GO

-- 5. USER PERMISSIONS
INSERT INTO dbo.UserPermissions (UserID, PermissionID)
SELECT 2, PermissionID FROM dbo.Permissions WHERE ModuleGroup IN ('PRODUCT', 'ORDER', 'CUSTOMER', 'WARRANTY', 'REPORT');

INSERT INTO dbo.UserPermissions (UserID, PermissionID)
SELECT 3, PermissionID FROM dbo.Permissions WHERE ModuleGroup IN ('PRODUCT', 'ORDER', 'CUSTOMER', 'WARRANTY', 'REPORT');
GO

-- 6. BRANDS
SET IDENTITY_INSERT dbo.Brands ON;
INSERT INTO dbo.Brands (BrandID, BrandCode, BrandName, BrandSlug, Country, LogoUrl, Description) VALUES
(1, 'SEIKO',    N'Seiko',    'seiko',    N'Nhật Bản', '/assets/images/brands/seiko.png',    N'Thương hiệu đồng hồ nổi tiếng Nhật Bản với lịch sử hơn 100 năm.'),
(2, 'ORIENT',   N'Orient',   'orient',   N'Nhật Bản', '/assets/images/brands/orient.png',   N'Nổi tiếng với các dòng đồng hồ cơ Automatic chất lượng cao.'),
(3, 'CASIO',    N'Casio',    'casio',    N'Nhật Bản', '/assets/images/brands/casio.png',    N'Đồng hồ thể thao, G-Shock bền bỉ hàng đầu.'),
(4, 'TISSOT',   N'Tissot',   'tissot',   N'Thụy Sỹ',  '/assets/images/brands/tissot.png',   N'Đồng hồ Thụy Sỹ đẳng cấp với bộ máy Powermatic 80 ấn tượng.'),
(5, 'CITIZEN',  N'Citizen',  'citizen',  N'Nhật Bản', '/assets/images/brands/citizen.png',  N'Tiên phong với công nghệ năng lượng ánh sáng Eco-Drive.'),
(6, 'LONGINES', N'Longines', 'longines', N'Thụy Sỹ',  '/assets/images/brands/longines.png', N'Thương hiệu đồng hồ cao cấp sang trọng của Thụy Sỹ.');
SET IDENTITY_INSERT dbo.Brands OFF;
GO

-- 7. CATEGORIES
SET IDENTITY_INSERT dbo.Categories ON;
INSERT INTO dbo.Categories (CategoryID, CategoryCode, CategoryName, CategorySlug, Description, DisplayOrder) VALUES
(1, 'MEN',       N'Đồng Hồ Nam',          'dong-ho-nam',          N'Bộ sưu tập đồng hồ nam cao cấp, lịch lãm', 1),
(2, 'AUTOMATIC', N'Đồng Hồ Cơ (Automatic)','dong-ho-co-automatic', N'Đồng hồ cơ tự động tinh xảo', 2),
(3, 'QUARTZ',    N'Đồng Hồ Pin (Quartz)',   'dong-ho-pin-quartz',   N'Đồng hồ máy Quartz chính xác cao', 3);
SET IDENTITY_INSERT dbo.Categories OFF;
GO

-- 8. PRODUCTS
SET IDENTITY_INSERT dbo.Products ON;
INSERT INTO dbo.Products (ProductID, ProductCode, ProductName, ProductSlug, BrandID, CategoryID, MovementType, Gender, ShortDescription, Description, CaseMaterial, GlassMaterial, StrapMaterial, WaterResistance, OriginCountry, WarrantyMonths, Status, IsFeatured, RatingAverage, RatingCount, CreatedBy) VALUES
(1, 'SRPD37J1',            N'Seiko Presage Cocktail Time SRPD37J1',        'seiko-presage-cocktail-time-srpd37j1',        1, 1, 'AUTOMATIC', 'MEN', N'Mặt số xanh ngọc lục bảo quyến rũ, bộ máy 4R35 tự động', N'Seiko Presage SRPD37J1 sở hữu thiết kế lấy cảm hứng từ những ly Cocktail thanh lịch tại quầy bar Tokyo.', N'Thép không gỉ 316L', N'Kính Hardlex cong', N'Dây da cao cấp', N'50m (5 ATM)', N'Nhật Bản', 24, 'ACTIVE', 1, 4.90, 15, 1),
(2, 'FAC00005W0',          N'Orient Bambino Gen 2 FAC00005W0',             'orient-bambino-gen-2-fac00005w0',             2, 1, 'AUTOMATIC', 'MEN', N'Kính vòm cổ điển, kim xanh nổi bật trên nền mặt trắng', N'Orient Bambino Gen 2 Ver 2 FAC00005W0 là mẫu đồng hồ dress watch cơ huyền thoại trong tầm giá.', N'Thép không gỉ 316L', N'Kính khoáng vòm (Mineral Crystal)', N'Dây da nâu', N'30m (3 ATM)', N'Nhật Bản', 24, 'ACTIVE', 1, 4.80, 22, 1),
(3, 'GA-2100-1A1',         N'Casio G-Shock GA-2100-1A1DR (CasiOak)',       'casio-g-shock-ga-2100-1a1dr',                 3, 1, 'QUARTZ',    'MEN', N'Thiết kế bát giác siêu mỏng, vỏ Carbon Core Guard', N'Dòng G-Shock GA-2100 đen nguyên khối thiết kế thể thao góc cạnh cá tính.', N'Nhựa gia cường Carbon', N'Kính khoáng (Mineral Glass)', N'Dây nhựa cao cấp', N'200m (20 ATM)', N'Nhật Bản', 12, 'ACTIVE', 1, 5.00, 30, 1),
(4, 'T063.907.11.038.00',  N'Tissot Tradition Open Heart T0639071103800',  'tissot-tradition-open-heart-t0639071103800',  4, 1, 'AUTOMATIC', 'MEN', N'Lộ cơ góc 12h tinh tế, bộ máy Powermatic 80 trữ cót 80 giờ', N'Tissot Open Heart Thụy Sỹ mang nét đẹp cổ điển hòa quyện cùng phong cách hiện đại.', N'Thép không gỉ 316L', N'Kính Sapphire chống xước', N'Dây thép không gỉ', N'30m (3 ATM)', N'Thụy Sỹ', 36, 'ACTIVE', 1, 4.95, 18, 1);
SET IDENTITY_INSERT dbo.Products OFF;
GO

-- 9. PRODUCT IMAGES
SET IDENTITY_INSERT dbo.ProductImages ON;
INSERT INTO dbo.ProductImages (ProductImageID, ProductID, ImageUrl, AltText, IsPrimary, DisplayOrder) VALUES
(1, 1, '/assets/images/products/seiko-srpd37j1-1.jpg', N'Ảnh chính Seiko Presage SRPD37J1', 1, 1),
(2, 1, '/assets/images/products/seiko-srpd37j1-2.jpg', N'Ảnh mặt sau Seiko Presage SRPD37J1', 0, 2),
(3, 2, '/assets/images/products/orient-fac00005w0-1.jpg', N'Ảnh chính Orient Bambino FAC00005W0', 1, 1),
(4, 3, '/assets/images/products/casio-ga-2100-1a1-1.jpg', N'Ảnh chính Casio G-Shock GA-2100-1A1', 1, 1),
(5, 4, '/assets/images/products/tissot-t0639071103800-1.jpg', N'Ảnh chính Tissot Open Heart', 1, 1);
SET IDENTITY_INSERT dbo.ProductImages OFF;
GO

-- 10. PRODUCT VARIANTS
SET IDENTITY_INSERT dbo.ProductVariants ON;
INSERT INTO dbo.ProductVariants (VariantID, ProductID, SKU, Barcode, VariantName, CostPrice, SalePrice, CompareAtPrice, WeightGram, Status) VALUES
(1, 1, 'SRPD37J1-STD',       '4954628230554', N'Mặt Xanh - Dây Da Nâu',                   8500000,  11500000, 13000000, 150, 'ACTIVE'),
(2, 2, 'FAC00005W0-STD',     '4942715010041', N'Mặt Trắng - Dây Da Nâu Kim Xanh',          4200000,  5800000,  6500000,  140, 'ACTIVE'),
(3, 3, 'GA-2100-1A1-STD',    '4549526241698', N'Màu Đen Nguyên Khối (All Black)',          2500000,  3800000,  4200000,  80,  'ACTIVE'),
(4, 4, 'T0639071103800-STD', '7612345678901', N'Mặt Trắng Lộ Cơ - Dây Kim Loại',            14500000, 20500000, 23000000, 180, 'ACTIVE');
SET IDENTITY_INSERT dbo.ProductVariants OFF;
GO

-- 11. WAREHOUSES
SET IDENTITY_INSERT dbo.Warehouses ON;
INSERT INTO dbo.Warehouses (WarehouseID, WarehouseCode, WarehouseName, Address, ManagerID, Status) VALUES
(1, 'WH-HN-01',  N'Kho Tổng Hà Nội',          N'Số 154 Cầu Giấy, Q. Cầu Giấy, Hà Nội', 4, 'ACTIVE'),
(2, 'WH-HCM-01', N'Kho Chi Nhánh TP.HCM',      N'Số 285 Cách Mạng Tháng 8, Q.10, TP.HCM', 4, 'ACTIVE');
SET IDENTITY_INSERT dbo.Warehouses OFF;
GO

-- 12. INVENTORY BALANCES
INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel) VALUES
(1, 1, 25, 2, 5),
(1, 2, 40, 0, 5),
(1, 3, 50, 5, 10),
(1, 4, 15, 1, 3),
(2, 1, 10, 0, 3),
(2, 2, 15, 1, 3),
(2, 3, 30, 2, 5);
GO

-- 13. VOUCHERS
SET IDENTITY_INSERT dbo.Vouchers ON;
INSERT INTO dbo.Vouchers (VoucherID, VoucherCode, VoucherName, Description, DiscountType, DiscountValue, MinimumOrderValue, MaximumDiscount, UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, IsPublic, Status, CreatedBy) VALUES
(1, 'WELCOME100', N'Giảm 100K Cho Đơn Hàng Đầu Tiên', N'Ưu đãi dành riêng cho khách hàng mới', 'FIXED',    100000, 2000000, 100000,  100, 1, 5,  DATEADD(DAY, -10, SYSDATETIME()), DATEADD(DAY, 90, SYSDATETIME()), 1, 'ACTIVE', 1),
(2, 'SUMMER2026', N'Giảm 10% Tối Đa 1 Triệu',         N'Chương trình khuyến mãi mùa hè 2026',  'PERCENT',  10,     5000000, 1000000, 50,  1, 12, DATEADD(DAY, -5,  SYSDATETIME()), DATEADD(DAY, 30, SYSDATETIME()), 1, 'ACTIVE', 1),
(3, 'FREESHIP50', N'Miễn Phí Vận Chuyển 50K',         N'Áp dụng cho mọi đơn hàng từ 1 triệu',   'FREESHIP', 50000,  1000000, 50000,   200, 2, 25, DATEADD(DAY, -15, SYSDATETIME()), DATEADD(DAY, 60, SYSDATETIME()), 1, 'ACTIVE', 1);
SET IDENTITY_INSERT dbo.Vouchers OFF;
GO

-- 14. BANNERS (BỔ SUNG SEED BANNERS TỪ LEGACY SOURCE)
SET IDENTITY_INSERT dbo.Banners ON;
INSERT INTO dbo.Banners (BannerID, BannerName, Title, Subtitle, ImageUrl, TargetUrl, PositionCode, DisplayOrder, Status) VALUES
(1, N'Banner Hero 1 - Seiko Presage', N'ĐẲNG CẤP ĐỒNG HỒ CƠ NHẬT BẢN', N'Bộ sưu tập Seiko Presage Cocktail Time chính hãng giảm tới 20%', '/assets/images/banners/banner1.jpg', '/page/product-detail?id=1', 'HOME_HERO', 1, 'ACTIVE'),
(2, N'Banner Hero 2 - Tissot Swiss', N'TISSOT POWERMATIC 80 - THỤY SỸ', N'Trữ cót 80 giờ, kính Sapphire chống xước tuyệt đối', '/assets/images/banners/banner2.jpg', '/page/product-detail?id=4', 'HOME_HERO', 2, 'ACTIVE'),
(3, N'Banner Hero 3 - CasiOak', N'CASIO G-SHOCK GA-2100 CASIOAK', N'Biểu tượng thể thao góc cạnh siêu bền bỉ', '/assets/images/banners/banner3.jpg', '/page/product-detail?id=3', 'HOME_HERO', 3, 'ACTIVE');
SET IDENTITY_INSERT dbo.Banners OFF;
GO

-- 15. ORDERS & ORDER ITEMS
SET IDENTITY_INSERT dbo.Orders ON;
INSERT INTO dbo.Orders (OrderID, OrderCode, CustomerID, RecipientName, RecipientPhone, ShippingAddress, OrderStatus, PaymentStatus, SubtotalAmount, DiscountAmount, TotalAmount, CreatedAt) VALUES
(1, 'WS8501', 5, N'Nguyễn Văn Lợn', '0988000005', N'123 Nguyễn Trãi, Thanh Xuân, Hà Nội', 'COMPLETED', 'PAID',   11500000, 0, 11500000, DATEADD(DAY, -5, SYSDATETIME())),
(2, 'WS8502', 5, N'Lê Thị Thơm', '0988000005', N'123 Nguyễn Trãi, Thanh Xuân, Hà Nội', 'CONFIRMED', 'UNPAID', 5800000,  0, 5800000,  DATEADD(DAY, -1, SYSDATETIME()));
SET IDENTITY_INSERT dbo.Orders OFF;
GO

SET IDENTITY_INSERT dbo.OrderItems ON;
INSERT INTO dbo.OrderItems (OrderItemID, OrderID, VariantID, ProductName, VariantName, SKU, ImageUrl, UnitPrice, Quantity, DiscountAmount) VALUES
(1, 1, 1, N'Seiko Presage Cocktail Time SRPD37J1', N'Mặt Xanh - Dây Da Nâu',          'SRPD37J1-STD',   '/assets/images/products/seiko-srpd37j1-1.jpg', 11500000, 1, 0),
(2, 2, 2, N'Orient Bambino Gen 2 FAC00005W0',      N'Mặt Trắng - Dây Da Nâu Kim Xanh', 'FAC00005W0-STD', '/assets/images/products/orient-fac00005w0-1.jpg', 5800000,  1, 0);
SET IDENTITY_INSERT dbo.OrderItems OFF;
GO

-- 16. RETURNS & WARRANTIES
SET IDENTITY_INSERT dbo.Returns ON;
INSERT INTO dbo.Returns (ReturnID, OrderID, OrderItemID, CustomerID, ProductName, Reason, Status, RefundStatus, EmployeeNote, CreatedAt) VALUES
(1, 1, 1, 5, N'Seiko Presage Cocktail Time SRPD37J1', N'Sản phẩm không vừa cổ tay', 'PENDING', 'UNREFUNDED', N'Khách hàng muốn đổi mẫu khác', DATEADD(DAY, -2, SYSDATETIME()));
SET IDENTITY_INSERT dbo.Returns OFF;
GO

SET IDENTITY_INSERT dbo.Warranties ON;
INSERT INTO dbo.Warranties (WarrantyID, OrderID, ProductName, SerialNumber, WarrantyMonths, StartDate, EndDate, Status, Note, ReceiveDate, ReceiveNote, CreatedAt) VALUES
(1, 1, N'Seiko Presage Cocktail Time SRPD37J1', 'SN-SEIKO-8899', 24, '2026-08-10', '2028-08-10', N'Đang bảo hành', N'Phiếu bảo hành chính hãng 24 tháng', '2026-08-15', N'Kiểm tra định kỳ', SYSDATETIME());
SET IDENTITY_INSERT dbo.Warranties OFF;
GO

PRINT N'================================================================================';
PRINT N'  ĐỒNG BỘ DATABASE WATCHSTORE_FINAL HOÀN THÀNH VÀ SẴN SÀNG SỬ DỤNG!';
PRINT N'================================================================================';

use master 
alter database WatchStore set single_user with rollback immediate;
drop database WatchStore;

UPDATE dbo.Users
SET PasswordHash = '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92'
WHERE Email IN (
    'admin@watchstore.vn',
    'employee@watchstore.vn',
    'sales@watchstore.vn',
    'sales2@watchstore.vn',
    'customer@watchstore.vn',
    'an.nguyen@example.com',
    'duc.tran@example.com',
    'cong.le@example.com'
);

SELECT UserID, Email, PasswordHash, Status
FROM dbo.Users
WHERE Email IN (
    'admin@watchstore.vn',
    'employee@watchstore.vn',
    'sales@watchstore.vn',
    'sales2@watchstore.vn',
    'customer@watchstore.vn',
    'an.nguyen@example.com',
    'duc.tran@example.com',
    'cong.le@example.com'
);

SELECT
    u.UserID,
    u.Email,
    r.RoleCode
FROM dbo.Users u
JOIN dbo.UserRoles ur ON ur.UserID = u.UserID
JOIN dbo.Roles r ON r.RoleID = ur.RoleID
WHERE u.Email = 'admin@watchstore.vn';