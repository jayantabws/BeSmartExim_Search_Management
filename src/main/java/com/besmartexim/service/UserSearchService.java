package com.besmartexim.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.besmartexim.controller.UserSearchController;
import com.besmartexim.database.entity.UserSearch;
import com.besmartexim.database.repository.UserSearchRepository;
import com.besmartexim.dto.request.SearchCountUpdateRequest;
import com.besmartexim.dto.request.SuggestionRequest;
import com.besmartexim.dto.request.UserSearchRequest;
import com.besmartexim.dto.response.ListCitiesResponse;
import com.besmartexim.dto.response.ListCountriesResponse;
import com.besmartexim.dto.response.ListExportersResponse;
import com.besmartexim.dto.response.ListHscodesResponse;
import com.besmartexim.dto.response.ListImportersResponse;
import com.besmartexim.dto.response.ListPortsResponse;
import com.besmartexim.dto.response.ListShipmentModeResponse;
import com.besmartexim.dto.response.SearchDetails;
import com.besmartexim.dto.response.SearchDetailsResponse;
import com.besmartexim.dto.response.SuggestionListResponse;
import com.besmartexim.dto.response.UserDetailsResponse;
import com.besmartexim.dto.response.UserSearchResponse;
import com.besmartexim.util.QueryConstant;
import com.besmartexim.util.QueryUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserSearchService {

	@Autowired
	UserSearchRepository userSearchRepository;
	
	@Autowired
	QueryUtil queryUtil;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	UserSearchServiceHelper userSearchServiceHelper;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	private static final Logger logger = LoggerFactory.getLogger(UserSearchService.class);
	
	public UserSearchResponse search(UserSearchRequest userSearchRequest,Long accessedBy) throws Exception {
		UserSearch userSearch = new UserSearch();
		
		if(userSearchRequest.getSearchId()==null || userSearchRequest.getSearchId().equals("")){
			userSearch.setCreatedDate(new Date());
			userSearch.setCreatedBy(accessedBy);
			userSearch.setIsSaved("N");
			userSearch.setIsDownloaded("N");
			userSearch.setTotalRecords(Long.valueOf(0));
			userSearch.setSearchJson(objectMapper.writeValueAsString(userSearchRequest));
			String query = queryUtil.buildSearchQuery(userSearchRequest);
			userSearch.setSearchQuery(query);
			userSearchRepository.save(userSearch);
		}else {
			UserSearch existingUserSearch = userSearchRepository.findById(userSearchRequest.getSearchId()).get();
			existingUserSearch.setId(userSearchRequest.getSearchId());
			existingUserSearch.setModifiedDate(new Date());
			existingUserSearch.setModifiedBy(accessedBy);
			//existingUserSearch.setIsSaved("N");
			//existingUserSearch.setIsDownloaded("N");
			existingUserSearch.setSearchJson(objectMapper.writeValueAsString(userSearchRequest));
			String query = queryUtil.buildSearchQuery(userSearchRequest);
			existingUserSearch.setSearchQuery(query);
			userSearchRepository.save(existingUserSearch);
			
			userSearch.setId(userSearchRequest.getSearchId());
		}
		
		 
		Connection connection = null;
		ResultSet rs =null;
		UserSearchResponse userSearchResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.searchProcedure;
		else
			proeName = QueryConstant.searchProcedurePrefix+userSearchRequest.getCountryCode().toUpperCase()+QueryConstant.searchProcedureSuffix;
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  callableStatement.setString(17, userSearchRequest.getOrderByColumn());
		  callableStatement.setString(18, userSearchRequest.getOrderByMode());
		  callableStatement.setInt(19, userSearchRequest.getPageNumber());
		  callableStatement.setInt(20, userSearchRequest.getNumberOfRecords());
		  

		  callableStatement.execute();
		
		  rs = callableStatement.getResultSet();


		  userSearchResponse = userSearchServiceHelper.creteResponse(rs, userSearchRequest);
		
		  userSearchResponse.setSearchId(userSearch.getId());
		 
		  
		  
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
			if(rs!=null)  rs.close();
			if(connection!=null) connection.close();
		}
		 
		  
		return userSearchResponse;
	}
	
	public Long searchcount(UserSearchRequest userSearchRequest) throws Exception {
		Long count = 0l; 
		Connection connection = null;ResultSet rs =null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.searchCountProcedure;
		else
			proeName = QueryConstant.searchCountProcedurePrefix+userSearchRequest.getCountryCode().toUpperCase()+QueryConstant.searchCountProcedureSuffix;
		
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		
		  callableStatement.execute();
		
		  rs = callableStatement.getResultSet();

		  while(rs.next()) {
			  count = rs.getLong("total_shipment");
		  }

			}catch(Exception e){
				logger.error(e.toString());
			}finally {
				if(rs!=null)  rs.close();
				if(connection!=null) connection.close();
			}
		return count;
	}
	
	public Long searchcountbycolumn(UserSearchRequest userSearchRequest) throws Exception {
		Long count = 0l; 
		Connection connection = null;ResultSet rs =null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		CallableStatement callableStatement = connection.prepareCall(QueryConstant.searchCountByColumnNameProcedure);
		callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  callableStatement.setString(17, userSearchRequest.getColumnName());
		
		  callableStatement.execute();
		
		  rs = callableStatement.getResultSet();

		  while(rs.next()) {
			  count = rs.getLong("total_count");
		  }

		}catch(Exception e){
			logger.error(e.toString());
		}finally {
				if(rs!=null)  rs.close();
				if(connection!=null) connection.close();
			}
		return count;
	}

	public ListImportersResponse listimporters(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListImportersResponse listImportersResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listImportersBySearchProcedure;
		else
			proeName = QueryConstant.listImportersBySearchForeignProcedure;
		
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listImportersResponse = userSearchServiceHelper.creteListImporters(rs);
		
		  
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
		if(rs!=null)  rs.close();
		if(connection!=null) connection.close();
	}
		return listImportersResponse;
	}

	public ListExportersResponse listexporters(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListExportersResponse listExportersResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listExportersBySearchProcedure;
		else
			proeName = QueryConstant.listExportersBySearchForeignProcedure;
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listExportersResponse = userSearchServiceHelper.creteListExporters(rs);		
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
		if(rs!=null)  rs.close();
		if(connection!=null) connection.close();
	}
		
		return listExportersResponse;
	}

	public ListCountriesResponse listforeigncountries(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListCountriesResponse listCountriesResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listForeignCountriesBySearchProcedure;
		else
			proeName = QueryConstant.listForeignCountriesBySearchForeignProcedure;
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listCountriesResponse = userSearchServiceHelper.creteListCountries(rs);		
		  
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
			if(rs!=null)  rs.close();
			if(connection!=null) connection.close();
		}
			
		return listCountriesResponse;
	}
	
	public ListCitiesResponse listindiancities(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListCitiesResponse listCitiesResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listIndianCitiesBySearchProcedure;
		else
			proeName = QueryConstant.listIndianCitiesBySearchForeignProcedure;
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listCitiesResponse = userSearchServiceHelper.creteListCities(rs);		
		  
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
			if(rs!=null)  rs.close();
			if(connection!=null) connection.close();
		}
			
		return listCitiesResponse;
	}

	public ListPortsResponse listindianports(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListPortsResponse listPortsResponse =null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listIndianPortsBySearchProcedure;
		else
			proeName = QueryConstant.listIndianPortsBySearchForeignProcedure;
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listPortsResponse = userSearchServiceHelper.creteListPorts(rs);		
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
		if(rs!=null)  rs.close();
		if(connection!=null) connection.close();
	}
		
		return listPortsResponse;
	}
	
	public ListPortsResponse listforeignports(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListPortsResponse listPortsResponse =null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listForeignPortsBySearchProcedure;
		else
			proeName = QueryConstant.listForeignPortsBySearchForeignProcedure;
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listPortsResponse = userSearchServiceHelper.creteListPorts(rs);		
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
		if(rs!=null)  rs.close();
		if(connection!=null) connection.close();
	}
		
		return listPortsResponse;
	}

	public ListHscodesResponse listhscodes(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListHscodesResponse listHscodesResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listHscodesBySearchProcedure;
		else
			proeName = QueryConstant.listHscodesBySearchForeignProcedure;
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listHscodesResponse = userSearchServiceHelper.creteListHscodes(rs);		
		  
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
		if(rs!=null)  rs.close();
		if(connection!=null) connection.close();
	}
		return listHscodesResponse;
		
	}
	
	public ListHscodesResponse listhscodes4digit(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListHscodesResponse listHscodesResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listHscodes4DigitBySearchProcedure;
		else
			proeName = QueryConstant.listHscodes4DigitBySearchForeignProcedure;
		
		CallableStatement callableStatement = connection.prepareCall(proeName);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listHscodesResponse = userSearchServiceHelper.creteListHscodes(rs);		
		  
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
		if(rs!=null)  rs.close();
		if(connection!=null) connection.close();
		}
		return listHscodesResponse;
		
	}
	
	public ListShipmentModeResponse listshipmentmode(UserSearchRequest userSearchRequest) throws Exception {
		Connection connection = null;ResultSet rs =null;ListShipmentModeResponse listShipmentModeResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		CallableStatement callableStatement = connection.prepareCall(QueryConstant.listShipmentModeBySearchProcedure);
		  callableStatement.setString(1, userSearchRequest.getSearchType().getValue());
		  callableStatement.setString(2, userSearchRequest.getTradeType().getValue());
		  callableStatement.setString(3, userSearchRequest.getFromDate());
		  callableStatement.setString(4, userSearchRequest.getToDate());
		  callableStatement.setString(5, userSearchRequest.getSearchBy().getValue());
		  callableStatement.setString(6, userSearchRequest.getMatchType().getValue());
		  callableStatement.setString(7, queryUtil.listToString(userSearchRequest.getSearchValue()));
		  callableStatement.setString(8, userSearchRequest.getCountryCode());
		  callableStatement.setString(9, queryUtil.listToString(userSearchRequest.getHsCodeList()));
		  callableStatement.setString(10, queryUtil.listToString(userSearchRequest.getHsCode4DigitList()));
		  callableStatement.setString(11, queryUtil.listToString(userSearchRequest.getExporterList()));
		  callableStatement.setString(12, queryUtil.listToString(userSearchRequest.getImporterList()));
		  callableStatement.setString(13, queryUtil.listToString(userSearchRequest.getCityOriginList()));
		  callableStatement.setString(14, queryUtil.listToString(userSearchRequest.getCityDestinationList()));
		  callableStatement.setString(15, queryUtil.listToString(userSearchRequest.getPortOriginList()));
		  callableStatement.setString(16, queryUtil.listToString(userSearchRequest.getPortDestinationList()));
		  
		  
		  callableStatement.execute();
			
		  rs = callableStatement.getResultSet();


		  listShipmentModeResponse = userSearchServiceHelper.creteListShipmentMode(rs);		
		  
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
		if(rs!=null)  rs.close();
		if(connection!=null) connection.close();
	}
		return listShipmentModeResponse;
		
	}
	
	public SearchDetailsResponse searchDetails(Long searchId) throws Exception
	{
		
		SearchDetailsResponse searchDetailsResponse = new SearchDetailsResponse();
		List<SearchDetails> list = new ArrayList<SearchDetails>();
		SearchDetails searchDetails = new SearchDetails();
		
		Optional<UserSearch> ulist = userSearchRepository.findById(searchId);
		
		if(ulist.isPresent()) {
			UserSearch userSearch = ulist.get();
			
			searchDetails.setSearchId(searchId);
			searchDetails.setUserSearchQuery(objectMapper.readValue(userSearch.getSearchJson(), UserSearchRequest.class));
			searchDetails.setTotalRecords(userSearch.getTotalRecords());
			searchDetails.setIsDownloaded(userSearch.getIsDownloaded());
			searchDetails.setDownloadedDate(userSearch.getDownloadedDate());
			searchDetails.setCreatedDate(userSearch.getCreatedDate());
			searchDetails.setCreatedBy(userSearch.getCreatedBy());
			searchDetails.setDownloadedBy(userSearch.getDownloadedBy());
		}
		list.add(searchDetails);
		
		searchDetailsResponse.setQueryList(list);
		
		return searchDetailsResponse;
		
	}

	public SearchDetailsResponse topFiveQueries(Long usetId) throws Exception{
		SearchDetailsResponse searchDetailsResponse = new SearchDetailsResponse();
		List<SearchDetails> list = new ArrayList<SearchDetails>();
		SearchDetails searchDetails = null;
	
		List<UserSearch> userSearchList = userSearchRepository.findTop5ByCreatedByOrderByCreatedDateDesc(usetId);
		
		for (Iterator iterator = userSearchList.iterator(); iterator.hasNext();) {
			UserSearch userSearch = (UserSearch) iterator.next();
			searchDetails = new SearchDetails();
			searchDetails.setSearchId(userSearch.getId());
			searchDetails.setCreatedDate(userSearch.getCreatedDate());
			searchDetails.setTotalRecords(userSearch.getTotalRecords());
			searchDetails.setCreatedBy(userSearch.getCreatedBy());
			searchDetails.setUserSearchQuery(objectMapper.readValue(userSearch.getSearchJson(), UserSearchRequest.class));
			searchDetails.setIsDownloaded(userSearch.getIsDownloaded());
			searchDetails.setDownloadedDate(userSearch.getDownloadedDate());
			searchDetails.setCreatedDate(userSearch.getCreatedDate());
			searchDetails.setCreatedBy(userSearch.getCreatedBy());
			searchDetails.setDownloadedBy(userSearch.getDownloadedBy());
			list.add(searchDetails);
		}
		
		searchDetailsResponse.setQueryList(list);
		
		return searchDetailsResponse;
	}
	
	
	@Value( "${usermanagement.service.url}" )
	private String usermanagementUrl;
	
	@Autowired
	private RestTemplate restTemplate;

	public SearchDetailsResponse listAllQueries(Long userId,Long uplineId,Long accessedBy) throws Exception {
		
		SearchDetailsResponse searchDetailsResponse = new SearchDetailsResponse();
		List<SearchDetails> list = new ArrayList<SearchDetails>();
		SearchDetails searchDetails = null;
		List<UserSearch> userSearchList = null;
	
		if(userId != null)
		{
			userSearchList = userSearchRepository.findByCreatedByOrderByCreatedDateDesc(userId);
		}
		else if(uplineId != null)
		{
			userSearchList = userSearchRepository.findByUplineIdOrderByCreatedDateDesc(uplineId);
		}
		else
		{
			userSearchList = userSearchRepository.findAllByOrderByCreatedDateDesc();
		}
		
		// Fetch user management Data From User Data
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.add("accessedBy", ""+accessedBy);
				    
		
		for (Iterator iterator = userSearchList.iterator(); iterator.hasNext();) {
			UserSearch userSearch = (UserSearch) iterator.next();
			searchDetails = new SearchDetails();
			searchDetails.setSearchId(userSearch.getId());
			searchDetails.setCreatedDate(userSearch.getCreatedDate());
			searchDetails.setTotalRecords(userSearch.getTotalRecords());
			searchDetails.setCreatedBy(userSearch.getCreatedBy());
			searchDetails.setUserSearchQuery(objectMapper.readValue(userSearch.getSearchJson(), UserSearchRequest.class));
			String absUrl = usermanagementUrl + userSearch.getCreatedBy();
			//System.out.println("absUrl = "+ absUrl);
		    ResponseEntity<UserDetailsResponse> responseEntity = restTemplate.exchange(absUrl,
		    		HttpMethod.GET, new HttpEntity<Object>(headers),
		    		UserDetailsResponse.class);
		    UserDetailsResponse userDetailsResponse = responseEntity.getBody();
			searchDetails.setCreatedByName(userDetailsResponse.getFirstname()+" "+userDetailsResponse.getLastname());
			searchDetails.setCreatedByEmail(userDetailsResponse.getEmail());
			
			searchDetails.setIsDownloaded(userSearch.getIsDownloaded());
			searchDetails.setDownloadedDate(userSearch.getDownloadedDate());
			searchDetails.setDownloadedBy(userSearch.getDownloadedBy());
			
			list.add(searchDetails);
		}
		
		searchDetailsResponse.setQueryList(list);
		
		return searchDetailsResponse;
	}

	public void updatesearchcount(@Valid SearchCountUpdateRequest searchCountUpdateRequest, Long searchId,
			Long accessedBy) {
		
			UserSearch existingUserSearch = userSearchRepository.findById(searchId).get();
			existingUserSearch.setTotalRecords(searchCountUpdateRequest.getTotalRecords());
			existingUserSearch.setModifiedDate(new Date());
			existingUserSearch.setModifiedBy(accessedBy);
			userSearchRepository.save(existingUserSearch);
		
	}

	public SuggestionListResponse listSuggestion(SuggestionRequest suggestionRequest, Long accessedBy) throws Exception{
		Connection connection = null;ResultSet rs =null;SuggestionListResponse suggestionListResponse=null;
		try {
		connection = jdbcTemplate.getDataSource().getConnection();
		
		String proeName = null;
		
		if(suggestionRequest.getCountryCode().equalsIgnoreCase("IND"))
			proeName = QueryConstant.listSuggestionBySearchProcedure;
		else
			proeName = QueryConstant.listSuggestionBySearchForeignProcedure;
		
			CallableStatement callableStatement = connection.prepareCall(proeName);
			callableStatement.setString(1, suggestionRequest.getTradeType().getValue());
			callableStatement.setString(2, suggestionRequest.getCountryCode());
			callableStatement.setString(3, suggestionRequest.getFromDate());
			callableStatement.setString(4, suggestionRequest.getToDate());
			callableStatement.setString(5, suggestionRequest.getSearchBy().getValue());
			callableStatement.setString(6, suggestionRequest.getSearchValue());
		  
		  
			callableStatement.execute();
			
			rs = callableStatement.getResultSet();


			suggestionListResponse = userSearchServiceHelper.creteListSuggestion(rs);		
		  
		}catch(Exception e){
			logger.error(e.toString());
		}finally {
		if(rs!=null)  rs.close();
		if(connection!=null) connection.close();
		}
		return suggestionListResponse;
	}

	public void downloadsearch(Long searchId, Long accessedBy) {
		// TODO Auto-generated method stub
		UserSearch existingUserSearch = userSearchRepository.findById(searchId).get();
		existingUserSearch.setIsDownloaded("Y");
		existingUserSearch.setDownloadedBy(accessedBy);
		existingUserSearch.setDownloadedDate(new Date());
		userSearchRepository.save(existingUserSearch);
		
	}

}
