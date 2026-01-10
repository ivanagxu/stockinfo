package com.ivan.mfutu.entity;

import java.util.Date;

public class FutuData {
	private Integer id;
	private String code;
	private String name;
	private String market;
	private String level;
	private String currency;
	private Float price;
	private Float myAvgPrice;
	private Integer quantity;
	private Float percentage;
	private String industry;
	private String subIndustry;
	private String tags;
	private Date updateTime;
	private Float value;
	private Float plValue;
	private Float plRatio;
	private String plDate;
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
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Float getPrice() {
		return price;
	}
	public void setPrice(Float price) {
		this.price = price;
	}
	public Float getMyAvgPrice() {
		return myAvgPrice;
	}
	public void setMyAvgPrice(Float myAvgPrice) {
		this.myAvgPrice = myAvgPrice;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Float getPercentage() {
		return percentage;
	}
	public void setPercentage(Float percentage) {
		this.percentage = percentage;
	}
	public String getIndustry() {
		return industry;
	}
	public void setIndustry(String industry) {
		this.industry = industry;
	}
	public String getSubIndustry() {
		return subIndustry;
	}
	public void setSubIndustry(String subIndustry) {
		this.subIndustry = subIndustry;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	public Float getPlValue() {
		return plValue;
	}
	public void setPlValue(Float plValue) {
		this.plValue = plValue;
	}
	public Float getPlRatio() {
		return plRatio;
	}
	public void setPlRatio(Float plRatio) {
		this.plRatio = plRatio;
	}
	
	public String getPlDate() {
		return plDate;
	}
	public void setPlDate(String plDate) {
		this.plDate = plDate;
	}
	@Override
	public String toString() {
		return "id=" + id + ", code=" + code;
	}
}
