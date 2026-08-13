-- Customer integration objects for WatchStore.
-- Run against the existing WatchStore SQL Server database after reviewing names.
-- The script creates missing Customer tables/procedures without dropping existing data.

IF OBJECT_ID('dbo.Roles', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Roles (
        RoleID INT IDENTITY(1,1) PRIMARY KEY,
        RoleCode VARCHAR(30) NOT NULL UNIQUE,
        RoleName NVARCHAR(100) NOT NULL
    );
END;

IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = 'CUSTOMER')
    INSERT INTO dbo.Roles (RoleCode, RoleName) VALUES ('CUSTOMER', N'Khach hang');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = 'ADMIN')
    INSERT INTO dbo.Roles (RoleCode, RoleName) VALUES ('ADMIN', N'Quan tri vien');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = 'SALES')
    INSERT INTO dbo.Roles (RoleCode, RoleName) VALUES ('SALES', N'Nhan vien ban hang');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = 'WAREHOUSE')
    INSERT INTO dbo.Roles (RoleCode, RoleName) VALUES ('WAREHOUSE', N'Nhan vien kho');

IF OBJECT_ID('dbo.Users', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Users (
        UserID INT IDENTITY(1,1) PRIMARY KEY,
        Email VARCHAR(255) NOT NULL UNIQUE,
        PasswordHash VARCHAR(64) NOT NULL,
        FullName NVARCHAR(100) NOT NULL,
        Phone VARCHAR(20) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        UpdatedAt DATETIME2 NULL
    );
END;

IF OBJECT_ID('dbo.UserRoles', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.UserRoles (
        UserID INT NOT NULL,
        RoleID INT NOT NULL,
        CONSTRAINT PK_UserRoles PRIMARY KEY (UserID, RoleID),
        CONSTRAINT FK_UserRoles_Users FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID),
        CONSTRAINT FK_UserRoles_Roles FOREIGN KEY (RoleID) REFERENCES dbo.Roles(RoleID)
    );
END;

IF OBJECT_ID('dbo.UserAddresses', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.UserAddresses (
        AddressID INT IDENTITY(1,1) PRIMARY KEY,
        UserID INT NOT NULL,
        RecipientName NVARCHAR(100) NOT NULL,
        RecipientPhone VARCHAR(20) NOT NULL,
        Province NVARCHAR(100) NOT NULL,
        District NVARCHAR(100) NOT NULL,
        Ward NVARCHAR(100) NOT NULL,
        AddressLine NVARCHAR(300) NOT NULL,
        AddressType VARCHAR(20) NOT NULL DEFAULT 'HOME',
        IsDefault BIT NOT NULL DEFAULT 0,
        CONSTRAINT FK_UserAddresses_Users FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID)
    );
END;

IF OBJECT_ID('dbo.Carts', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Carts (
        CartID BIGINT IDENTITY(1,1) PRIMARY KEY,
        UserID INT NOT NULL,
        Status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_Carts_Users FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID)
    );
END;

IF OBJECT_ID('dbo.CartItems', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.CartItems (
        CartItemID BIGINT IDENTITY(1,1) PRIMARY KEY,
        CartID BIGINT NOT NULL,
        VariantID INT NOT NULL,
        Quantity INT NOT NULL,
        CONSTRAINT FK_CartItems_Carts FOREIGN KEY (CartID) REFERENCES dbo.Carts(CartID)
    );
END;

IF OBJECT_ID('dbo.Wishlists', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Wishlists (
        WishlistID BIGINT IDENTITY(1,1) PRIMARY KEY,
        UserID INT NOT NULL UNIQUE,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_Wishlists_Users FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID)
    );
END;

IF OBJECT_ID('dbo.WishlistItems', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.WishlistItems (
        WishlistItemID BIGINT IDENTITY(1,1) PRIMARY KEY,
        WishlistID BIGINT NOT NULL,
        ProductID INT NOT NULL,
        AddedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_WishlistItems UNIQUE (WishlistID, ProductID),
        CONSTRAINT FK_WishlistItems_Wishlists FOREIGN KEY (WishlistID) REFERENCES dbo.Wishlists(WishlistID)
    );
END;

IF OBJECT_ID('dbo.Orders', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Orders (
        OrderID BIGINT IDENTITY(1,1) PRIMARY KEY,
        OrderCode VARCHAR(30) NOT NULL UNIQUE,
        CustomerID INT NOT NULL,
        RecipientName NVARCHAR(100) NOT NULL,
        RecipientPhone VARCHAR(20) NOT NULL,
        ShippingAddress NVARCHAR(600) NOT NULL,
        PaymentMethod VARCHAR(30) NOT NULL DEFAULT 'COD',
        OrderStatus VARCHAR(30) NOT NULL DEFAULT 'PENDING',
        Note NVARCHAR(500) NULL,
        TotalAmount DECIMAL(18,2) NOT NULL DEFAULT 0,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_Orders_Users FOREIGN KEY (CustomerID) REFERENCES dbo.Users(UserID)
    );
END;

IF OBJECT_ID('dbo.OrderItems', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.OrderItems (
        OrderItemID BIGINT IDENTITY(1,1) PRIMARY KEY,
        OrderID BIGINT NOT NULL,
        VariantID INT NOT NULL,
        ProductName NVARCHAR(255) NOT NULL,
        VariantName NVARCHAR(100) NULL,
        SKU VARCHAR(100) NULL,
        ImageUrl NVARCHAR(500) NULL,
        UnitPrice DECIMAL(18,2) NOT NULL,
        Quantity INT NOT NULL,
        DiscountAmount DECIMAL(18,2) NOT NULL DEFAULT 0,
        LineTotal DECIMAL(18,2) NOT NULL,
        CONSTRAINT FK_OrderItems_Orders FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID)
    );
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_Login
    @Email VARCHAR(255),
    @Password NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Hash VARCHAR(64) = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', CONVERT(VARBINARY(MAX), @Password)), 2);
    SELECT TOP 1 u.UserID, u.Email, u.FullName, u.Phone, r.RoleCode
    FROM dbo.Users u
    LEFT JOIN dbo.UserRoles ur ON ur.UserID = u.UserID
    LEFT JOIN dbo.Roles r ON r.RoleID = ur.RoleID
    WHERE LOWER(u.Email) = LOWER(@Email) AND UPPER(u.PasswordHash) = @Hash;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RegisterCustomer
    @Email VARCHAR(255),
    @Password NVARCHAR(100),
    @FullName NVARCHAR(100),
    @Phone VARCHAR(20) = NULL,
    @Gender VARCHAR(20) = NULL,
    @BirthDate DATE = NULL,
    @RecipientName NVARCHAR(100) = NULL,
    @RecipientPhone VARCHAR(20) = NULL,
    @Province NVARCHAR(100) = NULL,
    @District NVARCHAR(100) = NULL,
    @Ward NVARCHAR(100) = NULL,
    @AddressLine NVARCHAR(300) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM dbo.Users WHERE LOWER(Email) = LOWER(@Email))
        THROW 50001, 'Email da ton tai.', 1;

    DECLARE @Hash VARCHAR(64) = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', CONVERT(VARBINARY(MAX), @Password)), 2);
    INSERT INTO dbo.Users (Email, PasswordHash, FullName, Phone) VALUES (@Email, @Hash, @FullName, @Phone);

    DECLARE @UserID INT = SCOPE_IDENTITY();
    INSERT INTO dbo.UserRoles (UserID, RoleID)
    SELECT @UserID, RoleID FROM dbo.Roles WHERE RoleCode = 'CUSTOMER';

    SELECT u.UserID, u.Email, u.FullName, u.Phone, 'CUSTOMER' AS RoleCode
    FROM dbo.Users u WHERE u.UserID = @UserID;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_SaveAddress
    @UserID INT, @AddressID INT = NULL, @Name NVARCHAR(100), @Phone VARCHAR(20),
    @Province NVARCHAR(100), @District NVARCHAR(100), @Ward NVARCHAR(100),
    @Line NVARCHAR(300), @Type VARCHAR(20), @IsDefault BIT
AS
BEGIN
    SET NOCOUNT ON;
    IF @IsDefault = 1 UPDATE dbo.UserAddresses SET IsDefault = 0 WHERE UserID = @UserID;
    IF @AddressID IS NULL
        INSERT INTO dbo.UserAddresses (UserID, RecipientName, RecipientPhone, Province, District, Ward, AddressLine, AddressType, IsDefault)
        VALUES (@UserID, @Name, @Phone, @Province, @District, @Ward, @Line, ISNULL(@Type, 'HOME'), @IsDefault);
    ELSE
        UPDATE dbo.UserAddresses
        SET RecipientName=@Name, RecipientPhone=@Phone, Province=@Province, District=@District, Ward=@Ward,
            AddressLine=@Line, AddressType=ISNULL(@Type, 'HOME'), IsDefault=@IsDefault
        WHERE UserID=@UserID AND AddressID=@AddressID;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_DeleteAddress @UserID INT, @AddressID INT
AS
BEGIN
    DELETE FROM dbo.UserAddresses WHERE UserID=@UserID AND AddressID=@AddressID;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ToggleWishlist @UserID INT, @ProductID INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @WishlistID BIGINT;
    SELECT @WishlistID = WishlistID FROM dbo.Wishlists WHERE UserID=@UserID;
    IF @WishlistID IS NULL
    BEGIN
        INSERT INTO dbo.Wishlists (UserID) VALUES (@UserID);
        SET @WishlistID = SCOPE_IDENTITY();
    END;
    IF EXISTS (SELECT 1 FROM dbo.WishlistItems WHERE WishlistID=@WishlistID AND ProductID=@ProductID)
    BEGIN
        DELETE FROM dbo.WishlistItems WHERE WishlistID=@WishlistID AND ProductID=@ProductID;
        SELECT CAST(0 AS BIT) AS IsFavorite;
    END
    ELSE
    BEGIN
        INSERT INTO dbo.WishlistItems (WishlistID, ProductID) VALUES (@WishlistID, @ProductID);
        SELECT CAST(1 AS BIT) AS IsFavorite;
    END;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_AddToCart
    @UserID INT, @VariantID INT, @Quantity INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @CartID BIGINT;
    SELECT @CartID = CartID FROM dbo.Carts WHERE UserID=@UserID AND Status='ACTIVE';
    IF @CartID IS NULL
    BEGIN
        INSERT INTO dbo.Carts (UserID, Status) VALUES (@UserID, 'ACTIVE');
        SET @CartID = SCOPE_IDENTITY();
    END;

    IF EXISTS (SELECT 1 FROM dbo.CartItems WHERE CartID=@CartID AND VariantID=@VariantID)
        UPDATE dbo.CartItems SET Quantity = Quantity + @Quantity WHERE CartID=@CartID AND VariantID=@VariantID;
    ELSE
        INSERT INTO dbo.CartItems (CartID, VariantID, Quantity) VALUES (@CartID, @VariantID, @Quantity);
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_UpdateCartItem
    @UserID INT, @VariantID INT, @Quantity INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @CartID BIGINT;
    SELECT @CartID = CartID FROM dbo.Carts WHERE UserID=@UserID AND Status='ACTIVE';
    IF @CartID IS NULL RETURN;
    IF @Quantity <= 0
        DELETE FROM dbo.CartItems WHERE CartID=@CartID AND VariantID=@VariantID;
    ELSE
        UPDATE dbo.CartItems SET Quantity=@Quantity WHERE CartID=@CartID AND VariantID=@VariantID;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RemoveCartItem
    @UserID INT, @VariantID INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @CartID BIGINT;
    SELECT @CartID = CartID FROM dbo.Carts WHERE UserID=@UserID AND Status='ACTIVE';
    IF @CartID IS NOT NULL
        DELETE FROM dbo.CartItems WHERE CartID=@CartID AND VariantID=@VariantID;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_CreateOrderFromCart
    @UserID INT,
    @AddressID INT,
    @VoucherCode VARCHAR(50) = NULL,
    @Payment VARCHAR(30) = 'COD',
    @Note NVARCHAR(500) = NULL,
    @OrderID BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @CartID BIGINT;
    SELECT @CartID = CartID FROM dbo.Carts WHERE UserID=@UserID AND Status='ACTIVE';
    IF @CartID IS NULL OR NOT EXISTS (SELECT 1 FROM dbo.CartItems WHERE CartID=@CartID)
        THROW 50002, 'Gio hang dang trong.', 1;

    DECLARE @RecipientName NVARCHAR(100), @RecipientPhone VARCHAR(20), @ShippingAddress NVARCHAR(600);
    SELECT @RecipientName=RecipientName, @RecipientPhone=RecipientPhone,
           @ShippingAddress=CONCAT(AddressLine, ', ', Ward, ', ', District, ', ', Province)
    FROM dbo.UserAddresses
    WHERE UserID=@UserID AND AddressID=@AddressID;
    IF @RecipientName IS NULL THROW 50003, 'Dia chi giao hang khong hop le.', 1;

    BEGIN TRANSACTION;

    DECLARE @Total DECIMAL(18,2);
    SELECT @Total = SUM(ci.Quantity * pv.SalePrice)
    FROM dbo.CartItems ci
    JOIN dbo.ProductVariants pv ON pv.VariantID = ci.VariantID
    WHERE ci.CartID = @CartID;

    INSERT INTO dbo.Orders (OrderCode, CustomerID, RecipientName, RecipientPhone, ShippingAddress, PaymentMethod, OrderStatus, Note, TotalAmount)
    VALUES (CONCAT('WS', FORMAT(SYSDATETIME(), 'yyMMddHHmmss'), RIGHT(CONCAT('000', @UserID), 3)),
            @UserID, @RecipientName, @RecipientPhone, @ShippingAddress, ISNULL(@Payment, 'COD'), 'PENDING', @Note, ISNULL(@Total, 0));
    SET @OrderID = SCOPE_IDENTITY();

    INSERT INTO dbo.OrderItems (OrderID, VariantID, ProductName, VariantName, SKU, ImageUrl, UnitPrice, Quantity, DiscountAmount, LineTotal)
    SELECT @OrderID, pv.VariantID, p.ProductName, pv.VariantName, pv.SKU,
           (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID=p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC),
           pv.SalePrice, ci.Quantity, 0, pv.SalePrice * ci.Quantity
    FROM dbo.CartItems ci
    JOIN dbo.ProductVariants pv ON pv.VariantID = ci.VariantID
    JOIN dbo.Products p ON p.ProductID = pv.ProductID
    WHERE ci.CartID = @CartID;

    UPDATE dbo.Carts SET Status='ORDERED' WHERE CartID=@CartID;
    COMMIT TRANSACTION;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_UpdateOrderStatus
    @OrderID BIGINT, @Status VARCHAR(30), @ChangedBy INT, @Note NVARCHAR(500) = NULL
AS
BEGIN
    UPDATE dbo.Orders SET OrderStatus=@Status WHERE OrderID=@OrderID;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_CancelOrder
    @OrderID BIGINT, @UserID INT, @Reason NVARCHAR(500)
AS
BEGIN
    UPDATE dbo.Orders
    SET OrderStatus='CANCELLED', Note=@Reason
    WHERE OrderID=@OrderID AND CustomerID=@UserID AND OrderStatus IN ('PENDING','CONFIRMED');
END;
GO
