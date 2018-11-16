package com.easybi.leo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import com.fasterxml.jackson.databind.JsonNode;
import com.maxmind.db.Reader;

public class GeoUtil {
	
	public static final int COUNTRY = 1;
	public static final int PROVINCE = 2;
	public static final int CITY = 3;
	
	private static InputStream inputStream;
	private static Reader reader;
	
	static {
		try {
			inputStream = ClassLoader.getSystemResourceAsStream("GeoLite2-City.mmdb");
			reader = new Reader(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * 获取国家, 省份和城市信息, level代号为:
	 * 1. 国家
	 * 2. 省份
	 * 3. 城市
	 */
	public static String getLocation(String ip, int level) {
		try {
			JsonNode node = reader.get(InetAddress.getByName(ip));
			switch (level) {
			case 1:
				return node.get("country").get("names").get("zh-CN").textValue();
			case 2:
				return node.get("subdivisions").get(0).get("names").get("zh-CN").textValue();
			case 3:
				return node.get("city").get("names").get("zh-CN").textValue();
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	

}
