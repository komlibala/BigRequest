package com.bigrequest.BigRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

public class Util {
	 DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	@Test
	public void test(){
		String val = "39, 0, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0";
		String[] split = StringUtils.splitPreserveAllTokens(val,",");
		System.out.println(split.length);
		
		long start = System.currentTimeMillis();
		 String str = "";
		for(int i=0;i<10;i++){
			DateTime dt = new DateTime();
		  str = fmt.print(dt);
		}
		 System.out.println("time taken joda : "+(System.currentTimeMillis() - start));
		 System.out.println(str);
		 start = System.currentTimeMillis();
		 String str1 = "";
		 for(int i=0;i<10;i++){
			 final SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
			 str1 = format.format(new Date());
		 }
		 
		 System.out.println("Time taken simeple : "+(System.currentTimeMillis() - start));
		 System.out.println(str1);
		 
		 String advDoamin = "www.komlimobile.com";
		 String listOfDomains = "http://fuck.zestadz.com,http://fuck.komlimobile.com,http://fuck.google.com";
		 System.out.println(listOfDomains.contains(advDoamin));
		 
	}
}
