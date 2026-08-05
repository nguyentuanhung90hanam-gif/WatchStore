package com.watchstore.model;

/**
 * Model cho Phiếu kiểm kê (bảng Stocktakes trong CSDL).
 * Kiểm kê là đếm lại hàng thực tế trong kho rồi so sánh với số hệ thống.
 */
public class StocktakeRecord {

    private long   stocktakeID;    // ID tự tăng
    private String stocktakeCode;  // Mã kiểm kê, ví dụ: KK-20260801-001
    private String stocktakeDate;  // Ngày kiểm kê (text)
    private String status;         // DRAFT | COUNTING | COMPLETED | CANCELLED
    private String note;           // Ghi chú
    private String createdByName;  // Tên người tạo (join Users)

    // === Getter / Setter ===

    public long getStocktakeID()                    { return stocktakeID; }
    public void setStocktakeID(long stocktakeID)    { this.stocktakeID = stocktakeID; }

    public String getStocktakeCode()                     { return stocktakeCode; }
    public void setStocktakeCode(String stocktakeCode)   { this.stocktakeCode = stocktakeCode; }

    public String getStocktakeDate()                     { return stocktakeDate; }
    public void setStocktakeDate(String stocktakeDate)   { this.stocktakeDate = stocktakeDate; }

    public String getStatus()                { return status; }
    public void setStatus(String status)     { this.status = status; }

    public String getNote()                { return note; }
    public void setNote(String note)       { this.note = note; }

    public String getCreatedByName()                      { return createdByName; }
    public void setCreatedByName(String createdByName)    { this.createdByName = createdByName; }

    public String getStatusLabel() {
        switch (status == null ? "" : status) {
            case "DRAFT":      return "Nháp";
            case "COUNTING":   return "Đang đếm";
            case "COMPLETED":  return "Hoàn thành";
            case "CANCELLED":  return "Đã huỷ";
            default:           return status;
        }
    }

    public String getStatusClass() {
        switch (status == null ? "" : status) {
            case "COMPLETED": return "success";
            case "COUNTING":  return "info";
            case "CANCELLED": return "danger";
            default:          return "neutral";
        }
    }
}
