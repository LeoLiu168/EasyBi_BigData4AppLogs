package com.easybi.leo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.easybi.leo.common.Constants;
import com.easybi.leo.common.GeoInfo;
import com.easybi.leo.logdata.AppBaseLog;
import com.easybi.leo.logdata.AppLogEntity;
import com.easybi.leo.logdata.AppStartupLog;
import com.easybi.leo.utils.GeoUtil;
import com.easybi.leo.utils.PropertiesUtil;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

@SuppressWarnings("deprecation")
@Controller
@RequestMapping("/collect")
public class LogsCollectController {
	//地理位置信息的缓存
	private Map<String, GeoInfo> geoCache = new HashMap<>();
	
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
		sendMessage(e);
		return e;
	}
	/*
	 * 发送至Kafka的方法
	 */
	private void sendMessage(AppLogEntity e) {
		//创建配置对象
		Properties properties = new Properties();
		properties.put("metadata.broker.list", "s202:9092");
		properties.put("serializer.class", "kafka.serializer.StringEncoder");
		properties.put("request.required.acks", "1");
		//创建生产者
		Producer<Integer, String> producer = new Producer<Integer, String>(new ProducerConfig(properties));
		
		sendSingleLog(producer, Constants.TOPIC_APP_STARTUP, e.getAppStartupLogs());
		sendSingleLog(producer,Constants.TOPIC_APP_ERRROR,e.getAppErrorLogs());
		sendSingleLog(producer,Constants.TOPIC_APP_EVENT,e.getAppEventLogs());
		sendSingleLog(producer,Constants.TOPIC_APP_PAGE,e.getAppPageLogs());
		sendSingleLog(producer,Constants.TOPIC_APP_USAGE,e.getAppUsageLogs());
		//发送消息
		producer.close();
	}
	/*
	 * 发送单个消息的方法
	 */
	private void sendSingleLog(Producer<Integer, String> producer, String topic,
			AppBaseLog[] logs) {
		for (AppBaseLog log : logs) {
			String logMessage = JSONObject.toJSONString(log);
			KeyedMessage<Integer, String> data = new KeyedMessage<Integer, String>(topic, logMessage);
			producer.send(data);
		}
	}
	/*
	 * 操作IP的方法(缓存地理位置信息)
	 */
	private void processIP(AppLogEntity e, String clientIP) {
		GeoInfo info = geoCache.get(clientIP);
		if (info == null) {
			info = new GeoInfo();
			info.setCountry(GeoUtil.getLocation(clientIP, GeoUtil.COUNTRY));
			info.setProvince(GeoUtil.getLocation(clientIP, GeoUtil.PROVINCE));
			geoCache.put(clientIP, info);
		}
		for (AppStartupLog log : e.getAppStartupLogs()) {
			log.setCountry(info.getCountry());
			log.setProvince(info.getProvince());
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
