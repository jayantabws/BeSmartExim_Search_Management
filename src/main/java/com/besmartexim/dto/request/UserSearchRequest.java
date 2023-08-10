package com.besmartexim.dto.request;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;

public class UserSearchRequest {

	//@NotBlank
	private SearchType searchType;
	
	//@NotBlank
	private TreadType tradeType;
	
	@NotBlank
	private String fromDate;
	
	@NotBlank
	private String toDate;
	
	//@NotBlank
	private SearchBy searchBy;	
	
	private List<String> searchValue;
	
	private MatchType matchType;
	
	//@Max(value = 3)
	private String countryCode;
	
	private Long searchId;
	
	private List<String> hsCodeList;
	
	private List<String> hsCode4DigitList;
	
	private List<String> exporterList;
	
	private List<String> importerList;
	
	private List<String> cityOriginList;
	
	private List<String> cityDestinationList;
	
	private List<String> portOriginList;
	
	private List<String> portDestinationList;	
	
	private String columnName;
	
	private String orderByColumn;
	
	private String orderByMode;
	
	private Integer pageNumber;
	
	private Integer numberOfRecords;

	public SearchType getSearchType() {
		return searchType;
	}

	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}

	public TreadType getTradeType() {
		return tradeType;
	}

	public void setTradeType(TreadType tradeType) {
		this.tradeType = tradeType;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public SearchBy getSearchBy() {
		return searchBy;
	}

	public void setSearchBy(SearchBy searchBy) {
		this.searchBy = searchBy;
	}

	public List<String> getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(List<String> searchValue) {
		this.searchValue = searchValue;
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public Long getSearchId() {
		return searchId;
	}

	public void setSearchId(Long searchId) {
		this.searchId = searchId;
	}

	public List<String> getHsCodeList() {
		return hsCodeList;
	}

	public void setHsCodeList(List<String> hsCodeList) {
		this.hsCodeList = hsCodeList;
	}

	public List<String> getHsCode4DigitList() {
		return hsCode4DigitList;
	}

	public void setHsCode4DigitList(List<String> hsCode4DigitList) {
		this.hsCode4DigitList = hsCode4DigitList;
	}

	public List<String> getExporterList() {
		return exporterList;
	}

	public void setExporterList(List<String> exporterList) {
		this.exporterList = exporterList;
	}

	public List<String> getImporterList() {
		return importerList;
	}

	public void setImporterList(List<String> importerList) {
		this.importerList = importerList;
	}

	public List<String> getCityOriginList() {
		return cityOriginList;
	}

	public void setCityOriginList(List<String> cityOriginList) {
		this.cityOriginList = cityOriginList;
	}

	public List<String> getCityDestinationList() {
		return cityDestinationList;
	}

	public void setCityDestinationList(List<String> cityDestinationList) {
		this.cityDestinationList = cityDestinationList;
	}

	public List<String> getPortOriginList() {
		return portOriginList;
	}

	public void setPortOriginList(List<String> portOriginList) {
		this.portOriginList = portOriginList;
	}

	public List<String> getPortDestinationList() {
		return portDestinationList;
	}

	public void setPortDestinationList(List<String> portDestinationList) {
		this.portDestinationList = portDestinationList;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getOrderByColumn() {
		return orderByColumn;
	}

	public void setOrderByColumn(String orderByColumn) {
		this.orderByColumn = orderByColumn;
	}

	public String getOrderByMode() {
		return orderByMode;
	}

	public void setOrderByMode(String orderByMode) {
		this.orderByMode = orderByMode;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Integer getNumberOfRecords() {
		return numberOfRecords;
	}

	public void setNumberOfRecords(Integer numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	
	
	
}





