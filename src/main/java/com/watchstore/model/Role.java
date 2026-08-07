package com.watchstore.model;

public class Role {

    private Integer id;
    private String code;
    private String name;
    private Integer userCount;
    private Boolean status;

    public Role() {
    }

    public Role(Integer id,
                String code,
                String name,
                Integer userCount,
                Boolean status) {

        this.id = id;
        this.code = code;
        this.name = name;
        this.userCount = userCount;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}