package com.bigrequest.BigRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;

public class HttpClientTest {

	HttpClient client;
	String url = "/apphandler/request/0.2.0/android/14131C047A504046465945514455435D888427CC/200.95.162.199/Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.1-update1%3B+en-au%3B+T3020+Build%2FERE27%29+AppleWebKit%2F530.17+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F530.17/live/html/text+picture/1/imei/8762345898437598/A/NA/iueuytgw";
	@Before
	public void startServer(){
		Configuration conf = new PropertiesConfiguration();
		conf.setProperty("host", "192.168.1.91");
		conf.setProperty("port", "7100");
		conf.setProperty("url", url);
		client = new HttpClient(conf);
	}
	
	@Test
	public void makeRequest(){
		client.makeRquest(url);
	}
	
}
