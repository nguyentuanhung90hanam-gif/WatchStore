/*
================================================================================
    WATCHSTORE - FULL PRODUCTION-READY DATABASE FOR SQL SERVER
    Tương thích: SQL Server 2019 / SQL Server 2022 / SSMS
    Tên Database khớp với DBContext.java: WatchStore

    Mật khẩu mặc định cho các tài khoản demo: 123456
    PasswordHash: SHA2-256 mã hóa text minh họa.
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
GO

/* ============================================================================
   1. XÓA CÁC OBJECT CŨ (NẾU CÓ) ĐỂ TÁI TẠO SẠCH SẼ
   ============================================================================ */
DROP VIEW IF EXISTS dbo.vw_LowStock;
DROP VIEW IF EXISTS dbo.vw_TopSellingProducts;
DROP VIEW IF EXISTS dbo.vw_DashboardSalesDaily;
DROP VIEW IF EXISTS dbo.vw_CustomerSummary;
DROP VIEW IF EXISTS dbo.vw_OrderManagement;
DROP VIEW IF EXISTS dbo.vw_InventoryOverview;
DROP VIEW IF EXISTS dbo.vw_ProductCatalog;
GO

DROP PROCEDURE IF EXISTS dbo.sp_CreateOrderFromCart;
DROP PROCEDURE IF EXISTS dbo.sp_ClaimVoucher;
DROP PROCEDURE IF EXISTS dbo.sp_UpdateOrderStatus;
DROP PROCEDURE IF EXISTS dbo.sp_RecordInventoryTransaction;
DROP PROCEDURE IF EXISTS dbo.sp_AddToCart;
GO

DROP FUNCTION IF EXISTS dbo.fn_CalculateVoucherDiscount;
GO

DROP TABLE IF EXISTS dbo.AuditLogs;
DROP TABLE IF EXISTS dbo.SystemSettings;
DROP TABLE IF EXISTS dbo.Notifications;
DROP TABLE IF EXISTS dbo.StockAlertRules;
DROP TABLE IF EXISTS dbo.UserNotifications;
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
DROP TABLE IF EXISTS dbo.PostComments;
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
DROP TABLE IF EXISTS dbo.UserRoles;
DROP TABLE IF EXISTS dbo.RolePermissions;
DROP TABLE IF EXISTS dbo.Permissions;
DROP TABLE IF EXISTS dbo.Roles;
DROP TABLE IF EXISTS dbo.Users;
GO

DROP SEQUENCE IF EXISTS dbo.OrderNumberSequence;
GO

/* ============================================================================
   2. TÀI KHOẢN VÀ PHÂN QUYỀN (ACCOUNT & AUTHORIZATION)
   ============================================================================ */
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
    EmailVerifiedAt     DATETIME2 NULL,
    LastLoginAt         DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Users_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Users_Email UNIQUE (Email),
    CONSTRAINT CK_Users_Gender CHECK (Gender IS NULL OR Gender IN ('MALE','FEMALE','OTHER')),
    CONSTRAINT CK_Users_Status CHECK (Status IN ('ACTIVE','INACTIVE','LOCKED'))
);
GO
-- Chỉ mục duy nhất lọc NULL cho SĐT người dùng
CREATE UNIQUE INDEX UX_Users_Phone ON dbo.Users(Phone) WHERE Phone IS NOT NULL;
GO

CREATE TABLE dbo.Roles (
    RoleID              INT IDENTITY(1,1) PRIMARY KEY,
    RoleCode            VARCHAR(30) NOT NULL,
    RoleName            NVARCHAR(100) NOT NULL,
    Description         NVARCHAR(500) NULL,
    IsSystem            BIT NOT NULL CONSTRAINT DF_Roles_IsSystem DEFAULT 0,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Roles_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Roles_Code UNIQUE (RoleCode)
);
GO

CREATE TABLE dbo.Permissions (
    PermissionID        INT IDENTITY(1,1) PRIMARY KEY,
    PermissionCode      VARCHAR(60) NOT NULL,
    PermissionName      NVARCHAR(150) NOT NULL,
    ModuleCode          VARCHAR(40) NOT NULL,
    Description         NVARCHAR(500) NULL,
    CONSTRAINT UQ_Permissions_Code UNIQUE (PermissionCode)
);
GO

CREATE TABLE dbo.RolePermissions (
    RoleID              INT NOT NULL,
    PermissionID        INT NOT NULL,
    GrantedAt           DATETIME2 NOT NULL CONSTRAINT DF_RolePermissions_GrantedAt DEFAULT SYSDATETIME(),
    CONSTRAINT PK_RolePermissions PRIMARY KEY (RoleID, PermissionID),
    CONSTRAINT FK_RolePermissions_Role FOREIGN KEY (RoleID) REFERENCES dbo.Roles(RoleID) ON DELETE CASCADE,
    CONSTRAINT FK_RolePermissions_Permission FOREIGN KEY (PermissionID) REFERENCES dbo.Permissions(PermissionID) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.UserRoles (
    UserID              INT NOT NULL,
    RoleID              INT NOT NULL,
    AssignedBy          INT NULL,
    AssignedAt          DATETIME2 NOT NULL CONSTRAINT DF_UserRoles_AssignedAt DEFAULT SYSDATETIME(),
    CONSTRAINT PK_UserRoles PRIMARY KEY (UserID, RoleID),
    CONSTRAINT FK_UserRoles_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE,
    CONSTRAINT FK_UserRoles_Role FOREIGN KEY (RoleID) REFERENCES dbo.Roles(RoleID),
    CONSTRAINT FK_UserRoles_AssignedBy FOREIGN KEY (AssignedBy) REFERENCES dbo.Users(UserID)
);
GO

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
    CONSTRAINT FK_UserAddresses_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE,
    CONSTRAINT CK_UserAddresses_Type CHECK (AddressType IN ('HOME','OFFICE','OTHER'))
);
GO

