package ru.dmenu.data;

public class Section implements Comparable<Section>{
	private String tId;
	private String tName;
	private String tCategoryId;
	private String tImgUri;
	
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
	public String getImgUri() {
		return tImgUri;
	}
	public void setImgUri(String tImgUri) {
		this.tImgUri = tImgUri;
	}
	@Override
	public int compareTo(Section another) {
		return this.tName.compareTo(another.tName);
	}
	@Override
	public String toString() {
		return tName;
	}
	@Override
	public boolean equals(Object o) {
		return tId.equals(((Section)o).tId) && tName.equals(((Section)o).tName);
	}
	@Override
	public int hashCode() {
		return tId.hashCode() ^ tName.hashCode();
	}
}
