package com.watchstore.model;

import java.math.BigDecimal;

/**
 * Model ánh xạ bảng dbo.StockReceiptItems.
 *
 * Cột: StockReceiptItemID, StockReceiptID, VariantID, Quantity, UnitCost,
 *      LineTotal (computed = Quantity * UnitCost)
 * Thêm: VariantName, SKU từ JOIN ProductVariants (tiện hiển thị JSP)
 */
public class StockReceiptItem {

    private long stockReceiptItemId;
    private long stockReceiptId;
    private int variantId;
    private String productName;     // JOIN từ Products
    private String variantName;     // JOIN từ ProductVariants
    private String sku;             // JOIN từ ProductVariants
    private int quantity;
    private BigDecimal unitCost;
    private BigDecimal lineTotal;   // computed: Quantity * UnitCost

    public StockReceiptItem() {}

    public long getStockReceiptItemId()           { return stockReceiptItemId; }
    public void setStockReceiptItemId(long v)     { this.stockReceiptItemId = v; }

    public long getStockReceiptId()               { return stockReceiptId; }
    public void setStockReceiptId(long v)         { this.stockReceiptId = v; }

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

    public BigDecimal getUnitCost()               { return unitCost; }
    public void setUnitCost(BigDecimal v)         { this.unitCost = v; }

    public BigDecimal getLineTotal()              { return lineTotal; }
    public void setLineTotal(BigDecimal v)        { this.lineTotal = v; }
}