CREATE TABLE dbo.CustomerNotes (
    CustomerNoteID      BIGINT IDENTITY(1,1) PRIMARY KEY,
    CustomerID          INT NOT NULL,
    StaffID             INT NOT NULL,
    NoteContent         NVARCHAR(1000) NOT NULL,
    IsImportant         BIT NOT NULL CONSTRAINT DF_CustomerNotes_Important DEFAULT 0,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_CustomerNotes_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_CustomerNotes_Customer FOREIGN KEY (CustomerID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_CustomerNotes_Staff FOREIGN KEY (StaffID) REFERENCES dbo.Users(UserID)
);
GO

/* ============================================================================
   3. DANH MỤC SẢN PHẨM (PRODUCT CATALOG)
   ============================================================================ */
CREATE TABLE dbo.Brands (
    BrandID             INT IDENTITY(1,1) PRIMARY KEY,
    BrandCode           VARCHAR(40) NOT NULL,
    BrandName           NVARCHAR(120) NOT NULL,
    Slug                VARCHAR(150) NOT NULL,
    OriginCountry       NVARCHAR(100) NULL,
    LogoUrl             NVARCHAR(500) NULL,
    Description         NVARCHAR(MAX) NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Brands_Status DEFAULT 'ACTIVE',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Brands_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Brands_Code UNIQUE (BrandCode),
    CONSTRAINT UQ_Brands_Slug UNIQUE (Slug),
    CONSTRAINT CK_Brands_Status CHECK (Status IN ('ACTIVE','INACTIVE'))
);
GO

CREATE TABLE dbo.Categories (
    CategoryID          INT IDENTITY(1,1) PRIMARY KEY,
    ParentCategoryID    INT NULL,
    CategoryCode        VARCHAR(40) NOT NULL,
    CategoryName        NVARCHAR(120) NOT NULL,
    Slug                VARCHAR(150) NOT NULL,
    Description         NVARCHAR(1000) NULL,
    ImageUrl            NVARCHAR(500) NULL,
    DisplayOrder        INT NOT NULL CONSTRAINT DF_Categories_Order DEFAULT 0,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Categories_Status DEFAULT 'ACTIVE',
    CONSTRAINT UQ_Categories_Code UNIQUE (CategoryCode),
    CONSTRAINT UQ_Categories_Slug UNIQUE (Slug),
    CONSTRAINT FK_Categories_Parent FOREIGN KEY (ParentCategoryID) REFERENCES dbo.Categories(CategoryID),
    CONSTRAINT CK_Categories_Status CHECK (Status IN ('ACTIVE','INACTIVE'))
);
GO

CREATE TABLE dbo.Products (
    ProductID           INT IDENTITY(1,1) PRIMARY KEY,
    ProductCode         VARCHAR(50) NOT NULL,
    ProductName         NVARCHAR(250) NOT NULL,
    Slug                VARCHAR(300) NOT NULL,
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
    CONSTRAINT UQ_Products_Slug UNIQUE (Slug),
    CONSTRAINT FK_Products_Brand FOREIGN KEY (BrandID) REFERENCES dbo.Brands(BrandID),
    CONSTRAINT FK_Products_Category FOREIGN KEY (CategoryID) REFERENCES dbo.Categories(CategoryID),
    CONSTRAINT FK_Products_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Products_Movement CHECK (MovementType IN ('AUTOMATIC','QUARTZ','SOLAR','SMART','MECHANICAL')),
    CONSTRAINT CK_Products_Gender CHECK (Gender IN ('MEN','WOMEN','UNISEX','COUPLE')),
    CONSTRAINT CK_Products_Status CHECK (Status IN ('DRAFT','ACTIVE','INACTIVE','DISCONTINUED')),
    CONSTRAINT CK_Products_Warranty CHECK (WarrantyMonths >= 0),
    CONSTRAINT CK_Products_Rating CHECK (RatingAverage BETWEEN 0 AND 5)
);
GO

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

CREATE TABLE dbo.ProductAttributes (
    AttributeID         INT IDENTITY(1,1) PRIMARY KEY,
    AttributeCode       VARCHAR(50) NOT NULL,
    AttributeName       NVARCHAR(100) NOT NULL,
    DisplayOrder        INT NOT NULL CONSTRAINT DF_ProductAttributes_Order DEFAULT 0,
    CONSTRAINT UQ_ProductAttributes_Code UNIQUE (AttributeCode)
);
GO

CREATE TABLE dbo.ProductAttributeValues (
    AttributeValueID    INT IDENTITY(1,1) PRIMARY KEY,
    AttributeID         INT NOT NULL,
    ValueCode           VARCHAR(60) NOT NULL,
    ValueName           NVARCHAR(120) NOT NULL,
    ColorHex            VARCHAR(10) NULL,
    DisplayOrder        INT NOT NULL CONSTRAINT DF_ProductAttributeValues_Order DEFAULT 0,
    CONSTRAINT UQ_ProductAttributeValues UNIQUE (AttributeID, ValueCode),
    CONSTRAINT FK_ProductAttributeValues_Attribute FOREIGN KEY (AttributeID) REFERENCES dbo.ProductAttributes(AttributeID) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.ProductVariants (
    VariantID           INT IDENTITY(1,1) PRIMARY KEY,
    ProductID           INT NOT NULL,
    SKU                 VARCHAR(80) NOT NULL,
    Barcode             VARCHAR(80) NULL,
    VariantName         NVARCHAR(200) NOT NULL,
    CostPrice           DECIMAL(18,2) NOT NULL,
    SalePrice           DECIMAL(18,2) NOT NULL,
    CompareAtPrice      DECIMAL(18,2) NULL,
    WeightGram          INT NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_ProductVariants_Status DEFAULT 'ACTIVE',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_ProductVariants_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_ProductVariants_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_ProductVariants_SKU UNIQUE (SKU),
    CONSTRAINT FK_ProductVariants_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE,
    CONSTRAINT CK_ProductVariants_Price CHECK (CostPrice >= 0 AND SalePrice >= 0 AND (CompareAtPrice IS NULL OR CompareAtPrice >= 0)),
    CONSTRAINT CK_ProductVariants_Status CHECK (Status IN ('ACTIVE','INACTIVE','OUT_OF_STOCK'))
);
GO

-- Chỉ mục duy nhất lọc NULL cho Barcode biến thể
CREATE UNIQUE INDEX UX_ProductVariants_Barcode ON dbo.ProductVariants(Barcode) WHERE Barcode IS NOT NULL;
GO

CREATE TABLE dbo.VariantAttributeValues (
    VariantID           INT NOT NULL,
    AttributeValueID    INT NOT NULL,
    CONSTRAINT PK_VariantAttributeValues PRIMARY KEY (VariantID, AttributeValueID),
    CONSTRAINT FK_VariantAttributeValues_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID) ON DELETE CASCADE,
    CONSTRAINT FK_VariantAttributeValues_Value FOREIGN KEY (AttributeValueID) REFERENCES dbo.ProductAttributeValues(AttributeValueID)
);
GO

/* ============================================================================
   4. NỘI DUNG, BANNER VÀ TIN TỨC
   ============================================================================ */
CREATE TABLE dbo.Banners (
    BannerID            INT IDENTITY(1,1) PRIMARY KEY,
    BannerName          NVARCHAR(150) NOT NULL,
    Title               NVARCHAR(250) NOT NULL,
    Subtitle            NVARCHAR(500) NULL,
    ImageUrl            NVARCHAR(500) NOT NULL,
    TargetUrl           NVARCHAR(500) NULL,
    PositionCode        VARCHAR(40) NOT NULL CONSTRAINT DF_Banners_Position DEFAULT 'HOME_HERO',
    DisplayOrder        INT NOT NULL CONSTRAINT DF_Banners_Order DEFAULT 0,
    StartAt             DATETIME2 NULL,
    EndAt               DATETIME2 NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Banners_Status DEFAULT 'ACTIVE',
    CreatedBy           INT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Banners_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Banners_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Banners_Status CHECK (Status IN ('DRAFT','ACTIVE','INACTIVE')),
    CONSTRAINT CK_Banners_Dates CHECK (EndAt IS NULL OR StartAt IS NULL OR EndAt > StartAt)
);
GO

CREATE TABLE dbo.Posts (
    PostID              INT IDENTITY(1,1) PRIMARY KEY,
    PostType            VARCHAR(30) NOT NULL CONSTRAINT DF_Posts_Type DEFAULT 'NEWS',
    Title               NVARCHAR(300) NOT NULL,
    Slug                VARCHAR(320) NOT NULL,
    Summary             NVARCHAR(1000) NULL,
    Content             NVARCHAR(MAX) NOT NULL,
    ThumbnailUrl        NVARCHAR(500) NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Posts_Status DEFAULT 'DRAFT',
    AuthorID            INT NOT NULL,
    PublishedAt         DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Posts_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Posts_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Posts_Slug UNIQUE (Slug),
    CONSTRAINT FK_Posts_Author FOREIGN KEY (AuthorID) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Posts_Type CHECK (PostType IN ('NEWS','GUIDE','PROMOTION','POLICY')),
    CONSTRAINT CK_Posts_Status CHECK (Status IN ('DRAFT','PUBLISHED','HIDDEN'))
);
GO

CREATE TABLE dbo.PostComments (
    PostCommentID       BIGINT IDENTITY(1,1) PRIMARY KEY,
    PostID              INT NOT NULL,
    UserID              INT NULL,
    ParentCommentID     BIGINT NULL,
    GuestName           NVARCHAR(150) NULL,
    GuestEmail          VARCHAR(150) NULL,
    CommentContent      NVARCHAR(1500) NOT NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_PostComments_Status DEFAULT 'PENDING',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_PostComments_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_PostComments_Post FOREIGN KEY (PostID) REFERENCES dbo.Posts(PostID) ON DELETE CASCADE,
    CONSTRAINT FK_PostComments_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_PostComments_Parent FOREIGN KEY (ParentCommentID) REFERENCES dbo.PostComments(PostCommentID),
    CONSTRAINT CK_PostComments_Status CHECK (Status IN ('PENDING','APPROVED','REJECTED','HIDDEN'))
);
GO

/* ============================================================================
   5. VOUCHERS VÀ KHUYẾN MÃI
   ============================================================================ */
CREATE TABLE dbo.Vouchers (
    VoucherID           INT IDENTITY(1,1) PRIMARY KEY,
    VoucherCode         VARCHAR(50) NOT NULL,
    VoucherName         NVARCHAR(200) NOT NULL,
    Description         NVARCHAR(1000) NULL,
    DiscountType        VARCHAR(20) NOT NULL,
    DiscountValue       DECIMAL(18,2) NOT NULL,
    MaximumDiscount     DECIMAL(18,2) NULL,
    MinimumOrderValue   DECIMAL(18,2) NOT NULL CONSTRAINT DF_Vouchers_Minimum DEFAULT 0,
    UsageLimit          INT NULL,
    UsageLimitPerUser   INT NOT NULL CONSTRAINT DF_Vouchers_PerUser DEFAULT 1,
    UsedCount           INT NOT NULL CONSTRAINT DF_Vouchers_Used DEFAULT 0,
    StartAt             DATETIME2 NOT NULL,
    EndAt               DATETIME2 NOT NULL,
    IsPublic            BIT NOT NULL CONSTRAINT DF_Vouchers_Public DEFAULT 1,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Vouchers_Status DEFAULT 'ACTIVE',
    CreatedBy           INT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Vouchers_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Vouchers_Code UNIQUE (VoucherCode),
    CONSTRAINT FK_Vouchers_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Vouchers_Type CHECK (DiscountType IN ('PERCENT','FIXED','FREESHIP')),
    CONSTRAINT CK_Vouchers_Value CHECK (DiscountValue >= 0 AND MinimumOrderValue >= 0),
    CONSTRAINT CK_Vouchers_Dates CHECK (EndAt > StartAt),
    CONSTRAINT CK_Vouchers_Status CHECK (Status IN ('DRAFT','ACTIVE','INACTIVE','EXPIRED'))
);
GO

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
    AssignedAt          DATETIME2 NOT NULL CONSTRAINT DF_VoucherUsers_AssignedAt DEFAULT SYSDATETIME(),
    ClaimedAt           DATETIME2 NULL,
    CONSTRAINT PK_VoucherUsers PRIMARY KEY (VoucherID, UserID),
    CONSTRAINT FK_VoucherUsers_Voucher FOREIGN KEY (VoucherID) REFERENCES dbo.Vouchers(VoucherID) ON DELETE CASCADE,
    CONSTRAINT FK_VoucherUsers_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
);
GO

/* ============================================================================
   6. GIỎ HÀNG VÀ YÊU THÍCH (CART & WISHLIST)
   ============================================================================ */
CREATE TABLE dbo.Carts (
    CartID              BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID              INT NULL,
    GuestToken          VARCHAR(100) NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Carts_Status DEFAULT 'ACTIVE',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Carts_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Carts_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Carts_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE,
    CONSTRAINT CK_Carts_Owner CHECK (UserID IS NOT NULL OR GuestToken IS NOT NULL),
    CONSTRAINT CK_Carts_Status CHECK (Status IN ('ACTIVE','CHECKED_OUT','ABANDONED'))
);
GO

CREATE UNIQUE INDEX UX_Carts_ActiveUser ON dbo.Carts(UserID) WHERE UserID IS NOT NULL AND Status = 'ACTIVE';
CREATE UNIQUE INDEX UX_Carts_ActiveGuest ON dbo.Carts(GuestToken) WHERE GuestToken IS NOT NULL AND Status = 'ACTIVE';
GO

CREATE TABLE dbo.CartItems (
    CartItemID          BIGINT IDENTITY(1,1) PRIMARY KEY,
    CartID              BIGINT NOT NULL,
    VariantID           INT NOT NULL,
    Quantity            INT NOT NULL,
    AddedAt             DATETIME2 NOT NULL CONSTRAINT DF_CartItems_AddedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_CartItems_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_CartItems UNIQUE (CartID, VariantID),
    CONSTRAINT FK_CartItems_Cart FOREIGN KEY (CartID) REFERENCES dbo.Carts(CartID) ON DELETE CASCADE,
    CONSTRAINT FK_CartItems_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT CK_CartItems_Quantity CHECK (Quantity > 0)
);
GO

