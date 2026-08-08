package com.watchstore.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model ánh xạ bảng dbo.Stocktakes.
 *
 * Cột: StocktakeID, StocktakeCode, WarehouseID, StocktakeDate, Status, Note, CreatedBy, ApprovedBy, ApprovedAt
 * Status IN ('DRAFT','COUNTING','COMPLETED','CANCELLED')
 * Thêm: WarehouseName, CreatedByName từ JOIN
 */
public class Stocktake {

    private long stocktakeId;
    private String stocktakeCode;
    private int warehouseId;
    private String warehouseName;       // JOIN từ Warehouses
    private LocalDateTime stocktakeDate;
    private String status;              // DRAFT | COUNTING | COMPLETED | CANCELLED
    private String note;
    private int createdBy;
    private String createdByName;       // JOIN từ Users
    private Integer approvedBy;
    private LocalDateTime approvedAt;
    private List<StocktakeItem> items = new ArrayList<>();

    public Stocktake() {}

    public long getStocktakeId()                  { return stocktakeId; }
    public void setStocktakeId(long v)            { this.stocktakeId = v; }

    public String getStocktakeCode()              { return stocktakeCode; }
    public void setStocktakeCode(String v)        { this.stocktakeCode = v; }

    public int getWarehouseId()                   { return warehouseId; }
    public void setWarehouseId(int v)             { this.warehouseId = v; }

    public String getWarehouseName()              { return warehouseName; }
    public void setWarehouseName(String v)        { this.warehouseName = v; }

    public LocalDateTime getStocktakeDate()       { return stocktakeDate; }
    public void setStocktakeDate(LocalDateTime v) { this.stocktakeDate = v; }

    public String getStatus()                     { return status; }
    public void setStatus(String v)               { this.status = v; }

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

    public List<StocktakeItem> getItems()         { return items; }
    public void setItems(List<StocktakeItem> v)   { this.items = v; }
}
