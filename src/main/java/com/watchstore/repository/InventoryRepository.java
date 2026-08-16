package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.InventoryItem;
import com.watchstore.model.InventoryTransaction;
import com.watchstore.model.Warehouse;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryRepository {

    public List<InventoryItem> findAll() throws SQLException {
        List<InventoryItem> list = new ArrayList<>();

        String sql =
                "SELECT WarehouseID, WarehouseName, ProductID, ProductName, " +
                        "VariantID, SKU, VariantName, QuantityOnHand, QuantityReserved, " +
                        "AvailableQuantity, ReorderLevel, StockStatus " +
                        "FROM dbo.vw_InventoryOverview " +
                        "ORDER BY WarehouseName, ProductName, VariantName";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapInventoryItem(rs));
            }
        }

        return list;
    }

    public List<InventoryItem> findLowStock() throws SQLException {
        List<InventoryItem> list = new ArrayList<>();

        String sql =
                "SELECT WarehouseID, WarehouseName, ProductID, ProductName, " +
                        "VariantID, SKU, VariantName, QuantityOnHand, QuantityReserved, " +
                        "AvailableQuantity, ReorderLevel, StockStatus " +
                        "FROM dbo.vw_InventoryOverview " +
                        "WHERE AvailableQuantity <= ReorderLevel " +
                        "ORDER BY AvailableQuantity ASC, WarehouseName, ProductName, VariantName";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapInventoryItem(rs));
            }
        }

        return list;
    }

    public int getTotalQuantityOnHand() throws SQLException {
        String sql =
                "SELECT COALESCE(SUM(QuantityOnHand), 0) " +
                        "FROM dbo.InventoryBalances";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    public int getLowStockAlertCount() throws SQLException {
        String sql =
                "SELECT COUNT(1) " +
                        "FROM dbo.vw_InventoryOverview " +
                        "WHERE AvailableQuantity <= ReorderLevel";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    public int getOutOfStockCount() throws SQLException {
        String sql =
                "SELECT COUNT(1) " +
                        "FROM dbo.vw_InventoryOverview " +
                        "WHERE AvailableQuantity <= 0";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    public int getInventoryItemCount() throws SQLException {
        String sql =
                "SELECT COUNT(1) " +
                        "FROM dbo.InventoryBalances";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    public List<Warehouse> findAllWarehouses() throws SQLException {
        List<Warehouse> list = new ArrayList<>();

        String sql =
                "SELECT WarehouseID, WarehouseName, Address " +
                        "FROM dbo.Warehouses " +
                        "WHERE Status = 'ACTIVE' " +
                        "ORDER BY WarehouseName";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Warehouse warehouse = new Warehouse();

                warehouse.setWarehouseId(
                        rs.getInt("WarehouseID")
                );

                warehouse.setWarehouseName(
                        rs.getString("WarehouseName")
                );

                warehouse.setAddress(
                        rs.getString("Address")
                );

                list.add(warehouse);
            }
        }

        return list;
    }

    public List<InventoryTransaction> findAllTransactions()
            throws SQLException {

        List<InventoryTransaction> list =
                new ArrayList<>();

        String sql =
                "SELECT it.InventoryTransactionID, " +
                        "it.WarehouseID, " +
                        "w.WarehouseName, " +
                        "it.VariantID, " +
                        "pv.SKU, " +
                        "p.ProductName, " +

                        "(SELECT STRING_AGG(" +
                        "pa.AttributeName + ': ' + pav.ValueText, ', ') " +
                        "FROM dbo.VariantAttributeValues vav " +
                        "INNER JOIN dbo.ProductAttributeValues pav " +
                        "ON vav.AttributeValueID = pav.AttributeValueID " +
                        "INNER JOIN dbo.ProductAttributes pa " +
                        "ON pav.AttributeID = pa.AttributeID " +
                        "WHERE vav.VariantID = pv.VariantID" +
                        ") AS VariantName, " +

                        "it.TransactionType, " +
                        "it.QuantityChange, " +
                        "it.QuantityBefore, " +
                        "it.QuantityAfter, " +
                        "it.ReferenceType, " +
                        "it.ReferenceID, " +
                        "it.Note, " +
                        "it.CreatedBy, " +
                        "u.FullName AS CreatedByName, " +
                        "it.CreatedAt " +

                        "FROM dbo.InventoryTransactions it " +

                        "INNER JOIN dbo.Warehouses w " +
                        "ON it.WarehouseID = w.WarehouseID " +

                        "INNER JOIN dbo.ProductVariants pv " +
                        "ON it.VariantID = pv.VariantID " +

                        "INNER JOIN dbo.Products p " +
                        "ON pv.ProductID = p.ProductID " +

                        "LEFT JOIN dbo.Users u " +
                        "ON it.CreatedBy = u.UserID " +

                        "ORDER BY it.CreatedAt DESC, " +
                        "it.InventoryTransactionID DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                InventoryTransaction transaction =
                        new InventoryTransaction();

                transaction.setInventoryTransactionId(
                        rs.getLong("InventoryTransactionID")
                );

                transaction.setWarehouseId(
                        rs.getInt("WarehouseID")
                );

                transaction.setWarehouseName(
                        rs.getString("WarehouseName")
                );

                transaction.setVariantId(
                        rs.getInt("VariantID")
                );

                transaction.setSku(
                        rs.getString("SKU")
                );

                transaction.setProductName(
                        rs.getString("ProductName")
                );

                transaction.setVariantName(
                        rs.getString("VariantName")
                );

                transaction.setTransactionType(
                        rs.getString("TransactionType")
                );

                transaction.setQuantityChange(
                        rs.getInt("QuantityChange")
                );

                transaction.setQuantityBefore(
                        rs.getInt("QuantityBefore")
                );

                transaction.setQuantityAfter(
                        rs.getInt("QuantityAfter")
                );

                transaction.setReferenceType(
                        rs.getString("ReferenceType")
                );

                Object referenceId =
                        rs.getObject("ReferenceID");

                if (referenceId != null) {
                    transaction.setReferenceId(
                            rs.getLong("ReferenceID")
                    );
                } else {
                    transaction.setReferenceId(null);
                }

                transaction.setNote(
                        rs.getString("Note")
                );

                int createdBy =
                        rs.getInt("CreatedBy");

                if (!rs.wasNull()) {
                    transaction.setCreatedBy(
                            createdBy
                    );
                } else {
                    transaction.setCreatedBy(0);
                }

                transaction.setCreatedByName(
                        rs.getString("CreatedByName")
                );

                if (rs.getTimestamp("CreatedAt") != null) {
                    transaction.setCreatedAt(
                            rs.getTimestamp("CreatedAt")
                                    .toLocalDateTime()
                    );
                }

                list.add(transaction);
            }
        }

        return list;
    }


    public List<InventoryItem> search(String keyword, Integer warehouseId) throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT WarehouseID, WarehouseName, ProductID, ProductName, " +
                        "VariantID, SKU, VariantName, QuantityOnHand, QuantityReserved, " +
                        "AvailableQuantity, ReorderLevel, StockStatus " +
                        "FROM dbo.vw_InventoryOverview WHERE 1 = 1 "
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (SKU LIKE ? OR ProductName LIKE ? OR VariantName LIKE ?) ");
            String value = "%" + keyword.trim() + "%";
            params.add(value); params.add(value); params.add(value);
        }
        if (warehouseId != null && warehouseId > 0) {
            sql.append("AND WarehouseID = ? ");
            params.add(warehouseId);
        }
        sql.append("ORDER BY WarehouseName, ProductName, VariantName");
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()) list.add(mapInventoryItem(rs));
            }
        }
        return list;
    }

    public List<InventoryTransaction> searchTransactions(String keyword) throws SQLException {
        List<InventoryTransaction> list=new ArrayList<>();
        StringBuilder sql=new StringBuilder(
                "SELECT it.InventoryTransactionID,it.WarehouseID,w.WarehouseName,it.VariantID,pv.SKU,p.ProductName, " +
                        "(SELECT STRING_AGG(pa.AttributeName + ': ' + pav.ValueText, ', ') FROM dbo.VariantAttributeValues vav " +
                        "INNER JOIN dbo.ProductAttributeValues pav ON vav.AttributeValueID=pav.AttributeValueID " +
                        "INNER JOIN dbo.ProductAttributes pa ON pav.AttributeID=pa.AttributeID WHERE vav.VariantID=pv.VariantID) AS VariantName, " +
                        "it.TransactionType,it.QuantityChange,it.QuantityBefore,it.QuantityAfter,it.ReferenceType,it.ReferenceID,it.Note,it.CreatedBy,u.FullName AS CreatedByName,it.CreatedAt " +
                        "FROM dbo.InventoryTransactions it INNER JOIN dbo.Warehouses w ON it.WarehouseID=w.WarehouseID " +
                        "INNER JOIN dbo.ProductVariants pv ON it.VariantID=pv.VariantID INNER JOIN dbo.Products p ON pv.ProductID=p.ProductID " +
                        "LEFT JOIN dbo.Users u ON it.CreatedBy=u.UserID WHERE 1=1 "
        );
        List<Object> params=new ArrayList<>();
        if(keyword!=null&&!keyword.trim().isEmpty()){
            sql.append("AND (pv.SKU LIKE ? OR p.ProductName LIKE ? OR pv.VariantName LIKE ? OR it.TransactionType LIKE ? OR it.ReferenceType LIKE ? OR it.Note LIKE ? OR w.WarehouseName LIKE ?) ");
            String v="%"+keyword.trim()+"%"; for(int i=0;i<7;i++)params.add(v);
        }
        sql.append("ORDER BY it.CreatedAt DESC,it.InventoryTransactionID DESC");
        try(Connection conn=DBContext.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            for(int i=0;i<params.size();i++)ps.setObject(i+1,params.get(i));
            try(ResultSet rs=ps.executeQuery()){while(rs.next())list.add(mapTransaction(rs));}
        }
        return list;
    }

    public void adjustStock(int warehouseId, int variantId, int quantityChange,
                            String note, int createdBy) throws Exception {
        if (warehouseId <= 0 || variantId <= 0) throw new Exception("Kho hoặc biến thể không hợp lệ.");
        if (quantityChange == 0) throw new Exception("Số lượng điều chỉnh phải khác 0.");
        String type = quantityChange > 0 ? "ADJUST_IN" : "ADJUST_OUT";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL dbo.sp_RecordInventoryTransaction(?, ?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, warehouseId);
            cs.setInt(2, variantId);
            cs.setString(3, type);
            cs.setInt(4, quantityChange);
            cs.setString(5, "MANUAL_ADJUSTMENT");
            cs.setNull(6, java.sql.Types.BIGINT);
            cs.setString(7, note == null || note.isBlank() ? "Điều chỉnh tồn kho thủ công." : note.trim());
            cs.setInt(8, createdBy);
            cs.execute();
        }
    }

    public List<InventoryItem> findInventoryForReport() throws SQLException {
        return findAll();
    }

    private InventoryTransaction mapTransaction(ResultSet rs) throws SQLException {
        InventoryTransaction t=new InventoryTransaction();
        t.setInventoryTransactionId(rs.getLong("InventoryTransactionID"));
        t.setWarehouseId(rs.getInt("WarehouseID")); t.setWarehouseName(rs.getString("WarehouseName"));
        t.setVariantId(rs.getInt("VariantID")); t.setSku(rs.getString("SKU")); t.setProductName(rs.getString("ProductName"));
        t.setVariantName(rs.getString("VariantName")); t.setTransactionType(rs.getString("TransactionType"));
        t.setQuantityChange(rs.getInt("QuantityChange")); t.setQuantityBefore(rs.getInt("QuantityBefore")); t.setQuantityAfter(rs.getInt("QuantityAfter"));
        t.setReferenceType(rs.getString("ReferenceType")); t.setReferenceId(rs.getObject("ReferenceID")!=null?rs.getLong("ReferenceID"):null);
        t.setNote(rs.getString("Note")); t.setCreatedBy(rs.getInt("CreatedBy")); t.setCreatedByName(rs.getString("CreatedByName"));
        if(rs.getTimestamp("CreatedAt")!=null)t.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        return t;
    }

    private InventoryItem mapInventoryItem(
            ResultSet rs
    ) throws SQLException {

        InventoryItem item =
                new InventoryItem();

        item.setWarehouseId(
                rs.getInt("WarehouseID")
        );

        item.setWarehouseName(
                rs.getString("WarehouseName")
        );

        item.setProductId(
                rs.getInt("ProductID")
        );

        item.setProductName(
                rs.getString("ProductName")
        );

        item.setVariantId(
                rs.getInt("VariantID")
        );

        item.setSku(
                rs.getString("SKU")
        );

        item.setVariantName(
                rs.getString("VariantName")
        );

        item.setQuantityOnHand(
                rs.getInt("QuantityOnHand")
        );

        item.setQuantityReserved(
                rs.getInt("QuantityReserved")
        );

        item.setAvailableQuantity(
                rs.getInt("AvailableQuantity")
        );

        item.setReorderLevel(
                rs.getInt("ReorderLevel")
        );

        item.setStockStatus(
                rs.getString("StockStatus")
        );

        return item;
    }
}