CREATE TABLE dbo.Wishlists (
    WishlistID          BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID              INT NOT NULL,
    WishlistName        NVARCHAR(100) NOT NULL CONSTRAINT DF_Wishlists_Name DEFAULT N'Yêu thích',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Wishlists_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Wishlists_UserName UNIQUE (UserID, WishlistName),
    CONSTRAINT FK_Wishlists_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.WishlistItems (
    WishlistID          BIGINT NOT NULL,
    ProductID           INT NOT NULL,
    AddedAt             DATETIME2 NOT NULL CONSTRAINT DF_WishlistItems_AddedAt DEFAULT SYSDATETIME(),
    CONSTRAINT PK_WishlistItems PRIMARY KEY (WishlistID, ProductID),
    CONSTRAINT FK_WishlistItems_Wishlist FOREIGN KEY (WishlistID) REFERENCES dbo.Wishlists(WishlistID) ON DELETE CASCADE,
    CONSTRAINT FK_WishlistItems_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE
);
GO

/* ============================================================================
   7. ĐƠN HÀNG, THANH TOÁN VÀ GIAO HÀNG (ORDERS & FULFILLMENT)
   ============================================================================ */
CREATE SEQUENCE dbo.OrderNumberSequence AS BIGINT START WITH 8500 INCREMENT BY 1;
GO

CREATE TABLE dbo.Orders (
    OrderID             BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderCode           VARCHAR(30) NOT NULL,
    CustomerID          INT NOT NULL,
    SalesStaffID        INT NULL,
    VoucherID           INT NULL,
    RecipientName       NVARCHAR(150) NOT NULL,
    RecipientPhone      VARCHAR(20) NOT NULL,
    ShippingAddress     NVARCHAR(500) NOT NULL,
    CustomerNote        NVARCHAR(1000) NULL,
    OrderStatus         VARCHAR(30) NOT NULL CONSTRAINT DF_Orders_Status DEFAULT 'PENDING',
    PaymentStatus       VARCHAR(30) NOT NULL CONSTRAINT DF_Orders_PaymentStatus DEFAULT 'UNPAID',
    SubtotalAmount      DECIMAL(18,2) NOT NULL,
    DiscountAmount      DECIMAL(18,2) NOT NULL CONSTRAINT DF_Orders_Discount DEFAULT 0,
    ShippingFee         DECIMAL(18,2) NOT NULL CONSTRAINT DF_Orders_Shipping DEFAULT 0,
    TaxAmount           DECIMAL(18,2) NOT NULL CONSTRAINT DF_Orders_Tax DEFAULT 0,
    TotalAmount         DECIMAL(18,2) NOT NULL,
    CancelReason        NVARCHAR(500) NULL,
    ConfirmedAt         DATETIME2 NULL,
    CompletedAt         DATETIME2 NULL,
    CancelledAt         DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Orders_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Orders_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Orders_Code UNIQUE (OrderCode),
    CONSTRAINT FK_Orders_Customer FOREIGN KEY (CustomerID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Orders_SalesStaff FOREIGN KEY (SalesStaffID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Orders_Voucher FOREIGN KEY (VoucherID) REFERENCES dbo.Vouchers(VoucherID),
    CONSTRAINT CK_Orders_Status CHECK (OrderStatus IN ('PENDING','CONFIRMED','PACKING','SHIPPING','DELIVERED','COMPLETED','CANCELLED','RETURNED')),
    CONSTRAINT CK_Orders_PaymentStatus CHECK (PaymentStatus IN ('UNPAID','PENDING','PAID','FAILED','REFUNDED','PARTIALLY_REFUNDED')),
    CONSTRAINT CK_Orders_Amounts CHECK (SubtotalAmount >= 0 AND DiscountAmount >= 0 AND ShippingFee >= 0 AND TaxAmount >= 0 AND TotalAmount >= 0)
);
GO

CREATE TABLE dbo.OrderItems (
    OrderItemID         BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderID             BIGINT NOT NULL,
    VariantID           INT NOT NULL,
    ProductName         NVARCHAR(250) NOT NULL,
    VariantName         NVARCHAR(200) NOT NULL,
    SKU                 VARCHAR(80) NOT NULL,
    ImageUrl            NVARCHAR(500) NULL,
    UnitPrice           DECIMAL(18,2) NOT NULL,
    Quantity            INT NOT NULL,
    DiscountAmount      DECIMAL(18,2) NOT NULL CONSTRAINT DF_OrderItems_Discount DEFAULT 0,
    LineTotal           AS ((UnitPrice * Quantity) - DiscountAmount) PERSISTED,
    CONSTRAINT FK_OrderItems_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID) ON DELETE CASCADE,
    CONSTRAINT FK_OrderItems_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT CK_OrderItems_Values CHECK (UnitPrice >= 0 AND Quantity > 0 AND DiscountAmount >= 0)
);
GO

CREATE TABLE dbo.OrderStatusHistory (
    OrderStatusHistoryID BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderID             BIGINT NOT NULL,
    OldStatus           VARCHAR(30) NULL,
    NewStatus           VARCHAR(30) NOT NULL,
    ChangedBy           INT NULL,
    Note                NVARCHAR(500) NULL,
    ChangedAt           DATETIME2 NOT NULL CONSTRAINT DF_OrderStatusHistory_ChangedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_OrderStatusHistory_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID) ON DELETE CASCADE,
    CONSTRAINT FK_OrderStatusHistory_User FOREIGN KEY (ChangedBy) REFERENCES dbo.Users(UserID)
);
GO

CREATE TABLE dbo.Payments (
    PaymentID           BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderID             BIGINT NOT NULL,
    PaymentMethod       VARCHAR(30) NOT NULL,
    TransactionCode     VARCHAR(100) NULL,
    Amount              DECIMAL(18,2) NOT NULL,
    PaymentStatus       VARCHAR(30) NOT NULL CONSTRAINT DF_Payments_Status DEFAULT 'PENDING',
    ProviderResponse    NVARCHAR(MAX) NULL,
    PaidAt              DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Payments_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Payments_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID) ON DELETE CASCADE,
    CONSTRAINT CK_Payments_Method CHECK (PaymentMethod IN ('COD','BANK_TRANSFER','CREDIT_CARD','MOMO','VNPAY')),
    CONSTRAINT CK_Payments_Status CHECK (PaymentStatus IN ('PENDING','PAID','FAILED','CANCELLED','REFUNDED')),
    CONSTRAINT CK_Payments_Amount CHECK (Amount >= 0)
);
GO
CREATE UNIQUE INDEX UX_Payments_Transaction ON dbo.Payments(TransactionCode) WHERE TransactionCode IS NOT NULL;
GO

CREATE TABLE dbo.Deliveries (
    DeliveryID          BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderID             BIGINT NOT NULL,
    ShippingProvider    NVARCHAR(100) NOT NULL,
    TrackingCode        VARCHAR(100) NULL,
    DeliveryStatus      VARCHAR(30) NOT NULL CONSTRAINT DF_Deliveries_Status DEFAULT 'READY_TO_PICK',
    ShippedAt           DATETIME2 NULL,
    EstimatedDeliveryAt DATETIME2 NULL,
    DeliveredAt         DATETIME2 NULL,
    ShippingNote        NVARCHAR(500) NULL,
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Deliveries_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Deliveries_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID) ON DELETE CASCADE,
    CONSTRAINT CK_Deliveries_Status CHECK (DeliveryStatus IN ('READY_TO_PICK','PICKED_UP','IN_TRANSIT','DELIVERED','FAILED','RETURNING','RETURNED'))
);
GO
CREATE UNIQUE INDEX UX_Deliveries_Tracking ON dbo.Deliveries(TrackingCode) WHERE TrackingCode IS NOT NULL;
GO

CREATE TABLE dbo.ReturnRequests (
    ReturnRequestID     BIGINT IDENTITY(1,1) PRIMARY KEY,
    ReturnCode          VARCHAR(30) NOT NULL,
    OrderID             BIGINT NOT NULL,
    CustomerID          INT NOT NULL,
    RequestType         VARCHAR(20) NOT NULL,
    Reason              NVARCHAR(1000) NOT NULL,
    EvidenceNote        NVARCHAR(1000) NULL,
    Status              VARCHAR(30) NOT NULL CONSTRAINT DF_ReturnRequests_Status DEFAULT 'PENDING',
    RefundAmount        DECIMAL(18,2) NOT NULL CONSTRAINT DF_ReturnRequests_Refund DEFAULT 0,
    ProcessedBy         INT NULL,
    ProcessedAt         DATETIME2 NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_ReturnRequests_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_ReturnRequests_Code UNIQUE (ReturnCode),
    CONSTRAINT FK_ReturnRequests_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID),
    CONSTRAINT FK_ReturnRequests_Customer FOREIGN KEY (CustomerID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_ReturnRequests_Staff FOREIGN KEY (ProcessedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_ReturnRequests_Type CHECK (RequestType IN ('RETURN','EXCHANGE','WARRANTY')),
    CONSTRAINT CK_ReturnRequests_Status CHECK (Status IN ('PENDING','APPROVED','REJECTED','RECEIVED','COMPLETED','CANCELLED'))
);
GO

CREATE TABLE dbo.ReturnItems (
    ReturnItemID        BIGINT IDENTITY(1,1) PRIMARY KEY,
    ReturnRequestID     BIGINT NOT NULL,
    OrderItemID         BIGINT NOT NULL,
    Quantity            INT NOT NULL,
    ItemCondition       VARCHAR(30) NULL,
    Resolution          VARCHAR(30) NULL,
    CONSTRAINT FK_ReturnItems_Request FOREIGN KEY (ReturnRequestID) REFERENCES dbo.ReturnRequests(ReturnRequestID) ON DELETE CASCADE,
    CONSTRAINT FK_ReturnItems_OrderItem FOREIGN KEY (OrderItemID) REFERENCES dbo.OrderItems(OrderItemID),
    CONSTRAINT CK_ReturnItems_Quantity CHECK (Quantity > 0),
    CONSTRAINT CK_ReturnItems_Resolution CHECK (Resolution IS NULL OR Resolution IN ('REFUND','EXCHANGE','REPAIR','REJECT'))
);
GO

CREATE TABLE dbo.VoucherUsages (
    VoucherUsageID      BIGINT IDENTITY(1,1) PRIMARY KEY,
    VoucherID           INT NOT NULL,
    UserID              INT NOT NULL,
    OrderID             BIGINT NOT NULL,
    DiscountAmount      DECIMAL(18,2) NOT NULL,
    UsedAt              DATETIME2 NOT NULL CONSTRAINT DF_VoucherUsages_UsedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_VoucherUsages_Order UNIQUE (OrderID),
    CONSTRAINT FK_VoucherUsages_Voucher FOREIGN KEY (VoucherID) REFERENCES dbo.Vouchers(VoucherID),
    CONSTRAINT FK_VoucherUsages_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_VoucherUsages_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID)
);
GO

/* ============================================================================
   8. ĐÁNH GIÁ VÀ PHẢN HỒI (REVIEWS)
   ============================================================================ */
