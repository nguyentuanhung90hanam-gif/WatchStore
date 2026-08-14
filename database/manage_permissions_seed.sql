USE WatchStore;
GO

-- 1. Thêm các Permissions (Chỉ Insert những cái chưa có để tránh duplicate)
INSERT INTO dbo.Permissions (PermissionCode, PermissionName, ModuleCode, Description)
SELECT * FROM (VALUES
    ('DASHBOARD_VIEW', N'Xem Dashboard', 'DASHBOARD', N'Xem giao diện tổng quan'),

    ('ORDERS_VIEW', N'Xem Đơn hàng', 'ORDERS', N'Xem danh sách và chi tiết đơn hàng'),
    ('ORDERS_MANAGE', N'Quản lý Đơn hàng', 'ORDERS', N'Thêm, sửa, xóa, duyệt đơn hàng'),

    ('CUSTOMERS_VIEW', N'Xem Khách hàng', 'CUSTOMERS', N'Xem danh sách khách hàng'),
    ('CUSTOMERS_MANAGE', N'Quản lý Khách hàng', 'CUSTOMERS', N'Sửa thông tin khách hàng'),

    ('DELIVERY_VIEW', N'Xem Vận chuyển', 'DELIVERY', N'Xem trạng thái vận chuyển'),

    ('RETURNS_VIEW', N'Xem Đổi trả', 'RETURNS', N'Xem danh sách đổi trả'),
    ('RETURNS_MANAGE', N'Quản lý Đổi trả', 'RETURNS', N'Xử lý yêu cầu đổi trả'),

    ('WARRANTY_VIEW', N'Xem Bảo hành', 'WARRANTY', N'Xem danh sách bảo hành'),

    ('INVENTORY_VIEW', N'Xem Tồn kho', 'INVENTORY', N'Xem danh sách tồn kho'),
    ('INVENTORY_MANAGE', N'Quản lý Tồn kho', 'INVENTORY', N'Điều chỉnh tồn kho'),

    ('STOCK_RECEIPT_VIEW', N'Xem Phiếu nhập', 'INVENTORY', N'Xem phiếu nhập kho'),
    ('STOCK_RECEIPT_MANAGE', N'Quản lý Phiếu nhập', 'INVENTORY', N'Tạo và duyệt phiếu nhập'),

    ('STOCK_EXPORT_VIEW', N'Xem Phiếu xuất', 'INVENTORY', N'Xem phiếu xuất kho'),
    ('STOCK_EXPORT_MANAGE', N'Quản lý Phiếu xuất', 'INVENTORY', N'Tạo và duyệt phiếu xuất'),

    ('STOCKTAKE_VIEW', N'Xem Kiểm kê', 'INVENTORY', N'Xem phiếu kiểm kê'),
    ('STOCKTAKE_MANAGE', N'Quản lý Kiểm kê', 'INVENTORY', N'Tạo và duyệt phiếu kiểm kê'),

    ('VARIANTS_VIEW', N'Xem Biến thể', 'INVENTORY', N'Xem biến thể sản phẩm'),
    ('VARIANTS_MANAGE', N'Quản lý Biến thể', 'INVENTORY', N'Cập nhật biến thể'),

    ('SUPPLIERS_VIEW', N'Xem Nhà cung cấp', 'INVENTORY', N'Xem danh sách nhà cung cấp'),

    ('PRODUCTS_VIEW', N'Xem Sản phẩm', 'CATALOG', N'Xem danh sách sản phẩm'),
    ('PRODUCTS_MANAGE', N'Quản lý Sản phẩm', 'CATALOG', N'Thêm, sửa, xóa sản phẩm'),

    ('CATEGORIES_VIEW', N'Xem Danh mục', 'CATALOG', N'Xem danh mục'),
    ('CATEGORIES_MANAGE', N'Quản lý Danh mục', 'CATALOG', N'Thêm, sửa, xóa danh mục'),

    ('BRANDS_VIEW', N'Xem Thương hiệu', 'CATALOG', N'Xem thương hiệu'),
    ('BRANDS_MANAGE', N'Quản lý Thương hiệu', 'CATALOG', N'Thêm, sửa, xóa thương hiệu'),

    ('VOUCHERS_VIEW', N'Xem Voucher', 'MARKETING', N'Xem danh sách voucher'),
    ('VOUCHERS_MANAGE', N'Quản lý Voucher', 'MARKETING', N'Tạo, sửa, xóa voucher'),

    ('BANNERS_VIEW', N'Xem Banner', 'MARKETING', N'Xem danh sách banner'),
    ('BANNERS_MANAGE', N'Quản lý Banner', 'MARKETING', N'Thêm, sửa, xóa banner'),

    ('POSTS_VIEW', N'Xem Bài viết', 'MARKETING', N'Xem bài viết'),
    ('POSTS_MANAGE', N'Quản lý Bài viết', 'MARKETING', N'Đăng bài viết'),

    ('NOTIFICATIONS_VIEW', N'Xem Thông báo', 'MARKETING', N'Xem thông báo'),
    ('NOTIFICATIONS_MANAGE', N'Quản lý Thông báo', 'MARKETING', N'Gửi thông báo'),

    ('STATISTICS_VIEW', N'Xem Thống kê', 'REPORTS', N'Xem thống kê tổng hợp'),

    ('REPORTS_VIEW', N'Xem Báo cáo', 'REPORTS', N'Xem và xuất báo cáo'),

    ('ACCOUNTS_VIEW', N'Xem Tài khoản', 'SYSTEM', N'Xem danh sách tài khoản'),
    ('ACCOUNTS_MANAGE', N'Quản lý Tài khoản', 'SYSTEM', N'Sửa, xóa, cấp quyền tài khoản'),

    ('ROLES_VIEW', N'Xem Vai trò', 'SYSTEM', N'Xem danh sách vai trò'),
    ('ROLES_MANAGE', N'Quản lý Vai trò', 'SYSTEM', N'Tạo, sửa vai trò'),

    ('PERMISSIONS_VIEW', N'Xem Quyền hạn', 'SYSTEM', N'Xem danh sách quyền'),
    ('PERMISSIONS_MANAGE', N'Quản lý Quyền hạn', 'SYSTEM', N'Quản lý các quyền hệ thống'),

    ('PROFILE_VIEW', N'Xem Hồ sơ', 'SYSTEM', N'Xem thông tin cá nhân'),
    ('PROFILE_MANAGE', N'Cập nhật Hồ sơ', 'SYSTEM', N'Chỉnh sửa thông tin cá nhân')
) AS v(PermissionCode, PermissionName, ModuleCode, Description)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.Permissions p WHERE p.PermissionCode = v.PermissionCode
);

