package com.watchstore.model;

/**
 * Model ánh xạ bảng dbo.StockExportItems.
 *
 * Cột: StockExportItemID, StockExportID, VariantID, Quantity
 * Thêm: VariantName, SKU từ JOIN ProductVariants (tiện hiển thị JSP)
 */
public class StockExportItem {

    private long stockExportItemId;
    private long stockExportId;
    private int variantId;
    private String productName;     // JOIN từ Products
    private String variantName;     // JOIN từ ProductVariants
    private String sku;             // JOIN từ ProductVariants
    private int quantity;

    public StockExportItem() {}

    public long getStockExportItemId()            { return stockExportItemId; }
    public void setStockExportItemId(long v)      { this.stockExportItemId = v; }

    public long getStockExportId()                { return stockExportId; }
    public void setStockExportId(long v)          { this.stockExportId = v; }

    public int getVariantId()                     { return variantId; }
    public void setVariantId(int v)               { this.variantId = v; }

    public String getProductName()                { return productName; }
    public void setProductName(String v)          { this.productName = v; }

    public String getVariantName()                { return variantName; }
    public void setVariantName(String v)          { this.variantName = v; }

    public String getSku()                        { return sku; }
    public void setSku(String v)                  { this.sku = v; }

    public int getQuantity()                      { return quantity; }
    public void setQuantity(int v)                { this.quantity = v; }
}