CREATE TABLE dbo.Reviews (
    ReviewID            BIGINT IDENTITY(1,1) PRIMARY KEY,
    ProductID           INT NOT NULL,
    OrderItemID         BIGINT NOT NULL,
    UserID              INT NOT NULL,
    Rating              TINYINT NOT NULL,
    ReviewTitle         NVARCHAR(250) NULL,
    ReviewContent       NVARCHAR(2000) NULL,
    IsVerifiedPurchase  BIT NOT NULL CONSTRAINT DF_Reviews_Verified DEFAULT 1,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Reviews_Status DEFAULT 'PENDING',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Reviews_CreatedAt DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Reviews_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Reviews_OrderItem UNIQUE (OrderItemID),
    CONSTRAINT FK_Reviews_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID),
    CONSTRAINT FK_Reviews_OrderItem FOREIGN KEY (OrderItemID) REFERENCES dbo.OrderItems(OrderItemID),
    CONSTRAINT FK_Reviews_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Reviews_Rating CHECK (Rating BETWEEN 1 AND 5),
    CONSTRAINT CK_Reviews_Status CHECK (Status IN ('PENDING','APPROVED','REJECTED','HIDDEN'))
);
GO

CREATE TABLE dbo.ReviewMedia (
    ReviewMediaID       BIGINT IDENTITY(1,1) PRIMARY KEY,
    ReviewID            BIGINT NOT NULL,
    MediaType           VARCHAR(20) NOT NULL,
    MediaUrl            NVARCHAR(500) NOT NULL,
    DisplayOrder        INT NOT NULL CONSTRAINT DF_ReviewMedia_Order DEFAULT 0,
    CONSTRAINT FK_ReviewMedia_Review FOREIGN KEY (ReviewID) REFERENCES dbo.Reviews(ReviewID) ON DELETE CASCADE,
    CONSTRAINT CK_ReviewMedia_Type CHECK (MediaType IN ('IMAGE','VIDEO'))
);
GO

CREATE TABLE dbo.ReviewReplies (
    ReviewReplyID       BIGINT IDENTITY(1,1) PRIMARY KEY,
    ReviewID            BIGINT NOT NULL,
    StaffID             INT NOT NULL,
    ReplyContent        NVARCHAR(1500) NOT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_ReviewReplies_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_ReviewReplies_Review FOREIGN KEY (ReviewID) REFERENCES dbo.Reviews(ReviewID) ON DELETE CASCADE,
    CONSTRAINT FK_ReviewReplies_Staff FOREIGN KEY (StaffID) REFERENCES dbo.Users(UserID)
);
GO

/* ============================================================================
   9. KHU VỰC KHO VÀ TỒN KHO (WAREHOUSE & INVENTORY)
   ============================================================================ */
CREATE TABLE dbo.Warehouses (
    WarehouseID         INT IDENTITY(1,1) PRIMARY KEY,
    WarehouseCode       VARCHAR(40) NOT NULL,
    WarehouseName       NVARCHAR(150) NOT NULL,
    Address             NVARCHAR(500) NULL,
    ManagerID           INT NULL,
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Warehouses_Status DEFAULT 'ACTIVE',
    CONSTRAINT UQ_Warehouses_Code UNIQUE (WarehouseCode),
    CONSTRAINT FK_Warehouses_Manager FOREIGN KEY (ManagerID) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Warehouses_Status CHECK (Status IN ('ACTIVE','INACTIVE'))
);
GO

CREATE TABLE dbo.InventoryBalances (
    WarehouseID         INT NOT NULL,
    VariantID           INT NOT NULL,
    QuantityOnHand      INT NOT NULL CONSTRAINT DF_InventoryBalances_OnHand DEFAULT 0,
    QuantityReserved    INT NOT NULL CONSTRAINT DF_InventoryBalances_Reserved DEFAULT 0,
    ReorderLevel        INT NOT NULL CONSTRAINT DF_InventoryBalances_Reorder DEFAULT 5,
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_InventoryBalances_UpdatedAt DEFAULT SYSDATETIME(),
    AvailableQuantity   AS (QuantityOnHand - QuantityReserved) PERSISTED,
    CONSTRAINT PK_InventoryBalances PRIMARY KEY (WarehouseID, VariantID),
    CONSTRAINT FK_InventoryBalances_Warehouse FOREIGN KEY (WarehouseID) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_InventoryBalances_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT CK_InventoryBalances_Quantity CHECK (QuantityOnHand >= 0 AND QuantityReserved >= 0 AND QuantityReserved <= QuantityOnHand AND ReorderLevel >= 0)
);
GO

CREATE TABLE dbo.StockReceipts (
    StockReceiptID      BIGINT IDENTITY(1,1) PRIMARY KEY,
    ReceiptCode         VARCHAR(40) NOT NULL,
    WarehouseID         INT NOT NULL,
    SupplierName        NVARCHAR(200) NOT NULL,
    SupplierPhone       VARCHAR(20) NULL,
    ReceiptDate         DATETIME2 NOT NULL CONSTRAINT DF_StockReceipts_Date DEFAULT SYSDATETIME(),
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_StockReceipts_Status DEFAULT 'DRAFT',
    TotalCost           DECIMAL(18,2) NOT NULL CONSTRAINT DF_StockReceipts_Total DEFAULT 0,
    Note                NVARCHAR(1000) NULL,
    CreatedBy           INT NOT NULL,
    ApprovedBy          INT NULL,
    ApprovedAt          DATETIME2 NULL,
    CONSTRAINT UQ_StockReceipts_Code UNIQUE (ReceiptCode),
    CONSTRAINT FK_StockReceipts_Warehouse FOREIGN KEY (WarehouseID) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_StockReceipts_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_StockReceipts_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_StockReceipts_Status CHECK (Status IN ('DRAFT','PENDING','COMPLETED','CANCELLED'))
);
GO

CREATE TABLE dbo.StockReceiptItems (
    StockReceiptItemID  BIGINT IDENTITY(1,1) PRIMARY KEY,
    StockReceiptID      BIGINT NOT NULL,
    VariantID           INT NOT NULL,
    Quantity            INT NOT NULL,
    UnitCost            DECIMAL(18,2) NOT NULL,
    LineTotal           AS (Quantity * UnitCost) PERSISTED,
    CONSTRAINT UQ_StockReceiptItems UNIQUE (StockReceiptID, VariantID),
    CONSTRAINT FK_StockReceiptItems_Receipt FOREIGN KEY (StockReceiptID) REFERENCES dbo.StockReceipts(StockReceiptID) ON DELETE CASCADE,
    CONSTRAINT FK_StockReceiptItems_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT CK_StockReceiptItems_Values CHECK (Quantity > 0 AND UnitCost >= 0)
);
GO

CREATE TABLE dbo.StockExports (
    StockExportID       BIGINT IDENTITY(1,1) PRIMARY KEY,
    ExportCode          VARCHAR(40) NOT NULL,
    WarehouseID         INT NOT NULL,
    OrderID             BIGINT NULL,
    ExportType          VARCHAR(30) NOT NULL,
    ExportDate          DATETIME2 NOT NULL CONSTRAINT DF_StockExports_Date DEFAULT SYSDATETIME(),
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_StockExports_Status DEFAULT 'DRAFT',
    ReceiverName        NVARCHAR(200) NULL,
    Note                NVARCHAR(1000) NULL,
    CreatedBy           INT NOT NULL,
    ApprovedBy          INT NULL,
    ApprovedAt          DATETIME2 NULL,
    CONSTRAINT UQ_StockExports_Code UNIQUE (ExportCode),
    CONSTRAINT FK_StockExports_Warehouse FOREIGN KEY (WarehouseID) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_StockExports_Order FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID),
    CONSTRAINT FK_StockExports_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_StockExports_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_StockExports_Type CHECK (ExportType IN ('SALE','TRANSFER','DAMAGED','OTHER')),
    CONSTRAINT CK_StockExports_Status CHECK (Status IN ('DRAFT','PENDING','COMPLETED','CANCELLED'))
);
GO

CREATE TABLE dbo.StockExportItems (
    StockExportItemID   BIGINT IDENTITY(1,1) PRIMARY KEY,
    StockExportID       BIGINT NOT NULL,
    VariantID           INT NOT NULL,
    Quantity            INT NOT NULL,
    CONSTRAINT UQ_StockExportItems UNIQUE (StockExportID, VariantID),
    CONSTRAINT FK_StockExportItems_Export FOREIGN KEY (StockExportID) REFERENCES dbo.StockExports(StockExportID) ON DELETE CASCADE,
    CONSTRAINT FK_StockExportItems_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT CK_StockExportItems_Quantity CHECK (Quantity > 0)
);
GO

CREATE TABLE dbo.Stocktakes (
    StocktakeID         BIGINT IDENTITY(1,1) PRIMARY KEY,
    StocktakeCode       VARCHAR(40) NOT NULL,
    WarehouseID         INT NOT NULL,
    StocktakeDate       DATETIME2 NOT NULL CONSTRAINT DF_Stocktakes_Date DEFAULT SYSDATETIME(),
    Status              VARCHAR(20) NOT NULL CONSTRAINT DF_Stocktakes_Status DEFAULT 'DRAFT',
    Note                NVARCHAR(1000) NULL,
    CreatedBy           INT NOT NULL,
    ApprovedBy          INT NULL,
    ApprovedAt          DATETIME2 NULL,
    CONSTRAINT UQ_Stocktakes_Code UNIQUE (StocktakeCode),
    CONSTRAINT FK_Stocktakes_Warehouse FOREIGN KEY (WarehouseID) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_Stocktakes_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Stocktakes_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Stocktakes_Status CHECK (Status IN ('DRAFT','COUNTING','COMPLETED','CANCELLED'))
);
GO

CREATE TABLE dbo.StocktakeItems (
    StocktakeItemID     BIGINT IDENTITY(1,1) PRIMARY KEY,
    StocktakeID         BIGINT NOT NULL,
    VariantID           INT NOT NULL,
    SystemQuantity      INT NOT NULL,
    ActualQuantity      INT NOT NULL,
    DifferenceQuantity AS (ActualQuantity - SystemQuantity) PERSISTED,
    Note                NVARCHAR(500) NULL,
    CONSTRAINT UQ_StocktakeItems UNIQUE (StocktakeID, VariantID),
    CONSTRAINT FK_StocktakeItems_Stocktake FOREIGN KEY (StocktakeID) REFERENCES dbo.Stocktakes(StocktakeID) ON DELETE CASCADE,
    CONSTRAINT FK_StocktakeItems_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT CK_StocktakeItems_Quantity CHECK (SystemQuantity >= 0 AND ActualQuantity >= 0)
);
GO

