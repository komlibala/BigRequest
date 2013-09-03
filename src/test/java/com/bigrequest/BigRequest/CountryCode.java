package com.bigrequest.BigRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class CountryCode {

	public static void main(String args[]) throws IOException{
		String countryPath = "/home/km/blocked_countries.txt"; 
		String countryWithCodePath = "/home/km/countries_with_code.txt";
		
		List<String> blockedCountryNames = FileUtils.readLines(new File(countryPath));
		List<String> countryNames = FileUtils.readLines(new File(countryWithCodePath));
		
//		System.out.println(blockedCountryNames);
//		System.out.println(countryNames);
		
		Map<String,String> countryMap = new HashMap<String, String>();
		for(String country : countryNames){
			String[] countrySplit = country.split(",");
			String value = countrySplit[0];
			String key = countrySplit[1].replaceAll("\"","");
			countryMap.put(key, value);
		}
		
		for(String blockCountry : blockedCountryNames){
			String code = countryMap.get(blockCountry);
			String query = "";
			if(code!=null && !blockCountry.equals("Unknown"))
				query = "insert into blocked_countries('country_name','country_code','updated_time','updated_by') VALUES("+"'"+blockCountry+"'"+",'"+code+"'"+",'2013-08-05 11:09:24',1);";
//				query = blockCountry;
				
			System.out.println(query);
		}
//		System.out.println(countryMap);
	}
}