-- 2. Gán toàn bộ quyền cho ADMIN
INSERT INTO dbo.RolePermissions (RoleID, PermissionID)
SELECT r.RoleID, p.PermissionID
FROM dbo.Roles r
CROSS JOIN dbo.Permissions p
WHERE r.RoleCode = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM dbo.RolePermissions rp
    WHERE rp.RoleID = r.RoleID AND rp.PermissionID = p.PermissionID
);

-- 3. Gán quyền cho SALES
INSERT INTO dbo.RolePermissions (RoleID, PermissionID)
SELECT r.RoleID, p.PermissionID
FROM dbo.Roles r
CROSS JOIN dbo.Permissions p
WHERE r.RoleCode = 'SALES'
AND p.PermissionCode IN (
    'DASHBOARD_VIEW', 'PROFILE_VIEW', 'PROFILE_MANAGE',
    'ORDERS_VIEW', 'ORDERS_MANAGE',
    'CUSTOMERS_VIEW', 'CUSTOMERS_MANAGE',
    'DELIVERY_VIEW',
    'RETURNS_VIEW', 'RETURNS_MANAGE',
    'WARRANTY_VIEW',
    'PRODUCTS_VIEW',
    'REPORTS_VIEW'
)
AND NOT EXISTS (
    SELECT 1 FROM dbo.RolePermissions rp
    WHERE rp.RoleID = r.RoleID AND rp.PermissionID = p.PermissionID
);

-- 4. Gán quyền cho WAREHOUSE
INSERT INTO dbo.RolePermissions (RoleID, PermissionID)
SELECT r.RoleID, p.PermissionID
FROM dbo.Roles r
CROSS JOIN dbo.Permissions p
WHERE r.RoleCode = 'WAREHOUSE'
AND p.PermissionCode IN (
    'DASHBOARD_VIEW', 'PROFILE_VIEW', 'PROFILE_MANAGE',
    'INVENTORY_VIEW', 'INVENTORY_MANAGE',
    'STOCK_RECEIPT_VIEW', 'STOCK_RECEIPT_MANAGE',
    'STOCK_EXPORT_VIEW', 'STOCK_EXPORT_MANAGE',
    'STOCKTAKE_VIEW', 'STOCKTAKE_MANAGE',
    'VARIANTS_VIEW', 'VARIANTS_MANAGE',
    'SUPPLIERS_VIEW',
    'REPORTS_VIEW'
)
AND NOT EXISTS (
    SELECT 1 FROM dbo.RolePermissions rp
    WHERE rp.RoleID = r.RoleID AND rp.PermissionID = p.PermissionID
);