CREATE TABLE dbo.InventoryTransactions (
    InventoryTransactionID BIGINT IDENTITY(1,1) PRIMARY KEY,
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
    CONSTRAINT FK_InventoryTransactions_Warehouse FOREIGN KEY (WarehouseID) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_InventoryTransactions_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT FK_InventoryTransactions_User FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_InventoryTransactions_Type CHECK (TransactionType IN ('RECEIPT','SALE','TRANSFER_IN','TRANSFER_OUT','RETURN_IN','DAMAGED_OUT','ADJUST_IN','ADJUST_OUT')),
    CONSTRAINT CK_InventoryTransactions_Change CHECK (QuantityChange <> 0 AND QuantityBefore >= 0 AND QuantityAfter >= 0)
);
GO

CREATE TABLE dbo.StockAlertRules (
    StockAlertRuleID    INT IDENTITY(1,1) PRIMARY KEY,
    WarehouseID         INT NULL,
    VariantID           INT NULL,
    AlertThreshold      INT NOT NULL,
    IsEnabled           BIT NOT NULL CONSTRAINT DF_StockAlertRules_Enabled DEFAULT 1,
    NotifyRoleCode      VARCHAR(30) NOT NULL CONSTRAINT DF_StockAlertRules_Role DEFAULT 'WAREHOUSE',
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_StockAlertRules_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_StockAlertRules_Warehouse FOREIGN KEY (WarehouseID) REFERENCES dbo.Warehouses(WarehouseID),
    CONSTRAINT FK_StockAlertRules_Variant FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT CK_StockAlertRules_Threshold CHECK (AlertThreshold >= 0)
);
GO

/* ============================================================================
   10. THÔNG BÁO, CẤU HÌNH HỆ THỐNG VÀ AUDIT
   ============================================================================ */
CREATE TABLE dbo.Notifications (
    NotificationID      BIGINT IDENTITY(1,1) PRIMARY KEY,
    NotificationType    VARCHAR(40) NOT NULL,
    Title               NVARCHAR(250) NOT NULL,
    Message             NVARCHAR(1500) NOT NULL,
    TargetUrl           NVARCHAR(500) NULL,
    CreatedBy           INT NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Notifications_CreatedAt DEFAULT SYSDATETIME(),
    ExpiresAt           DATETIME2 NULL,
    CONSTRAINT FK_Notifications_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES dbo.Users(UserID)
);
GO

CREATE TABLE dbo.UserNotifications (
    NotificationID      BIGINT NOT NULL,
    UserID              INT NOT NULL,
    IsRead              BIT NOT NULL CONSTRAINT DF_UserNotifications_Read DEFAULT 0,
    ReadAt              DATETIME2 NULL,
    CONSTRAINT PK_UserNotifications PRIMARY KEY (NotificationID, UserID),
    CONSTRAINT FK_UserNotifications_Notification FOREIGN KEY (NotificationID) REFERENCES dbo.Notifications(NotificationID) ON DELETE CASCADE,
    CONSTRAINT FK_UserNotifications_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.SystemSettings (
    SettingKey          VARCHAR(100) PRIMARY KEY,
    SettingValue        NVARCHAR(MAX) NULL,
    SettingGroup        VARCHAR(50) NOT NULL,
    Description         NVARCHAR(500) NULL,
    IsPublic            BIT NOT NULL CONSTRAINT DF_SystemSettings_Public DEFAULT 0,
    UpdatedBy           INT NULL,
    UpdatedAt           DATETIME2 NOT NULL CONSTRAINT DF_SystemSettings_UpdatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_SystemSettings_User FOREIGN KEY (UpdatedBy) REFERENCES dbo.Users(UserID)
);
GO

CREATE TABLE dbo.AuditLogs (
    AuditLogID          BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID              INT NULL,
    ActionCode          VARCHAR(60) NOT NULL,
    EntityName          VARCHAR(100) NOT NULL,
    EntityID            VARCHAR(100) NULL,
    OldData             NVARCHAR(MAX) NULL,
    NewData             NVARCHAR(MAX) NULL,
    IpAddress           VARCHAR(50) NULL,
    UserAgent           NVARCHAR(500) NULL,
    CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_AuditLogs_CreatedAt DEFAULT SYSDATETIME(),
    CONSTRAINT FK_AuditLogs_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID)
);
GO

/* ============================================================================
   11. CHỈ MỤC TỐI ƯU HIỆU NĂNG (INDEXES)
   ============================================================================ */
CREATE INDEX IX_Users_Status ON dbo.Users(Status);
CREATE INDEX IX_Products_BrandCategoryStatus ON dbo.Products(BrandID, CategoryID, Status);
CREATE INDEX IX_Products_Featured ON dbo.Products(IsFeatured, Status);
CREATE INDEX IX_ProductVariants_Product ON dbo.ProductVariants(ProductID, Status);
CREATE INDEX IX_Orders_CustomerCreated ON dbo.Orders(CustomerID, CreatedAt DESC);
CREATE INDEX IX_Orders_StatusCreated ON dbo.Orders(OrderStatus, CreatedAt DESC);
CREATE INDEX IX_OrderItems_Order ON dbo.OrderItems(OrderID);
CREATE INDEX IX_OrderItems_Variant ON dbo.OrderItems(VariantID);
CREATE INDEX IX_Payments_Order ON dbo.Payments(OrderID, CreatedAt DESC);
CREATE INDEX IX_Reviews_ProductStatus ON dbo.Reviews(ProductID, Status, CreatedAt DESC);
CREATE INDEX IX_InventoryBalances_LowStock ON dbo.InventoryBalances(WarehouseID, AvailableQuantity, ReorderLevel);
CREATE INDEX IX_InventoryTransactions_VariantDate ON dbo.InventoryTransactions(VariantID, CreatedAt DESC);
CREATE INDEX IX_Posts_StatusPublished ON dbo.Posts(Status, PublishedAt DESC);
CREATE INDEX IX_UserNotifications_UserRead ON dbo.UserNotifications(UserID, IsRead);
GO

/* ============================================================================
   12. NGHIỆP VỤ VIEWS TRUY VẤN
   ============================================================================ */
