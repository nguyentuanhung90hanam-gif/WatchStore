package com.watchstore.model;

public class DashboardDTO {

    private int totalProducts;
    private int totalVariants;
    private int totalInventory;
    private int todayReceipts;
    private int todayExports;
    private int lowStockProducts;

    public DashboardDTO() {
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalVariants() {
        return totalVariants;
    }

    public void setTotalVariants(int totalVariants) {
        this.totalVariants = totalVariants;
    }

    public int getTotalInventory() {
        return totalInventory;
    }

    public void setTotalInventory(int totalInventory) {
        this.totalInventory = totalInventory;
    }

    public int getTodayReceipts() {
        return todayReceipts;
    }

    public void setTodayReceipts(int todayReceipts) {
        this.todayReceipts = todayReceipts;
    }

    public int getTodayExports() {
        return todayExports;
    }

    public void setTodayExports(int todayExports) {
        this.todayExports = todayExports;
    }

    public int getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(int lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

}
