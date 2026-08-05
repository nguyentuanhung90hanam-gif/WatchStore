package com.watchstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model cho Phiếu nhập kho (bảng StockReceipts trong CSDL).
 * Mỗi phiếu nhập ghi lại một lần hàng về từ nhà cung cấp.
 */
public class StockReceipt {

    private long   receiptID;       // ID tự tăng trong CSDL
    private String receiptCode;     // Mã phiếu, ví dụ: PN-20260801-001
    private String supplierName;    // Tên nhà cung cấp
    private String supplierPhone;   // Số điện thoại nhà cung cấp
    private String receiptDate;     // Ngày lập phiếu (dạng text để dễ hiển thị)
    private String status;          // DRAFT | PENDING | COMPLETED | CANCELLED
    private BigDecimal totalCost;   // Tổng tiền nhập
    private String note;            // Ghi chú
    private String createdByName;   // Tên người tạo phiếu (join từ Users)

    // === Getter / Setter ===

    public long getReceiptID()                      { return receiptID; }
    public void setReceiptID(long receiptID)        { this.receiptID = receiptID; }

    public String getReceiptCode()                  { return receiptCode; }
    public void setReceiptCode(String receiptCode)  { this.receiptCode = receiptCode; }

    public String getSupplierName()                  { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierPhone()                   { return supplierPhone; }
    public void setSupplierPhone(String supplierPhone) { this.supplierPhone = supplierPhone; }

    public String getReceiptDate()                    { return receiptDate; }
    public void setReceiptDate(String receiptDate)    { this.receiptDate = receiptDate; }

    public String getStatus()                { return status; }
    public void setStatus(String status)     { this.status = status; }

    public BigDecimal getTotalCost()                   { return totalCost; }
    public void setTotalCost(BigDecimal totalCost)     { this.totalCost = totalCost; }

    public String getNote()                { return note; }
    public void setNote(String note)       { this.note = note; }

    public String getCreatedByName()                      { return createdByName; }
    public void setCreatedByName(String createdByName)    { this.createdByName = createdByName; }

    /**
     * Trả về nhãn trạng thái tiếng Việt để hiển thị trên JSP.
     */
    public String getStatusLabel() {
        switch (status == null ? "" : status) {
            case "DRAFT":      return "Nháp";
            case "PENDING":    return "Chờ duyệt";
            case "COMPLETED":  return "Hoàn thành";
            case "CANCELLED":  return "Đã huỷ";
            default:           return status;
        }
    }

    /**
     * Trả về CSS class để tô màu badge trạng thái.
     */
    public String getStatusClass() {
        switch (status == null ? "" : status) {
            case "COMPLETED": return "success";
            case "PENDING":   return "warning";
            case "CANCELLED": return "danger";
            default:          return "neutral";
        }
    }
}
