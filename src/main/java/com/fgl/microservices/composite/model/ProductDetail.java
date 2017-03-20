package com.fgl.microservices.composite.model;

public class ProductDetail {
private String id;
private String name;
private String description;
private int originalPrice;
private int offerPrice;
public ProductDetail(){}
public ProductDetail(String id, String name, String description, int originalPrice, int offerPrice) {
	super();
	this.id = id;
	this.name = name;
	this.description = description;
	this.originalPrice = originalPrice;
	this.offerPrice = offerPrice;
}
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public int getOriginalPrice() {
	return originalPrice;
}
public void setOriginalPrice(int originalPrice) {
	this.originalPrice = originalPrice;
}
public int getOfferPrice() {
	return offerPrice;
}
public void setOfferPrice(int offerPrice) {
	this.offerPrice = offerPrice;
}

}
