package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Customer;
import com.watchstore.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    private final UserRepository userRepository = new UserRepository();

    /**
     * Lấy danh sách tất cả khách hàng kiểu List<Customer>
     */
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = """
            SELECT u.UserID, u.Email, u.PasswordHash, u.FullName, u.Phone, 
                   (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address,
                   r.RoleCode
            FROM Users u
            JOIN UserRoles ur ON u.UserID = ur.UserID
            JOIN Roles r ON ur.RoleID = r.RoleID
            WHERE r.RoleCode = 'CUSTOMER'
            ORDER BY u.UserID DESC
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    /**
     * Tìm khách hàng theo ID
     */
    public Customer findById(int id) {
        String sql = """
            SELECT u.UserID, u.Email, u.PasswordHash, u.FullName, u.Phone, 
                   (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address,
                   r.RoleCode
            FROM Users u
            JOIN UserRoles ur ON u.UserID = ur.UserID
            JOIN Roles r ON ur.RoleID = r.RoleID
            WHERE r.RoleCode = 'CUSTOMER' AND u.UserID = ?
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm kiếm khách hàng theo tên hoặc số điện thoại
     */
    public List<Customer> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        List<Customer> result = new ArrayList<>();
        String sql = """
            SELECT u.UserID, u.Email, u.PasswordHash, u.FullName, u.Phone, 
                   (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address,
                   r.RoleCode
            FROM Users u
            JOIN UserRoles ur ON u.UserID = ur.UserID
            JOIN Roles r ON ur.RoleID = r.RoleID
            WHERE r.RoleCode = 'CUSTOMER' 
              AND (LOWER(u.FullName) LIKE ? OR u.Phone LIKE ? OR LOWER(u.Email) LIKE ? OR CAST(u.UserID AS VARCHAR) LIKE ?)
            ORDER BY u.UserID DESC
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    public void update(Customer customer) {
        String sqlUser = "UPDATE Users SET FullName = ?, Email = ?, Phone = ? WHERE UserID = ?";
        String sqlCheckAddr = "SELECT AddressID FROM UserAddresses WHERE UserID = ? AND IsDefault = 1";
        String sqlUpdateAddr = "UPDATE UserAddresses SET RecipientName = ?, RecipientPhone = ?, AddressLine = ? WHERE UserID = ? AND IsDefault = 1";
        String sqlInsertAddr = "INSERT INTO UserAddresses (UserID, RecipientName, RecipientPhone, Province, District, Ward, AddressLine, IsDefault) VALUES (?, ?, ?, 'Hà Nội', 'Nam Từ Liêm', 'Mỹ Đình', ?, 1)";

        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Update User info
                try (PreparedStatement ps = con.prepareStatement(sqlUser)) {
                    ps.setString(1, customer.getFullName());
                    ps.setString(2, customer.getEmail());
                    ps.setString(3, customer.getPhone());
                    ps.setInt(4, customer.getId());
                    ps.executeUpdate();
                }

                // 2. Check address
                boolean hasAddress = false;
                try (PreparedStatement ps = con.prepareStatement(sqlCheckAddr)) {
                    ps.setInt(1, customer.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            hasAddress = true;
                        }
                    }
                }

                // 3. Update or Insert address
                if (hasAddress) {
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdateAddr)) {
                        ps.setString(1, customer.getFullName());
                        ps.setString(2, customer.getPhone() != null ? customer.getPhone() : "");
                        ps.setString(3, customer.getAddress());
                        ps.setInt(4, customer.getId());
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(sqlInsertAddr)) {
                        ps.setInt(1, customer.getId());
                        ps.setString(2, customer.getFullName());
                        ps.setString(3, customer.getPhone() != null ? customer.getPhone() : "");
                        ps.setString(4, customer.getAddress());
                        ps.executeUpdate();
                    }
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thêm mới khách hàng (Đảm bảo phân quyền CUSTOMER)
     */
    public boolean insert(Customer customer) {
        if (customer == null) return false;
        String sqlUser = "INSERT INTO Users (Email, PasswordHash, FullName, Phone, Status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        String sqlRole = "INSERT INTO UserRoles (UserID, RoleID) VALUES (?, (SELECT RoleID FROM Roles WHERE RoleCode = 'CUSTOMER'))";
        String sqlAddr = "INSERT INTO UserAddresses (UserID, RecipientName, RecipientPhone, Province, District, Ward, AddressLine, IsDefault) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

        String defaultPassHash = userRepository.hashPassword("123456"); // Mật khẩu mặc định

        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try {
                int userId = -1;
                try (PreparedStatement psUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, customer.getEmail());
                    psUser.setString(2, defaultPassHash);
                    psUser.setString(3, customer.getFullName());
                    psUser.setString(4, customer.getPhone());
                    psUser.executeUpdate();

                    try (ResultSet rsKeys = psUser.getGeneratedKeys()) {
                        if (rsKeys.next()) {
                            userId = rsKeys.getInt(1);
                        }
                    }
                }

                if (userId == -1) {
                    throw new SQLException("Failed to get auto-generated UserID.");
                }

                // Gán vai trò CUSTOMER
                try (PreparedStatement psRole = con.prepareStatement(sqlRole)) {
                    psRole.setInt(1, userId);
                    psRole.executeUpdate();
                }

                // Thêm địa chỉ mặc định
                try (PreparedStatement psAddr = con.prepareStatement(sqlAddr)) {
                    psAddr.setInt(1, userId);
                    psAddr.setString(2, customer.getFullName());
                    psAddr.setString(3, customer.getPhone() != null ? customer.getPhone() : "");
                    psAddr.setString(4, "Hà Nội");
                    psAddr.setString(5, "Nam Từ Liêm");
                    psAddr.setString(6, "Mỹ Đình");
                    psAddr.setString(7, customer.getAddress() != null ? customer.getAddress() : "Hà Nội");
                    psAddr.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("UserID"));
        c.setUsername(rs.getString("Email"));
        c.setPassword(rs.getString("PasswordHash"));
        c.setFullName(rs.getString("FullName"));
        c.setEmail(rs.getString("Email"));
        c.setPhone(rs.getString("Phone"));
        c.setAddress(rs.getString("Address") != null ? rs.getString("Address") : "Chưa cập nhật địa chỉ");
        return c;
    }
}