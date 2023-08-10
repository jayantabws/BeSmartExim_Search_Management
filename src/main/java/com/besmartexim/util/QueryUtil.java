package com.besmartexim.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.besmartexim.dto.request.UserSearchRequest;

@Component
public class QueryUtil {

	public String buildSearchQuery(UserSearchRequest userSearchRequest)throws Exception {
		
		/*
		Date frmDate = new SimpleDateFormat("yyyy-MM-dd").parse(userSearchRequest.getFromDate());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(frmDate);
		
		String query= "Select * from"+
					AppConstant.blank+userSearchRequest.getTradeType().getValue()+"_"+userSearchRequest.getCountryCode()+"_"+calendar.get(Calendar.YEAR)+
					AppConstant.blank+"where "+userSearchRequest.getSearchBy().getValue()+" like '"+userSearchRequest.getSearchValue()+"%'"+
					AppConstant.blank+" and sb_date between '"+userSearchRequest.getFromDate()+"' and '"+userSearchRequest.getToDate()+"'";
		*/
		String str = null;
		if(userSearchRequest.getCountryCode().equalsIgnoreCase("IND"))
			str = QueryConstant.searchProcedure;
		else
			str = QueryConstant.searchProcedurePrefix+userSearchRequest.getCountryCode().toUpperCase()+QueryConstant.searchProcedureSuffix;
		
		
		String searchParams="'"+userSearchRequest.getSearchType().getValue()+"','"+userSearchRequest.getTradeType().getValue()+
				"','"+userSearchRequest.getFromDate()+"','"+userSearchRequest.getToDate()+"','"+userSearchRequest.getSearchBy().getValue()+"','"+userSearchRequest.getMatchType().getValue()+
				"','"+listToString(userSearchRequest.getSearchValue())+"','"+userSearchRequest.getCountryCode()+"','"+listToString(userSearchRequest.getHsCodeList())+"','"+listToString(userSearchRequest.getHsCode4DigitList())+"','"+listToString(userSearchRequest.getExporterList())+
				"','"+listToString(userSearchRequest.getImporterList())+"','"+listToString(userSearchRequest.getCityOriginList())+"','"+listToString(userSearchRequest.getCityDestinationList())+
				"','"+listToString(userSearchRequest.getPortOriginList())+"','"+listToString(userSearchRequest.getPortDestinationList())+"','"+userSearchRequest.getOrderByColumn()+
				"','"+userSearchRequest.getOrderByMode()+"',"+userSearchRequest.getPageNumber()+","+userSearchRequest.getNumberOfRecords();
		
		str =  str.replace("?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?", searchParams);
		
		return str;
	}
	
	
	public static String listToString(List<String> list) {
		String out="";
		if(list!=null && !list.isEmpty()) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				String str = (String) iterator.next();
				out=out+"'"+str+"',";
			}
			
			out=out.substring(0, out.length()-1);
		}
		
		return out;
	}
	
	
	public static void main(String args[]) {
		List<String> list = new ArrayList<String>();
		list.add("Jayanta");
		list.add("Palash");
		System.out.println(listToString(list));
	}
}
