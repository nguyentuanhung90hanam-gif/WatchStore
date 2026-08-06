package com.watchstore.model;

import java.math.BigDecimal;


public class Variant {

    private long   variantID;       // ID tự tăng
    private String sku;             // Mã SKU, ví dụ: SKU-EFR-108-SL
    private String variantName;     // Tên biến thể, ví dụ: "Mặt Xanh / Dây thép"
    private String color;           // Màu sắc
    private String material;        // Chất liệu dây
    private BigDecimal price;       // Giá bán
    private int    stockQty;        // Số lượng tồn kho
    private String status;          // ACTIVE | INACTIVE
    private String productName;     // Tên sản phẩm cha (join từ Products)
    private String productCode;     // Mã sản phẩm cha

    // === Getter / Setter ===

    public long getVariantID()                     { return variantID; }
    public void setVariantID(long variantID)       { this.variantID = variantID; }

    public String getSku()                         { return sku; }
    public void   setSku(String sku)               { this.sku = sku; }

    public String getVariantName()                 { return variantName; }
    public void   setVariantName(String variantName) { this.variantName = variantName; }

    public String getColor()                       { return color; }
    public void   setColor(String color)           { this.color = color; }

    public String getMaterial()                    { return material; }
    public void   setMaterial(String material)     { this.material = material; }

    public BigDecimal getPrice()                   { return price; }
    public void       setPrice(BigDecimal price)   { this.price = price; }

    public int  getStockQty()                      { return stockQty; }
    public void setStockQty(int stockQty)          { this.stockQty = stockQty; }

    public String getStatus()                      { return status; }
    public void   setStatus(String status)         { this.status = status; }

    public String getProductName()                 { return productName; }
    public void   setProductName(String productName) { this.productName = productName; }

    public String getProductCode()                 { return productCode; }
    public void   setProductCode(String productCode) { this.productCode = productCode; }

    /**
     * Trả về nhãn trạng thái tiếng Việt.
     */
    public String getStatusLabel() {
        if ("ACTIVE".equals(status))   return "Đang bán";
        if ("INACTIVE".equals(status)) return "Ngừng bán";
        return status;
    }

    /**
     * Trả về CSS class để tô màu badge.
     */
    public String getStatusClass() {
        if ("ACTIVE".equals(status))   return "success";
        if ("INACTIVE".equals(status)) return "neutral";
        return "neutral";
    }
}
