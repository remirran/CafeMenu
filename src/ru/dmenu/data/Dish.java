package ru.dmenu.data;

public class Dish extends Section {
	private String tDesc;
	private Float tPrice;
	
	private boolean tAvailFlag;
	
	public String getDesc() {
		return tDesc;
	}
	public void setDesc(String tDesc) {
		this.tDesc = tDesc;
	}
	public Float getPrice() {
		return tPrice;
	}
	public void setPrice(float tPrice) {
		this.tPrice = tPrice;
	}
	
	public void setAvailFlag(boolean tAvailFlag) {
		this.tAvailFlag = tAvailFlag;
	}
	public boolean isAvailable() {
		return tAvailFlag;
	}
}
