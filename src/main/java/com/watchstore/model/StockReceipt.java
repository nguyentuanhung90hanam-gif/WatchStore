package com.watchstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model ánh xạ bảng dbo.StockReceipts.
 *
 * Cột: StockReceiptID, ReceiptCode, WarehouseID, SupplierName, SupplierPhone,
 *      ReceiptDate, Status, TotalCost, Note, CreatedBy, ApprovedBy, ApprovedAt
 * Thêm: WarehouseName, CreatedByName từ JOIN (tiện hiển thị JSP)
 */
public class StockReceipt {

    private long stockReceiptId;
    private String receiptCode;
    private int warehouseId;
    private String warehouseName;       // JOIN từ Warehouses
    private String supplierName;
    private String supplierPhone;
    private LocalDateTime receiptDate;
    private String status;              // DRAFT | PENDING | COMPLETED | CANCELLED
    private BigDecimal totalCost;
    private String note;
    private int createdBy;
    private String createdByName;       // JOIN từ Users
    private Integer approvedBy;
    private LocalDateTime approvedAt;
    private List<StockReceiptItem> items = new ArrayList<>();

    public StockReceipt() {}

    public long getStockReceiptId()               { return stockReceiptId; }
    public void setStockReceiptId(long v)         { this.stockReceiptId = v; }

    public String getReceiptCode()                { return receiptCode; }
    public void setReceiptCode(String v)          { this.receiptCode = v; }

    public int getWarehouseId()                   { return warehouseId; }
    public void setWarehouseId(int v)             { this.warehouseId = v; }

    public String getWarehouseName()              { return warehouseName; }
    public void setWarehouseName(String v)        { this.warehouseName = v; }

    public String getSupplierName()               { return supplierName; }
    public void setSupplierName(String v)         { this.supplierName = v; }

    public String getSupplierPhone()              { return supplierPhone; }
    public void setSupplierPhone(String v)        { this.supplierPhone = v; }

    public LocalDateTime getReceiptDate()         { return receiptDate; }
    public void setReceiptDate(LocalDateTime v)   { this.receiptDate = v; }

    public String getStatus()                     { return status; }
    public void setStatus(String v)               { this.status = v; }

    public BigDecimal getTotalCost()              { return totalCost; }
    public void setTotalCost(BigDecimal v)        { this.totalCost = v; }

    public String getNote()                       { return note; }
    public void setNote(String v)                 { this.note = v; }

    public int getCreatedBy()                     { return createdBy; }
    public void setCreatedBy(int v)               { this.createdBy = v; }

    public String getCreatedByName()              { return createdByName; }
    public void setCreatedByName(String v)        { this.createdByName = v; }

    public Integer getApprovedBy()                { return approvedBy; }
    public void setApprovedBy(Integer v)          { this.approvedBy = v; }

    public LocalDateTime getApprovedAt()          { return approvedAt; }
    public void setApprovedAt(LocalDateTime v)    { this.approvedAt = v; }

    public List<StockReceiptItem> getItems()      { return items; }
    public void setItems(List<StockReceiptItem> v){ this.items = v; }
}
