package com.besmartexim.dto.response;

public class ListCities {
	
	private String city_name;
	private Double value_inr;
	private Double value_usd;
	private Long quantity;
	private Double share;
	private Long shipment_count;
	public String getCity_name() {
		return city_name;
	}
	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}
	public Double getValue_inr() {
		return value_inr;
	}
	public void setValue_inr(Double value_inr) {
		this.value_inr = value_inr;
	}
	public Double getValue_usd() {
		return value_usd;
	}
	public void setValue_usd(Double value_usd) {
		this.value_usd = value_usd;
	}
	public Long getQuantity() {
		return quantity;
	}
	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}
	public Double getShare() {
		return share;
	}
	public void setShare(Double share) {
		this.share = share;
	}
	public Long getShipment_count() {
		return shipment_count;
	}
	public void setShipment_count(Long shipment_count) {
		this.shipment_count = shipment_count;
	}
	
	

}
