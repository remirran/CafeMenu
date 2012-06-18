package com.remirran.digitalmenu.data;

public class Dish extends Section {
	private String tDesc;
	private String tPrice;
	
	private boolean tAvailFlag;
	
	public String getDesc() {
		return tDesc;
	}
	public void setDesc(String tDesc) {
		this.tDesc = tDesc;
	}
	public String getPrice() {
		return tPrice;
	}
	public void setPrice(String tPrice) {
		this.tPrice = tPrice;
	}
	
	public void setAvailFlag(boolean tAvailFlag) {
		this.tAvailFlag = tAvailFlag;
	}
	public boolean isAvailable() {
		return tAvailFlag;
	}
}
