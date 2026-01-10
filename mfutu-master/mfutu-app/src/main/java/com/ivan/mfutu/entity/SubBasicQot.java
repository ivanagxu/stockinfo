package com.ivan.mfutu.entity;

import java.util.Date;

public class SubBasicQot {
    private String code;
    private Integer market;
    private String name;
    private Float curPrice;
    private Float lastClosePrice;
    private Float increaseRate;
    private Date updateTime;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getMarket() {
        return market;
    }

    public void setMarket(Integer market) {
        this.market = market;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getCurPrice() {
        return curPrice;
    }

    public void setCurPrice(Float curPrice) {
        this.curPrice = curPrice;
    }

    public Float getLastClosePrice() {
        return lastClosePrice;
    }

    public void setLastClosePrice(Float lastClosePrice) {
        this.lastClosePrice = lastClosePrice;
    }

    public Float getIncreaseRate() {
        return increaseRate;
    }

    public void setIncreaseRate(Float increaseRate) {
        this.increaseRate = increaseRate;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "code=" + code + ", name=" + name;
    }
}
