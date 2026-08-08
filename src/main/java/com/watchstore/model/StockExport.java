package com.watchstore.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model ánh xạ bảng dbo.StockExports.
 *
 * Cột: StockExportID, ExportCode, WarehouseID, OrderID (nullable), ExportType,
 *      ExportDate, Status, ReceiverName, Note, CreatedBy, ApprovedBy, ApprovedAt
 * ExportType IN ('SALE','TRANSFER','DAMAGED','OTHER')
 * Status     IN ('DRAFT','PENDING','COMPLETED','CANCELLED')
 * Thêm: WarehouseName, CreatedByName từ JOIN
 */
public class StockExport {

    private long stockExportId;
    private String exportCode;
    private int warehouseId;
    private String warehouseName;       // JOIN từ Warehouses
    private Long orderId;               // nullable FK → Orders
    private String exportType;          // SALE | TRANSFER | DAMAGED | OTHER
    private LocalDateTime exportDate;
    private String status;              // DRAFT | PENDING | COMPLETED | CANCELLED
    private String receiverName;
    private String note;
    private int createdBy;
    private String createdByName;       // JOIN từ Users
    private Integer approvedBy;
    private LocalDateTime approvedAt;
    private List<StockExportItem> items = new ArrayList<>();

    public StockExport() {}

    public long getStockExportId()                { return stockExportId; }
    public void setStockExportId(long v)          { this.stockExportId = v; }

    public String getExportCode()                 { return exportCode; }
    public void setExportCode(String v)           { this.exportCode = v; }

    public int getWarehouseId()                   { return warehouseId; }
    public void setWarehouseId(int v)             { this.warehouseId = v; }

    public String getWarehouseName()              { return warehouseName; }
    public void setWarehouseName(String v)        { this.warehouseName = v; }

    public Long getOrderId()                      { return orderId; }
    public void setOrderId(Long v)                { this.orderId = v; }

    public String getExportType()                 { return exportType; }
    public void setExportType(String v)           { this.exportType = v; }

    public LocalDateTime getExportDate()          { return exportDate; }
    public void setExportDate(LocalDateTime v)    { this.exportDate = v; }

    public String getStatus()                     { return status; }
    public void setStatus(String v)               { this.status = v; }

    public String getReceiverName()               { return receiverName; }
    public void setReceiverName(String v)         { this.receiverName = v; }

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

    public List<StockExportItem> getItems()       { return items; }
    public void setItems(List<StockExportItem> v) { this.items = v; }
}
