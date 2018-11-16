package com.easybi.leo.web.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import org.junit.Test;

import com.easybi.leo.utils.GeoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.maxmind.db.Reader;


public class TestGeoLite {
	
	@Test
	public void test1() {
		InputStream inputStream = null;
		Reader reader =null;
		try {
			inputStream = ClassLoader.getSystemResourceAsStream("GeoLite2-City.mmdb");
			reader = new Reader(inputStream);
			JsonNode node = reader.get(InetAddress.getByName("detshirts.com"));
			//国家
			String country = node.get("country").get("names").get("zh-CN").textValue();
			System.out.println(country);
			//省份
			String area = node.get("subdivisions").get(0).get("names").get("zh-CN").textValue();
			//城市
			String city = node.get("city").get("names").get("zh-CN").textValue();
			System.out.println(country + " " + area + " " + city);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
	}
	
	@Test
	public void test2() {
		String host = "taobao.com";
		System.out.println(GeoUtil.getLocation(host, GeoUtil.COUNTRY));
		System.out.println(GeoUtil.getLocation(host, GeoUtil.PROVINCE));
		System.out.println(GeoUtil.getLocation(host, GeoUtil.CITY));
		
	}

}