CREATE OR ALTER VIEW dbo.vw_ProductCatalog AS
SELECT 
    p.ProductID,
    p.ProductCode,
    p.ProductName,
    p.Slug AS ProductSlug,
    b.BrandID,
    b.BrandName,
    c.CategoryID,
    c.CategoryName,
    p.MovementType,
    p.Gender,
    p.WarrantyMonths,
    p.Status AS ProductStatus,
    p.IsFeatured,
    p.RatingAverage,
    p.RatingCount,
    MIN(pv.SalePrice) AS MinSalePrice,
    MAX(pv.SalePrice) AS MaxSalePrice,
    SUM(ISNULL(ib.AvailableQuantity, 0)) AS TotalAvailableQuantity,
    (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS PrimaryImageUrl
FROM dbo.Products p
INNER JOIN dbo.Brands b ON p.BrandID = b.BrandID
INNER JOIN dbo.Categories c ON p.CategoryID = c.CategoryID
LEFT JOIN dbo.ProductVariants pv ON p.ProductID = pv.ProductID AND pv.Status = 'ACTIVE'
LEFT JOIN dbo.InventoryBalances ib ON pv.VariantID = ib.VariantID
GROUP BY 
    p.ProductID, p.ProductCode, p.ProductName, p.Slug, 
    b.BrandID, b.BrandName, c.CategoryID, c.CategoryName, 
    p.MovementType, p.Gender, p.WarrantyMonths, p.Status, 
    p.IsFeatured, p.RatingAverage, p.RatingCount;
GO

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

CREATE OR ALTER VIEW dbo.vw_OrderManagement AS
SELECT 
    o.OrderID,
    o.OrderCode,
    o.CustomerID,
    u.FullName AS CustomerName,
    u.Email AS CustomerEmail,
    u.Phone AS CustomerPhone,
    o.RecipientName,
    o.RecipientPhone,
    o.ShippingAddress,
    o.OrderStatus,
    o.PaymentStatus,
    o.SubtotalAmount,
    o.DiscountAmount,
    o.ShippingFee,
    o.TotalAmount,
    o.CreatedAt,
    (SELECT COUNT(*) FROM dbo.OrderItems WHERE OrderID = o.OrderID) AS TotalItems
FROM dbo.Orders o
INNER JOIN dbo.Users u ON o.CustomerID = u.UserID;
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
   13. HÀM VÀ STORED PROCEDURES NGHIỆP VỤ
   ============================================================================ */

-- Hàm tính giá trị giảm giá Voucher
CREATE OR ALTER FUNCTION dbo.fn_CalculateVoucherDiscount(
    @VoucherID INT,
    @SubtotalAmount DECIMAL(18,2)
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @DiscountAmount DECIMAL(18,2) = 0;
    DECLARE @DiscountType VARCHAR(20), @DiscountValue DECIMAL(18,2), @MaxDiscount DECIMAL(18,2), @MinOrder DECIMAL(18,2);

    SELECT 
        @DiscountType = DiscountType,
        @DiscountValue = DiscountValue,
        @MaxDiscount = MaximumDiscount,
        @MinOrder = MinimumOrderValue
    FROM dbo.Vouchers
    WHERE VoucherID = @VoucherID AND Status = 'ACTIVE' AND SYSDATETIME() BETWEEN StartAt AND EndAt;

    IF @SubtotalAmount >= ISNULL(@MinOrder, 0)
    BEGIN
        IF @DiscountType = 'FIXED' SET @DiscountAmount = @DiscountValue;
        ELSE IF @DiscountType = 'PERCENT'
        BEGIN
            SET @DiscountAmount = (@SubtotalAmount * @DiscountValue) / 100.0;
            IF @MaxDiscount IS NOT NULL AND @DiscountAmount > @MaxDiscount
                SET @DiscountAmount = @MaxDiscount;
        END
    END

    RETURN ISNULL(@DiscountAmount, 0);
END;
GO

-- Stored Procedure Thêm sản phẩm vào giỏ hàng
CREATE OR ALTER PROCEDURE dbo.sp_AddToCart
    @UserID INT = NULL,
    @GuestToken VARCHAR(100) = NULL,
    @VariantID INT,
    @Quantity INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        IF @UserID IS NULL AND @GuestToken IS NULL
        BEGIN
            RAISERROR(N'Cần có UserID hoặc GuestToken để xác định giỏ hàng', 16, 1);
        END

        DECLARE @CartID BIGINT;

        IF @UserID IS NOT NULL
        BEGIN
            SELECT TOP 1 @CartID = CartID FROM dbo.Carts WHERE UserID = @UserID AND Status = 'ACTIVE';
            IF @CartID IS NULL
            BEGIN
                INSERT INTO dbo.Carts (UserID, Status) VALUES (@UserID, 'ACTIVE');
                SET @CartID = SCOPE_IDENTITY();
            END
        END
        ELSE
        BEGIN
            SELECT TOP 1 @CartID = CartID FROM dbo.Carts WHERE GuestToken = @GuestToken AND Status = 'ACTIVE';
            IF @CartID IS NULL
            BEGIN
                INSERT INTO dbo.Carts (GuestToken, Status) VALUES (@GuestToken, 'ACTIVE');
                SET @CartID = SCOPE_IDENTITY();
            END
        END

        IF EXISTS (SELECT 1 FROM dbo.CartItems WHERE CartID = @CartID AND VariantID = @VariantID)
        BEGIN
            UPDATE dbo.CartItems 
            SET Quantity = Quantity + @Quantity, UpdatedAt = SYSDATETIME()
            WHERE CartID = @CartID AND VariantID = @VariantID;
        END
        ELSE
        BEGIN
            INSERT INTO dbo.CartItems (CartID, VariantID, Quantity)
            VALUES (@CartID, @VariantID, @Quantity);
        END

        UPDATE dbo.Carts SET UpdatedAt = SYSDATETIME() WHERE CartID = @CartID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- Stored Procedure Tạo Đơn hàng từ Giỏ hàng
CREATE OR ALTER PROCEDURE dbo.sp_CreateOrderFromCart
    @CartID BIGINT,
    @RecipientName NVARCHAR(150),
    @RecipientPhone VARCHAR(20),
    @ShippingAddress NVARCHAR(500),
    @CustomerNote NVARCHAR(1000) = NULL,
    @VoucherID INT = NULL,
    @ShippingFee DECIMAL(18,2) = 0,
    @NewOrderID BIGINT OUTPUT,
    @NewOrderCode VARCHAR(30) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @CustomerID INT;
        SELECT @CustomerID = UserID FROM dbo.Carts WHERE CartID = @CartID AND Status = 'ACTIVE';

        IF @CustomerID IS NULL
        BEGIN
            RAISERROR(N'Giỏ hàng không hợp lệ hoặc đã được checkout.', 16, 1);
        END

        DECLARE @Subtotal DECIMAL(18,2) = 0;
        SELECT @Subtotal = SUM(pv.SalePrice * ci.Quantity)
        FROM dbo.CartItems ci
        INNER JOIN dbo.ProductVariants pv ON ci.VariantID = pv.VariantID
        WHERE ci.CartID = @CartID;

        IF ISNULL(@Subtotal, 0) <= 0
        BEGIN
            RAISERROR(N'Giỏ hàng rỗng.', 16, 1);
        END

        DECLARE @DiscountAmount DECIMAL(18,2) = 0;
        IF @VoucherID IS NOT NULL
        BEGIN
            SET @DiscountAmount = dbo.fn_CalculateVoucherDiscount(@VoucherID, @Subtotal);
        END

        DECLARE @TotalAmount DECIMAL(18,2) = @Subtotal - @DiscountAmount + @ShippingFee;
        IF @TotalAmount < 0 SET @TotalAmount = 0;

        DECLARE @SeqVal BIGINT = NEXT VALUE FOR dbo.OrderNumberSequence;
        SET @NewOrderCode = 'WS' + CAST(@SeqVal AS VARCHAR(20));

        INSERT INTO dbo.Orders (
            OrderCode, CustomerID, VoucherID, RecipientName, RecipientPhone,
            ShippingAddress, CustomerNote, OrderStatus, PaymentStatus,
            SubtotalAmount, DiscountAmount, ShippingFee, TotalAmount
        )
        VALUES (
            @NewOrderCode, @CustomerID, @VoucherID, @RecipientName, @RecipientPhone,
            @ShippingAddress, @CustomerNote, 'PENDING', 'UNPAID',
            @Subtotal, @DiscountAmount, @ShippingFee, @TotalAmount
        );

        SET @NewOrderID = SCOPE_IDENTITY();

        INSERT INTO dbo.OrderItems (OrderID, VariantID, ProductName, VariantName, SKU, ImageUrl, UnitPrice, Quantity, DiscountAmount)
        SELECT 
            @NewOrderID,
            ci.VariantID,
            p.ProductName,
            pv.VariantName,
            pv.SKU,
            (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC),
            pv.SalePrice,
            ci.Quantity,
            0
        FROM dbo.CartItems ci
        INNER JOIN dbo.ProductVariants pv ON ci.VariantID = pv.VariantID
        INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID
        WHERE ci.CartID = @CartID;

        IF @VoucherID IS NOT NULL
        BEGIN
            INSERT INTO dbo.VoucherUsages (VoucherID, UserID, OrderID, DiscountAmount)
            VALUES (@VoucherID, @CustomerID, @NewOrderID, @DiscountAmount);

            UPDATE dbo.Vouchers SET UsedCount = UsedCount + 1 WHERE VoucherID = @VoucherID;
        END

        INSERT INTO dbo.OrderStatusHistory (OrderID, OldStatus, NewStatus, Note)
        VALUES (@NewOrderID, NULL, 'PENDING', N'Đơn hàng được tạo thành công từ giỏ hàng.');

        UPDATE dbo.Carts SET Status = 'CHECKED_OUT', UpdatedAt = SYSDATETIME() WHERE CartID = @CartID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- Stored Procedure Cập nhật trạng thái đơn hàng
CREATE OR ALTER PROCEDURE dbo.sp_UpdateOrderStatus
    @OrderID BIGINT,
    @NewStatus VARCHAR(30),
    @ChangedBy INT,
    @Note NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @OldStatus VARCHAR(30);
        SELECT @OldStatus = OrderStatus FROM dbo.Orders WHERE OrderID = @OrderID;

        IF @OldStatus IS NULL
        BEGIN
            RAISERROR(N'Đơn hàng không tồn tại.', 16, 1);
        END

        UPDATE dbo.Orders 
        SET OrderStatus = @NewStatus, 
            UpdatedAt = SYSDATETIME(),
            ConfirmedAt = CASE WHEN @NewStatus = 'CONFIRMED' THEN SYSDATETIME() ELSE ConfirmedAt END,
            CompletedAt = CASE WHEN @NewStatus = 'COMPLETED' THEN SYSDATETIME() ELSE CompletedAt END,
            CancelledAt = CASE WHEN @NewStatus = 'CANCELLED' THEN SYSDATETIME() ELSE CancelledAt END
        WHERE OrderID = @OrderID;

        INSERT INTO dbo.OrderStatusHistory (OrderID, OldStatus, NewStatus, ChangedBy, Note)
        VALUES (@OrderID, @OldStatus, @NewStatus, @ChangedBy, @Note);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- Stored Procedure Ghi nhận Giao dịch tồn kho (Nhập kho, Xuất bán, Điều chỉnh)
CREATE OR ALTER PROCEDURE dbo.sp_RecordInventoryTransaction
    @WarehouseID INT,
    @VariantID INT,
    @TransactionType VARCHAR(30),
    @QuantityChange INT,
    @ReferenceType VARCHAR(30) = NULL,
    @ReferenceID BIGINT = NULL,
    @Note NVARCHAR(500) = NULL,
    @CreatedBy INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @QtyBefore INT = 0;
        SELECT @QtyBefore = QuantityOnHand FROM dbo.InventoryBalances WITH (UPDLOCK) 
        WHERE WarehouseID = @WarehouseID AND VariantID = @VariantID;

        IF @QtyBefore IS NULL AND NOT EXISTS(SELECT 1 FROM dbo.InventoryBalances WHERE WarehouseID = @WarehouseID AND VariantID = @VariantID)
        BEGIN
            INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel)
            VALUES (@WarehouseID, @VariantID, 0, 0, 5);
            SET @QtyBefore = 0;
        END

        DECLARE @QtyAfter INT = @QtyBefore + @QuantityChange;
        IF @QtyAfter < 0
        BEGIN
            RAISERROR(N'Số lượng tồn kho sau giao dịch không thể âm.', 16, 1);
        END

        UPDATE dbo.InventoryBalances 
        SET QuantityOnHand = @QtyAfter, UpdatedAt = SYSDATETIME()
        WHERE WarehouseID = @WarehouseID AND VariantID = @VariantID;

        INSERT INTO dbo.InventoryTransactions (
            WarehouseID, VariantID, TransactionType, QuantityChange, QuantityBefore, QuantityAfter, ReferenceType, ReferenceID, Note, CreatedBy
        )
        VALUES (
            @WarehouseID, @VariantID, @TransactionType, @QuantityChange, @QtyBefore, @QtyAfter, @ReferenceType, @ReferenceID, @Note, @CreatedBy
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

/* ============================================================================
   14. DỮ LIỆU MẪU ĐẦY ĐỦ (DEMO SEEDED DATA) - CHỐNG TRÙNG LẶP KHÓA
   ============================================================================ */
DECLARE @DemoPasswordHash VARCHAR(64) = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '123456'), 2);

-- 14.1. USERS
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = 'admin@watchstore.vn')
BEGIN
    INSERT INTO dbo.Users (Email, PasswordHash, FullName, Phone, Gender, DateOfBirth, Status, EmailVerifiedAt, LastLoginAt)
    VALUES
    ('admin@watchstore.vn',     @DemoPasswordHash, N'Thạch Như Thuận',       '0988000001', 'MALE',   '2007-01-15', 'ACTIVE', SYSDATETIME(), SYSDATETIME()),
    ('sales@watchstore.vn',     @DemoPasswordHash, N'Nguyễn Minh Sales',     '0988000002', 'MALE',   '1999-06-10', 'ACTIVE', SYSDATETIME(), DATEADD(HOUR,-1,SYSDATETIME())),
    ('warehouse@watchstore.vn', @DemoPasswordHash, N'Trần Hoàng Kho',        '0988000003', 'MALE',   '1998-09-20', 'ACTIVE', SYSDATETIME(), DATEADD(HOUR,-2,SYSDATETIME())),
    ('customer@watchstore.vn',  @DemoPasswordHash, N'Khách hàng WatchStore', '0988000004', 'OTHER',  '2002-03-12', 'ACTIVE', SYSDATETIME(), DATEADD(DAY,-1,SYSDATETIME())),
    ('an.nguyen@example.com',   @DemoPasswordHash, N'Nguyễn Văn An',         '0988000005', 'MALE',   '1995-04-18', 'ACTIVE', SYSDATETIME(), DATEADD(DAY,-2,SYSDATETIME())),
    ('duc.tran@example.com',    @DemoPasswordHash, N'Trần Minh Đức',         '0988000006', 'MALE',   '1993-11-05', 'ACTIVE', SYSDATETIME(), DATEADD(DAY,-3,SYSDATETIME())),
    ('cong.le@example.com',     @DemoPasswordHash, N'Lê Thành Công',         '0988000007', 'MALE',   '1997-08-22', 'ACTIVE', SYSDATETIME(), DATEADD(DAY,-4,SYSDATETIME()));
