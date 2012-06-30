package com.remirran.digitalmenu.data;

public class Dish extends Section {
	private String tDesc;
	private Integer tPrice;
	
	private boolean tAvailFlag;
	
	public String getDesc() {
		return tDesc;
	}
	public void setDesc(String tDesc) {
		this.tDesc = tDesc;
	}
	public Integer getPrice() {
		return tPrice;
	}
	public void setPrice(int tPrice) {
		this.tPrice = tPrice;
	}
	
	public void setAvailFlag(boolean tAvailFlag) {
		this.tAvailFlag = tAvailFlag;
	}
	public boolean isAvailable() {
		return tAvailFlag;
	}
}
