package com.watchstore.model;

/**
 * Model ánh xạ View dbo.vw_InventoryOverview.
 *
 * Cột view:
 *   WarehouseID, WarehouseName,
 *   ProductID, ProductName,
 *   VariantID, SKU, VariantName,
 *   QuantityOnHand, QuantityReserved, AvailableQuantity (computed), ReorderLevel,
 *   StockStatus (computed: HẾT HÀNG / CẢNH BÁO SẮP HẾT / AN TOÀN)
 */
public class InventoryItem {

    private int warehouseId;
    private String warehouseName;
    private int productId;
    private String productName;
    private int variantId;
    private String sku;
    private String variantName;
    private int quantityOnHand;
    private int quantityReserved;
    private int availableQuantity;
    private int reorderLevel;
    private String stockStatus;

    public InventoryItem() {}

    public int getWarehouseId()                   { return warehouseId; }
    public void setWarehouseId(int v)             { this.warehouseId = v; }

    public String getWarehouseName()              { return warehouseName; }
    public void setWarehouseName(String v)        { this.warehouseName = v; }

    public int getProductId()                     { return productId; }
    public void setProductId(int v)               { this.productId = v; }

    public String getProductName()                { return productName; }
    public void setProductName(String v)          { this.productName = v; }

    public int getVariantId()                     { return variantId; }
    public void setVariantId(int v)               { this.variantId = v; }

    public String getSku()                        { return sku; }
    public void setSku(String v)                  { this.sku = v; }

    public String getVariantName()                { return variantName; }
    public void setVariantName(String v)          { this.variantName = v; }

    public int getQuantityOnHand()                { return quantityOnHand; }
    public void setQuantityOnHand(int v)          { this.quantityOnHand = v; }

    public int getQuantityReserved()              { return quantityReserved; }
    public void setQuantityReserved(int v)        { this.quantityReserved = v; }

    public int getAvailableQuantity()             { return availableQuantity; }
    public void setAvailableQuantity(int v)       { this.availableQuantity = v; }

    public int getReorderLevel()                  { return reorderLevel; }
    public void setReorderLevel(int v)            { this.reorderLevel = v; }

    public String getStockStatus()                { return stockStatus; }
    public void setStockStatus(String v)          { this.stockStatus = v; }
}
