package com.easybi.leo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.easybi.leo.common.AppBaseLog;
import com.easybi.leo.common.AppLogEntity;
import com.easybi.leo.common.AppStartupLog;
import com.easybi.leo.utils.GeoUtil;
import com.easybi.leo.utils.PropertiesUtil;

@Controller
@RequestMapping("/collect")
public class LogsCollectController {
	
	@RequestMapping(value = "/index", method = RequestMethod.POST)
	@ResponseBody
	public AppLogEntity collect(@RequestBody AppLogEntity e, HttpServletRequest request) {
		System.out.println("=============================");
		//server时间
		long serverTime = System.currentTimeMillis();
		//client时间
		long clientTime = Long.parseLong(request.getHeader("clientTime"));
		//时间校对
		long duration = serverTime - clientTime;
		verifyTime(e,duration);
		//基本属性复制
		copyBaseProperties(e);
		//获取客户端IP并填充地理位置信息
		String clientIP = request.getRemoteAddr();
		processIP(e, clientIP);
		
		//发送到Kafka集群
		return e;
	}
	/*
	 * 操作IP的方法
	 */
	private void processIP(AppLogEntity e, String clientIP) {
		String country = GeoUtil.getLocation(clientIP, GeoUtil.COUNTRY);
		String province = GeoUtil.getLocation(clientIP, GeoUtil.PROVINCE);
		for (AppStartupLog log : e.getAppStartupLogs()) {
			log.setCountry(country);
			log.setProvince(province);
			log.setIpAddress(clientIP);
		}
	}



	/*
	 * 校正时间
	 */
	private void verifyTime(AppLogEntity e, long duration) {
		for (AppBaseLog log : e.getAppStartupLogs()) {
			log.setCreatedAtMs(log.getCreatedAtMs() + duration);
		}
		for (AppBaseLog log : e.getAppPageLogs()) {
			log.setCreatedAtMs(log.getCreatedAtMs() + duration);
		}
		for (AppBaseLog log : e.getAppEventLogs()) {
			log.setCreatedAtMs(log.getCreatedAtMs() + duration);
		}
		for (AppBaseLog log : e.getAppUsageLogs()) {
			log.setCreatedAtMs(log.getCreatedAtMs() + duration);
		}
		for (AppBaseLog log : e.getAppErrorLogs()) {
			log.setCreatedAtMs(log.getCreatedAtMs() + duration);
		}
	}
	/*
	 * 复制基本属性
	 */
	private void copyBaseProperties(AppLogEntity e) {
		PropertiesUtil.copyProperties(e, e.getAppStartupLogs());
		PropertiesUtil.copyProperties(e, e.getAppErrorLogs());
		PropertiesUtil.copyProperties(e, e.getAppEventLogs());
		PropertiesUtil.copyProperties(e, e.getAppPageLogs());
		PropertiesUtil.copyProperties(e, e.getAppUsageLogs());
	}

}
