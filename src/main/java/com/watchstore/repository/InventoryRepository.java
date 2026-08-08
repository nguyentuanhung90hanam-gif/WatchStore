package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.InventoryItem;
import com.watchstore.model.InventoryTransaction;
import com.watchstore.model.Warehouse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryRepository {

    public List<InventoryItem> findAll() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM dbo.vw_InventoryOverview ORDER BY WarehouseName, ProductName, VariantName";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setWarehouseId(rs.getInt("WarehouseID"));
                item.setWarehouseName(rs.getString("WarehouseName"));
                item.setProductId(rs.getInt("ProductID"));
                item.setProductName(rs.getString("ProductName"));
                item.setVariantId(rs.getInt("VariantID"));
                item.setSku(rs.getString("SKU"));
                item.setVariantName(rs.getString("VariantName"));
                item.setQuantityOnHand(rs.getInt("QuantityOnHand"));
                item.setQuantityReserved(rs.getInt("QuantityReserved"));
                item.setAvailableQuantity(rs.getInt("AvailableQuantity"));
                item.setReorderLevel(rs.getInt("ReorderLevel"));
                item.setStockStatus(rs.getString("StockStatus"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<InventoryItem> findLowStock() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM dbo.vw_InventoryOverview " +
                "WHERE AvailableQuantity <= ReorderLevel " +
                "ORDER BY AvailableQuantity ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setWarehouseId(rs.getInt("WarehouseID"));
                item.setWarehouseName(rs.getString("WarehouseName"));
                item.setProductId(rs.getInt("ProductID"));
                item.setProductName(rs.getString("ProductName"));
                item.setVariantId(rs.getInt("VariantID"));
                item.setSku(rs.getString("SKU"));
                item.setVariantName(rs.getString("VariantName"));
                item.setQuantityOnHand(rs.getInt("QuantityOnHand"));
                item.setQuantityReserved(rs.getInt("QuantityReserved"));
                item.setAvailableQuantity(rs.getInt("AvailableQuantity"));
                item.setReorderLevel(rs.getInt("ReorderLevel"));
                item.setStockStatus(rs.getString("StockStatus"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public int getTotalQuantityOnHand() {
        String sql = "SELECT SUM(QuantityOnHand) FROM dbo.InventoryBalances";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getLowStockAlertCount() {
        String sql = "SELECT COUNT(1) " +
                "FROM dbo.vw_InventoryOverview " +
                "WHERE AvailableQuantity <= ReorderLevel";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Warehouse> findAllWarehouses() {
        List<Warehouse> list = new ArrayList<>();
        String sql = "SELECT * FROM dbo.Warehouses";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Warehouse w = new Warehouse();
                w.setWarehouseId(rs.getInt("WarehouseID"));
                w.setWarehouseName(rs.getString("WarehouseName"));
                w.setAddress(rs.getString("Address"));
                list.add(w);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<InventoryTransaction> findAllTransactions() {
        List<InventoryTransaction> list = new ArrayList<>();

        String sql = "SELECT it.*, w.WarehouseName, pv.SKU, " +
                "p.ProductName, " +
                "(SELECT STRING_AGG(pa.AttributeName + ': ' + pav.Value, ', ') " +
                " FROM dbo.VariantAttributeValues vav " +
                " INNER JOIN dbo.ProductAttributeValues pav ON vav.ValueID = pav.ValueID " +
                " INNER JOIN dbo.ProductAttributes pa ON pav.AttributeID = pa.AttributeID " +
                " WHERE vav.VariantID = pv.VariantID) as VariantName, " +
                "u.FullName as CreatedByName " +
                "FROM dbo.InventoryTransactions it " +
                "INNER JOIN dbo.Warehouses w ON it.WarehouseID = w.WarehouseID " +
                "INNER JOIN dbo.ProductVariants pv ON it.VariantID = pv.VariantID " +
                "INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID " +
                "LEFT JOIN dbo.Users u ON it.CreatedBy = u.UserID " +
                "ORDER BY it.CreatedAt DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventoryTransaction t = new InventoryTransaction();

                t.setInventoryTransactionId(rs.getLong("InventoryTransactionID"));
                t.setWarehouseId(rs.getInt("WarehouseID"));
                t.setWarehouseName(rs.getString("WarehouseName"));
                t.setVariantId(rs.getInt("VariantID"));
                t.setSku(rs.getString("SKU"));
                t.setProductName(rs.getString("ProductName"));
                t.setVariantName(rs.getString("VariantName"));
                t.setTransactionType(rs.getString("TransactionType"));
                t.setQuantityChange(rs.getInt("QuantityChange"));
                t.setQuantityBefore(rs.getInt("QuantityBefore"));
                t.setQuantityAfter(rs.getInt("QuantityAfter"));
                t.setReferenceType(rs.getString("ReferenceType"));
                t.setReferenceId(
                        rs.getObject("ReferenceID") != null
                                ? rs.getLong("ReferenceID")
                                : null
                );
                t.setNote(rs.getString("Note"));
                t.setCreatedBy(rs.getInt("CreatedBy"));
                t.setCreatedByName(rs.getString("CreatedByName"));

                if (rs.getTimestamp("CreatedAt") != null) {
                    t.setCreatedAt(
                            rs.getTimestamp("CreatedAt").toLocalDateTime()
                    );
                }

                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}

