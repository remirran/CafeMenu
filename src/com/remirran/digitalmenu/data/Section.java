package com.remirran.digitalmenu.data;

public class Section implements Comparable<Section>{
	protected String tId;
	protected String tName;
	protected String tCategoryId;
	
	public String getId() {
		return tId;
	}
	public String getName() {
		return tName;
	}
	public String getCategoryId() {
		return tCategoryId;
	}
	public void setId(String id) {
		this.tId = id;
	}
	public void setName(String name) {
		this.tName = name;
	}
	public void setCategoryId(String categoryId) {
		this.tCategoryId = categoryId;
	}
	public boolean isRootElement() {
		return (tCategoryId == null) || tCategoryId.isEmpty();
	}
	@Override
	public int compareTo(Section another) {
		return this.tName.compareTo(another.tName);
	}
	@Override
	public String toString() {
		return tName;
	}
}
