package com.watchstore.model;

/**
 * Model ánh xạ bảng dbo.StocktakeItems.
 *
 * Cột: StocktakeItemID, StocktakeID, VariantID, SystemQuantity, ActualQuantity, DifferenceQuantity, Note
 * Thêm: VariantName, SKU từ JOIN ProductVariants (tiện hiển thị JSP)
 */
public class StocktakeItem {

    private long stocktakeItemId;
    private long stocktakeId;
    private int variantId;
    private String variantName;     // JOIN từ ProductVariants
    private String sku;             // JOIN từ ProductVariants
    private int systemQuantity;
    private int actualQuantity;
    private int differenceQuantity; // computed: ActualQuantity - SystemQuantity
    private String note;

    public StocktakeItem() {}

    public long getStocktakeItemId()              { return stocktakeItemId; }
    public void setStocktakeItemId(long v)        { this.stocktakeItemId = v; }

    public long getStocktakeId()                  { return stocktakeId; }
    public void setStocktakeId(long v)            { this.stocktakeId = v; }

    public int getVariantId()                     { return variantId; }
    public void setVariantId(int v)               { this.variantId = v; }

    public String getVariantName()                { return variantName; }
    public void setVariantName(String v)          { this.variantName = v; }

    public String getSku()                        { return sku; }
    public void setSku(String v)                  { this.sku = v; }

    public int getSystemQuantity()                { return systemQuantity; }
    public void setSystemQuantity(int v)          { this.systemQuantity = v; }

    public int getActualQuantity()                { return actualQuantity; }
    public void setActualQuantity(int v)          { this.actualQuantity = v; }

    public int getDifferenceQuantity()            { return differenceQuantity; }
    public void setDifferenceQuantity(int v)      { this.differenceQuantity = v; }

    public String getNote()                       { return note; }
    public void setNote(String v)                 { this.note = v; }
}
