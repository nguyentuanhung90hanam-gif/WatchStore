package com.watchstore.model;

import java.time.LocalDateTime;

/**
 * Model cho Phiếu xuất kho (bảng StockExports trong CSDL).
 * Phiếu xuất ghi lại hàng rời khỏi kho: xuất theo đơn hàng, hỏng, điều chuyển...
 */
public class StockExport {

    private long   exportID;       // ID tự tăng
    private String exportCode;     // Mã phiếu, ví dụ: PX-20260801-001
    private String exportType;     // SALE | TRANSFER | DAMAGED | OTHER
    private String exportDate;     // Ngày xuất (text)
    private String status;         // DRAFT | PENDING | COMPLETED | CANCELLED
    private String receiverName;   // Người/bộ phận nhận hàng
    private String note;           // Ghi chú
    private String createdByName;  // Tên người tạo phiếu (join Users)
    private Long   orderID;        // Liên kết đơn hàng (có thể null nếu không phải xuất theo đơn)

    // === Getter / Setter ===

    public long getExportID()                   { return exportID; }
    public void setExportID(long exportID)      { this.exportID = exportID; }

    public String getExportCode()                   { return exportCode; }
    public void setExportCode(String exportCode)    { this.exportCode = exportCode; }

    public String getExportType()                   { return exportType; }
    public void setExportType(String exportType)    { this.exportType = exportType; }

    public String getExportDate()                   { return exportDate; }
    public void setExportDate(String exportDate)    { this.exportDate = exportDate; }

    public String getStatus()                { return status; }
    public void setStatus(String status)     { this.status = status; }

    public String getReceiverName()                      { return receiverName; }
    public void setReceiverName(String receiverName)     { this.receiverName = receiverName; }

    public String getNote()                { return note; }
    public void setNote(String note)       { this.note = note; }

    public String getCreatedByName()                      { return createdByName; }
    public void setCreatedByName(String createdByName)    { this.createdByName = createdByName; }

    public Long getOrderID()               { return orderID; }
    public void setOrderID(Long orderID)   { this.orderID = orderID; }

    /**
     * Loại xuất tiếng Việt.
     */
    public String getExportTypeLabel() {
        switch (exportType == null ? "" : exportType) {
            case "SALE":      return "Xuất theo đơn hàng";
            case "TRANSFER":  return "Điều chuyển";
            case "DAMAGED":   return "Hỏng / Thanh lý";
            case "OTHER":     return "Khác";
            default:          return exportType;
        }
    }

    /**
     * CSS class cho badge trạng thái.
     */
    public String getStatusClass() {
        switch (status == null ? "" : status) {
            case "COMPLETED": return "success";
            case "PENDING":   return "warning";
            case "CANCELLED": return "danger";
            default:          return "neutral";
        }
    }

    public String getStatusLabel() {
        switch (status == null ? "" : status) {
            case "DRAFT":      return "Nháp";
            case "PENDING":    return "Chờ duyệt";
            case "COMPLETED":  return "Hoàn thành";
            case "CANCELLED":  return "Đã huỷ";
            default:           return status;
        }
    }
}
