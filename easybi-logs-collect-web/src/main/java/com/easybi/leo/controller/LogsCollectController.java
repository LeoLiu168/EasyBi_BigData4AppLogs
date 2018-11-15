package com.easybi.leo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/collect")
public class LogsCollectController {
	
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	@ResponseBody
	public void collect(HttpServletRequest request) {
		System.out.println("Hello World!");
	}

}
