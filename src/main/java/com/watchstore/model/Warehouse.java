package com.watchstore.model;

/**
 * The model maps the dbo.Warehouses table.
 *
 * Columns: WarehouseID, WarehouseCode, WarehouseName, Address, ManagerID, Status
 */
public class Warehouse {

    private int warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String address;
    private int managerId;
    private String status;

    public Warehouse() {
    }

    public Warehouse(int warehouseId, String warehouseCode, String warehouseName,
                     String address, Integer managerId, String status) {
        this.warehouseId = warehouseId;
        this.warehouseCode = warehouseCode;
        this.warehouseName = warehouseName;
        this.address = address;
        this.managerId = managerId;
        this.status = status;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int v) {
        this.warehouseId = v;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String v) {
        this.warehouseCode = v;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String v) {
        this.warehouseName = v;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String v) {
        this.address = v;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer v) {
        this.managerId = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }
}

