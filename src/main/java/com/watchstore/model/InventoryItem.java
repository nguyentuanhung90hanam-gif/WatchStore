package com.watchstore.model;

/**
 * Model cho một dòng tồn kho (bảng InventoryBalances join ProductVariants/Products).
 * Mỗi dòng = một biến thể sản phẩm trong một kho.
 */
public class InventoryItem {

    private String productName;       // Tên sản phẩm (từ Products)
    private String productCode;       // Mã sản phẩm
    private String variantSku;        // SKU biến thể (từ ProductVariants)
    private String variantName;       // Tên biến thể, ví dụ "Đen / Dây da"
    private int    quantityOnHand;    // Số lượng thực tế trong kho
    private int    quantityReserved;  // Số lượng đang giữ cho đơn hàng chưa giao
    private int    availableQuantity; // = OnHand - Reserved (cột computed trong CSDL)
    private int    reorderLevel;      // Ngưỡng cảnh báo sắp hết

    // === Getter / Setter ===

    public String getProductName()                     { return productName; }
    public void setProductName(String productName)     { this.productName = productName; }

    public String getProductCode()                     { return productCode; }
    public void setProductCode(String productCode)     { this.productCode = productCode; }

    public String getVariantSku()                    { return variantSku; }
    public void setVariantSku(String variantSku)     { this.variantSku = variantSku; }

    public String getVariantName()                     { return variantName; }
    public void setVariantName(String variantName)     { this.variantName = variantName; }

    public int getQuantityOnHand()                       { return quantityOnHand; }
    public void setQuantityOnHand(int quantityOnHand)    { this.quantityOnHand = quantityOnHand; }

    public int getQuantityReserved()                         { return quantityReserved; }
    public void setQuantityReserved(int quantityReserved)    { this.quantityReserved = quantityReserved; }

    public int getAvailableQuantity()                          { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity)    { this.availableQuantity = availableQuantity; }

    public int getReorderLevel()                       { return reorderLevel; }
    public void setReorderLevel(int reorderLevel)      { this.reorderLevel = reorderLevel; }

    /**
     * Trả về true nếu tồn kho dưới ngưỡng cảnh báo.
     * JSP dùng: ${item.lowStock ? 'warning' : 'success'}
     */
    public boolean isLowStock() {
        return availableQuantity <= reorderLevel;
    }
}
