package com.ivan.mfutu.entity;

import java.util.Date;

public class KlineData {
	private Long id;
	private String code;
	private String timeKey;
	private double open;
	private double close;
	private double high;
	private double low;
	private double preRatio;
	private double turnover_rate;
	private int volumn;
	private double turnover;
	private double changeRate;
	private double lastClose;
	private Date updateTime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTimeKey() {
		return timeKey;
	}
	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getPreRatio() {
		return preRatio;
	}
	public void setPreRatio(double preRatio) {
		this.preRatio = preRatio;
	}
	public double getTurnover_rate() {
		return turnover_rate;
	}
	public void setTurnover_rate(double turnover_rate) {
		this.turnover_rate = turnover_rate;
	}
	public int getVolumn() {
		return volumn;
	}
	public void setVolumn(int volumn) {
		this.volumn = volumn;
	}
	public double getTurnover() {
		return turnover;
	}
	public void setTurnover(double turnover) {
		this.turnover = turnover;
	}
	public double getChangeRate() {
		return changeRate;
	}
	public void setChangeRate(double changeRate) {
		this.changeRate = changeRate;
	}
	public double getLastClose() {
		return lastClose;
	}
	public void setLastClose(double lastClose) {
		this.lastClose = lastClose;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	
}
