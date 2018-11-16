package com.easybi.leo.mockclient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * 模拟客户端应用基于RESTful上传日志数据到Collect Web应用.
 */
public class UploadUtil {
	
	private static OutputStream out;
	
	public static void upload(String json) {
		try {
			URL url = new URL("http://localhost:8080/collect/index");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//设置请求方式为post
			connection.setRequestMethod("POST");
			//获取客户端当前时间, 用于服务器进行时钟校对
			connection.setRequestProperty("clientTime", System.currentTimeMillis()+"");
			//允许上传数据
			connection.setDoOutput(true);
			//设置请求头信息, 内容类型为json
			connection.setRequestProperty("Content-Type", "application/json");
			out = connection.getOutputStream();
			out.write(json.getBytes());
			out.flush();
			int code = connection.getResponseCode();
			System.out.println("响应码为: " + code);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