END

-- 14.2. ROLES
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = 'ADMIN')
BEGIN
    INSERT INTO dbo.Roles (RoleCode, RoleName, Description, IsSystem)
    VALUES
    ('ADMIN', N'Quản trị viên', N'Toàn quyền cấu hình và quản trị hệ thống', 1),
    ('SALES', N'Nhân viên bán hàng', N'Quản lý khách hàng, đơn hàng, giao hàng và đổi trả', 1),
    ('WAREHOUSE', N'Nhân viên kho', N'Quản lý nhập, xuất, tồn và kiểm kê', 1),
    ('CUSTOMER', N'Khách hàng', N'Mua hàng và quản lý tài khoản cá nhân', 1);
END

-- 14.3. USER ROLES (Khớp linh hoạt theo Email và RoleCode, kiểm tra NOT EXISTS)
INSERT INTO dbo.UserRoles (UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM dbo.Users u
INNER JOIN dbo.Roles r ON (
    (u.Email = 'admin@watchstore.vn' AND r.RoleCode = 'ADMIN')
    OR (u.Email = 'sales@watchstore.vn' AND r.RoleCode = 'SALES')
    OR (u.Email = 'warehouse@watchstore.vn' AND r.RoleCode = 'WAREHOUSE')
    OR (u.Email IN ('customer@watchstore.vn', 'an.nguyen@example.com', 'duc.tran@example.com', 'cong.le@example.com') AND r.RoleCode = 'CUSTOMER')
)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID
);

-- 14.4. BRANDS
IF NOT EXISTS (SELECT 1 FROM dbo.Brands WHERE BrandCode = 'SEIKO')
BEGIN
    INSERT INTO dbo.Brands (BrandCode, BrandName, Slug, OriginCountry, LogoUrl, Description)
    VALUES
    ('SEIKO', N'Seiko', 'seiko', N'Nhật Bản', '/assets/images/brands/seiko.png', N'Thương hiệu đồng hồ nổi tiếng Nhật Bản với lịch sử hơn 100 năm.'),
    ('ORIENT', N'Orient', 'orient', N'Nhật Bản', '/assets/images/brands/orient.png', N'Nổi tiếng với các dòng đồng hồ cơ Automatic chất lượng cao.'),
    ('CASIO', N'Casio', 'casio', N'Nhật Bản', '/assets/images/brands/casio.png', N'Đồng hồ thể thao, G-Shock bền bỉ hàng đầu.'),
    ('TISSOT', N'Tissot', 'tissot', N'Thụy Sỹ', '/assets/images/brands/tissot.png', N'Đồng hồ Thụy Sỹ đẳng cấp với bộ máy Powermatic 80 ấn tượng.'),
    ('CITIZEN', N'Citizen', 'citizen', N'Nhật Bản', '/assets/images/brands/citizen.png', N'Tiên phong với công nghệ năng lượng ánh sáng Eco-Drive.'),
    ('LONGINES', N'Longines', 'longines', N'Thụy Sỹ', '/assets/images/brands/longines.png', N'Thương hiệu đồng hồ cao cấp sang trọng của Thụy Sỹ.');
END

-- 14.5. CATEGORIES
IF NOT EXISTS (SELECT 1 FROM dbo.Categories WHERE CategoryCode = 'MEN')
BEGIN
    INSERT INTO dbo.Categories (CategoryCode, CategoryName, Slug, Description, DisplayOrder)
    VALUES
    ('MEN', N'Đồng Hồ Nam', 'dong-ho-nam', N'Bộ sưu tập đồng hồ nam cao cấp, lịch lãm', 1),
    ('WOMEN', N'Đồng Hồ Nữ', 'dong-ho-nu', N'Đồng hồ nữ thời trang, quý phái', 2),
    ('COUPLE', N'Đồng Hồ Đôi', 'dong-ho-doi', N'Bộ sưu tập đồng hồ cặp đôi ý nghĩa', 3),
    ('AUTOMATIC', N'Đồng Hồ Cơ (Automatic)', 'dong-ho-co-automatic', N'Đồng hồ cơ tự động tinh xảo', 4),
    ('QUARTZ', N'Đồng Hồ Pin (Quartz)', 'dong-ho-pin-quartz', N'Đồng hồ máy Quartz chính xác cao', 5);
END

-- 14.6. PRODUCTS
IF NOT EXISTS (SELECT 1 FROM dbo.Products WHERE ProductCode = 'SRPD37J1')
BEGIN
    INSERT INTO dbo.Products (ProductCode, ProductName, Slug, BrandID, CategoryID, MovementType, Gender, ShortDescription, Description, CaseMaterial, GlassMaterial, StrapMaterial, WaterResistance, OriginCountry, WarrantyMonths, Status, IsFeatured, RatingAverage, RatingCount, CreatedBy)
    SELECT 'SRPD37J1', N'Seiko Presage Cocktail Time SRPD37J1', 'seiko-presage-cocktail-time-srpd37j1', b.BrandID, c.CategoryID, 'AUTOMATIC', 'MEN', N'Mặt số xanh ngọc lục bảo quyến rũ, bộ máy 4R35 tự động', N'Seiko Presage SRPD37J1 sở hữu thiết kế lấy cảm hứng từ những ly Cocktail thanh lịch tại quầy bar Tokyo.', N'Thép không gỉ 316L', N'Kính Hardlex cong', N'Dây da cao cấp', N'50m (5 ATM)', N'Nhật Bản', 24, 'ACTIVE', 1, 4.9, 15, u.UserID
    FROM dbo.Brands b, dbo.Categories c, dbo.Users u 
    WHERE b.BrandCode = 'SEIKO' AND c.CategoryCode = 'MEN' AND u.Email = 'admin@watchstore.vn';

    INSERT INTO dbo.Products (ProductCode, ProductName, Slug, BrandID, CategoryID, MovementType, Gender, ShortDescription, Description, CaseMaterial, GlassMaterial, StrapMaterial, WaterResistance, OriginCountry, WarrantyMonths, Status, IsFeatured, RatingAverage, RatingCount, CreatedBy)
    SELECT 'FAC00005W0', N'Orient Bambino Gen 2 FAC00005W0', 'orient-bambino-gen-2-fac00005w0', b.BrandID, c.CategoryID, 'AUTOMATIC', 'MEN', N'Kính vòm cổ điển, kim xanh nổi bật trên nền mặt trắng', N'Orient Bambino Gen 2 Ver 2 FAC00005W0 là mẫu đồng hồ dress watch cơ huyền thoại trong tầm giá.', N'Thép không gỉ 316L', N'Kính khoáng vòm (Mineral Crystal)', N'Dây da nâu', N'30m (3 ATM)', N'Nhật Bản', 24, 'ACTIVE', 1, 4.8, 22, u.UserID
    FROM dbo.Brands b, dbo.Categories c, dbo.Users u 
    WHERE b.BrandCode = 'ORIENT' AND c.CategoryCode = 'MEN' AND u.Email = 'admin@watchstore.vn';

    INSERT INTO dbo.Products (ProductCode, ProductName, Slug, BrandID, CategoryID, MovementType, Gender, ShortDescription, Description, CaseMaterial, GlassMaterial, StrapMaterial, WaterResistance, OriginCountry, WarrantyMonths, Status, IsFeatured, RatingAverage, RatingCount, CreatedBy)
    SELECT 'GA-2100-1A1', N'Casio G-Shock GA-2100-1A1DR (CasiOak)', 'casio-g-shock-ga-2100-1a1dr', b.BrandID, c.CategoryID, 'QUARTZ', 'MEN', N'Thiết kế bát giác siêu mỏng, vỏ Carbon Core Guard', N'Dòng G-Shock GA-2100 đen nguyên khối thiết kế thể thao góc cạnh cá tính.', N'Nhựa gia cường Carbon', N'Kính khoáng (Mineral Glass)', N'Dây nhựa cao cấp', N'200m (20 ATM)', N'Nhật Bản', 12, 'ACTIVE', 1, 5.0, 30, u.UserID
    FROM dbo.Brands b, dbo.Categories c, dbo.Users u 
    WHERE b.BrandCode = 'CASIO' AND c.CategoryCode = 'MEN' AND u.Email = 'admin@watchstore.vn';

    INSERT INTO dbo.Products (ProductCode, ProductName, Slug, BrandID, CategoryID, MovementType, Gender, ShortDescription, Description, CaseMaterial, GlassMaterial, StrapMaterial, WaterResistance, OriginCountry, WarrantyMonths, Status, IsFeatured, RatingAverage, RatingCount, CreatedBy)
    SELECT 'T063.907.11.038.00', N'Tissot Tradition Open Heart T0639071103800', 'tissot-tradition-open-heart-t0639071103800', b.BrandID, c.CategoryID, 'AUTOMATIC', 'MEN', N'Lộ cơ góc 12h tinh tế, bộ máy Powermatic 80 trữ cót 80 giờ', N'Tissot Open Heart Thụy Sỹ mang nét đẹp cổ điển hòa quyện cùng phong cách hiện đại.', N'Thép không gỉ 316L', N'Kính Sapphire chống xước', N'Dây thép không gỉ', N'30m (3 ATM)', N'Thụy Sỹ', 36, 'ACTIVE', 1, 4.95, 18, u.UserID
    FROM dbo.Brands b, dbo.Categories c, dbo.Users u 
    WHERE b.BrandCode = 'TISSOT' AND c.CategoryCode = 'MEN' AND u.Email = 'admin@watchstore.vn';
END

