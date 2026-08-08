package com.watchstore.model;

import java.math.BigDecimal;

/**
 * Model ánh xạ bảng dbo.ProductVariants + JOIN Products, Brands, Attributes.
 *
 * Lưu ý: ProductVariants KHÔNG có cột Color hay Size.
 * Thuộc tính màu sắc, kích thước được lấy thông qua:
 *   ProductVariants → VariantAttributeValues → ProductAttributeValues → ProductAttributes
 * Tổng hợp thành chuỗi attributes (VD: "Màu sắc: Xanh | Dây: Da nâu").
 */
public class Variant {

    private int variantId;
    private int productId;
    private String productName;
    private String brandName;
    private String sku;
    private String barcode;
    private String variantName;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private BigDecimal compareAtPrice;
    private Integer weightGram;
    private String status;
    // Chuỗi tổng hợp các attribute từ JOIN, VD: "Màu: Xanh | Dây: Da"
    private String attributes;

    public Variant() {}

    public int getVariantId()                     { return variantId; }
    public void setVariantId(int v)               { this.variantId = v; }

    public int getProductId()                     { return productId; }
    public void setProductId(int v)               { this.productId = v; }

    public String getProductName()                { return productName; }
    public void setProductName(String v)          { this.productName = v; }

    public String getBrandName()                  { return brandName; }
    public void setBrandName(String v)            { this.brandName = v; }

    public String getSku()                        { return sku; }
    public void setSku(String v)                  { this.sku = v; }

    public String getBarcode()                    { return barcode; }
    public void setBarcode(String v)              { this.barcode = v; }

    public String getVariantName()                { return variantName; }
    public void setVariantName(String v)          { this.variantName = v; }

    public BigDecimal getCostPrice()              { return costPrice; }
    public void setCostPrice(BigDecimal v)        { this.costPrice = v; }

    public BigDecimal getSalePrice()              { return salePrice; }
    public void setSalePrice(BigDecimal v)        { this.salePrice = v; }

    public BigDecimal getCompareAtPrice()         { return compareAtPrice; }
    public void setCompareAtPrice(BigDecimal v)   { this.compareAtPrice = v; }

    public Integer getWeightGram()                { return weightGram; }
    public void setWeightGram(Integer v)          { this.weightGram = v; }

    public String getStatus()                     { return status; }
    public void setStatus(String v)               { this.status = v; }

    public String getAttributes()                 { return attributes; }
    public void setAttributes(String v)           { this.attributes = v; }
}