-- 14.7. PRODUCT VARIANTS
IF NOT EXISTS (SELECT 1 FROM dbo.ProductVariants WHERE SKU = 'SRPD37J1-STD')
BEGIN
    INSERT INTO dbo.ProductVariants (ProductID, SKU, Barcode, VariantName, CostPrice, SalePrice, CompareAtPrice, WeightGram, Status)
    SELECT p.ProductID, 'SRPD37J1-STD', '4954628230554', N'Mặt Xanh - Dây Da Nâu', 8500000, 11500000, 13000000, 150, 'ACTIVE'
    FROM dbo.Products p WHERE p.ProductCode = 'SRPD37J1';

    INSERT INTO dbo.ProductVariants (ProductID, SKU, Barcode, VariantName, CostPrice, SalePrice, CompareAtPrice, WeightGram, Status)
    SELECT p.ProductID, 'FAC00005W0-STD', '4942715010041', N'Mặt Trắng - Dây Da Nâu Kim Xanh', 4200000, 5800000, 6500000, 140, 'ACTIVE'
    FROM dbo.Products p WHERE p.ProductCode = 'FAC00005W0';

    INSERT INTO dbo.ProductVariants (ProductID, SKU, Barcode, VariantName, CostPrice, SalePrice, CompareAtPrice, WeightGram, Status)
    SELECT p.ProductID, 'GA-2100-1A1-STD', '4549526241698', N'Màu Đen Nguyên Khối (All Black)', 2500000, 3800000, 4200000, 80, 'ACTIVE'
    FROM dbo.Products p WHERE p.ProductCode = 'GA-2100-1A1';

    INSERT INTO dbo.ProductVariants (ProductID, SKU, Barcode, VariantName, CostPrice, SalePrice, CompareAtPrice, WeightGram, Status)
    SELECT p.ProductID, 'T0639071103800-STD', '7612345678901', N'Mặt Trắng Lộ Cơ - Dây Kim Loại', 14500000, 20500000, 23000000, 180, 'ACTIVE'
    FROM dbo.Products p WHERE p.ProductCode = 'T063.907.11.038.00';
END

-- 14.8. WAREHOUSES
IF NOT EXISTS (SELECT 1 FROM dbo.Warehouses WHERE WarehouseCode = 'WH-HN-01')
BEGIN
    INSERT INTO dbo.Warehouses (WarehouseCode, WarehouseName, Address, ManagerID, Status)
    SELECT 'WH-HN-01', N'Kho Tổng Hà Nội', N'Số 154 Cầu Giấy, Q. Cầu Giấy, Hà Nội', u.UserID, 'ACTIVE'
    FROM dbo.Users u WHERE u.Email = 'warehouse@watchstore.vn';

    INSERT INTO dbo.Warehouses (WarehouseCode, WarehouseName, Address, ManagerID, Status)
    SELECT 'WH-HCM-01', N'Kho Chi Nhánh TP.HCM', N'Số 285 Cách Mạng Tháng 8, Q.10, TP.HCM', u.UserID, 'ACTIVE'
    FROM dbo.Users u WHERE u.Email = 'warehouse@watchstore.vn';
END

-- 14.9. INVENTORY BALANCES
IF NOT EXISTS (SELECT 1 FROM dbo.InventoryBalances)
BEGIN
    INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel)
    SELECT w.WarehouseID, v.VariantID, 25, 2, 5
    FROM dbo.Warehouses w CROSS JOIN dbo.ProductVariants v
    WHERE w.WarehouseCode = 'WH-HN-01' AND v.SKU = 'SRPD37J1-STD';

    INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel)
    SELECT w.WarehouseID, v.VariantID, 40, 0, 5
    FROM dbo.Warehouses w CROSS JOIN dbo.ProductVariants v
    WHERE w.WarehouseCode = 'WH-HN-01' AND v.SKU = 'FAC00005W0-STD';

    INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel)
    SELECT w.WarehouseID, v.VariantID, 50, 5, 10
    FROM dbo.Warehouses w CROSS JOIN dbo.ProductVariants v
    WHERE w.WarehouseCode = 'WH-HN-01' AND v.SKU = 'GA-2100-1A1-STD';

    INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel)
    SELECT w.WarehouseID, v.VariantID, 15, 1, 3
    FROM dbo.Warehouses w CROSS JOIN dbo.ProductVariants v
    WHERE w.WarehouseCode = 'WH-HN-01' AND v.SKU = 'T0639071103800-STD';

    INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel)
    SELECT w.WarehouseID, v.VariantID, 10, 0, 3
    FROM dbo.Warehouses w CROSS JOIN dbo.ProductVariants v
    WHERE w.WarehouseCode = 'WH-HCM-01' AND v.SKU = 'SRPD37J1-STD';

    INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel)
    SELECT w.WarehouseID, v.VariantID, 15, 1, 3
    FROM dbo.Warehouses w CROSS JOIN dbo.ProductVariants v
    WHERE w.WarehouseCode = 'WH-HCM-01' AND v.SKU = 'FAC00005W0-STD';

    INSERT INTO dbo.InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel)
    SELECT w.WarehouseID, v.VariantID, 30, 2, 5
    FROM dbo.Warehouses w CROSS JOIN dbo.ProductVariants v
    WHERE w.WarehouseCode = 'WH-HCM-01' AND v.SKU = 'GA-2100-1A1-STD';
END

-- 14.10. VOUCHERS
IF NOT EXISTS (SELECT 1 FROM dbo.Vouchers WHERE VoucherCode = 'WELCOME100')
BEGIN
    INSERT INTO dbo.Vouchers (VoucherCode, VoucherName, Description, DiscountType, DiscountValue, MaximumDiscount, MinimumOrderValue, UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, IsPublic, Status, CreatedBy)
    SELECT 'WELCOME100', N'Giảm 100K Cho Đơn Hàng Đầu Tiên', N'Ưu đãi dành riêng cho khách hàng mới', 'FIXED', 100000, 100000, 2000000, 100, 1, 5, DATEADD(DAY, -10, SYSDATETIME()), DATEADD(DAY, 90, SYSDATETIME()), 1, 'ACTIVE', u.UserID
    FROM dbo.Users u WHERE u.Email = 'admin@watchstore.vn';

    INSERT INTO dbo.Vouchers (VoucherCode, VoucherName, Description, DiscountType, DiscountValue, MaximumDiscount, MinimumOrderValue, UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, IsPublic, Status, CreatedBy)
    SELECT 'SUMMER2026', N'Giảm 10% Tối Đa 1 Triệu', N'Chương trình khuyến mãi mùa hè 2026', 'PERCENT', 10, 1000000, 5000000, 50, 1, 12, DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, 30, SYSDATETIME()), 1, 'ACTIVE', u.UserID
    FROM dbo.Users u WHERE u.Email = 'admin@watchstore.vn';

    INSERT INTO dbo.Vouchers (VoucherCode, VoucherName, Description, DiscountType, DiscountValue, MaximumDiscount, MinimumOrderValue, UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, IsPublic, Status, CreatedBy)
    SELECT 'FREESHIP50', N'Miễn Phí Vận Chuyển 50K', N'Áp dụng cho mọi đơn hàng từ 1 triệu', 'FREESHIP', 50000, 50000, 1000000, 200, 2, 25, DATEADD(DAY, -15, SYSDATETIME()), DATEADD(DAY, 60, SYSDATETIME()), 1, 'ACTIVE', u.UserID
    FROM dbo.Users u WHERE u.Email = 'admin@watchstore.vn';
END

-- 14.11. BANNERS
IF NOT EXISTS (SELECT 1 FROM dbo.Banners WHERE PositionCode = 'HOME_HERO')
BEGIN
    INSERT INTO dbo.Banners (BannerName, Title, Subtitle, ImageUrl, TargetUrl, PositionCode, DisplayOrder, Status)
    VALUES
    (N'Banner Hero 1 - Seiko Presage', N'ĐẲNG CẤP ĐỒNG HỒ CƠ NHẬT BẢN', N'Bộ sưu tập Seiko Presage Cocktail Time chính hãng giảm tới 20%', '/assets/images/banners/banner1.jpg', '/page/product-detail?id=1', 'HOME_HERO', 1, 'ACTIVE'),
    (N'Banner Hero 2 - Tissot Swiss', N'TISSOT POWERMATIC 80 - THỤY SỸ', N'Trữ cót 80 giờ, kính Sapphire chống xước tuyệt đối', '/assets/images/banners/banner2.jpg', '/page/product-detail?id=4', 'HOME_HERO', 2, 'ACTIVE'),
    (N'Banner Hero 3 - CasiOak', N'CASIO G-SHOCK GA-2100 CASIOAK', N'Biểu tượng thể thao góc cạnh siêu bền bỉ', '/assets/images/banners/banner3.jpg', '/page/product-detail?id=3', 'HOME_HERO', 3, 'ACTIVE');
END

-- 14.12. SYSTEM SETTINGS
IF NOT EXISTS (SELECT 1 FROM dbo.SystemSettings WHERE SettingKey = 'SHOP_NAME')
BEGIN
    INSERT INTO dbo.SystemSettings (SettingKey, SettingValue, SettingGroup, Description, IsPublic)
    VALUES
    ('SHOP_NAME', N'WatchStore Pro - Đồng Hồ Chính Hãng', 'GENERAL', N'Tên thương hiệu website', 1),
    ('HOTLINE', '1900 6868', 'GENERAL', N'Hotline tổng đài tư vấn', 1),
    ('CONTACT_EMAIL', 'support@watchstore.vn', 'GENERAL', N'Email liên hệ chăm sóc khách hàng', 1),
    ('SHOWROOM_ADDRESS', N'154 Cầu Giấy, Q. Cầu Giấy, Hà Nội', 'GENERAL', N'Địa chỉ showroom chính', 1),
    ('CURRENCY_SYMBOL', N'VNĐ', 'GENERAL', N'Đơn vị tiền tệ', 1);
END

GO

PRINT N'================================================================================';
PRINT N'  KHỞI TẠO CƠ SỞ DỮ LIỆU WATCHSTORE HOÀN TẤT THÀNH CÔNG!';
PRINT N'================================================================================';

use master;
alter database WatchStore set single_user with rollback immediate;
drop database WatchStore

------------------------

   
SELECT name FROM sys.tables ORDER BY name;
SELECT name FROM sys.views ORDER BY name;

SELECT * FROM dbo.InventoryBalances;

SELECT * FROM dbo.vw_LowStock;