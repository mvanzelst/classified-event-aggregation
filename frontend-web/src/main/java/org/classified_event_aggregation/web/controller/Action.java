package org.classified_event_aggregation.web.controller;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.classified_event_aggregation.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;

@Controller
public class Action {

	@Autowired
	private StatisticService statisticService;
	
	@ModelAttribute
	public void getModelAttributes(Model model, HttpServletRequest request) throws UnsupportedEncodingException{
		model.addAttribute("currenturl", URLDecoder.decode(request.getRequestURI(), "UTF-8"));
		model.addAttribute("applications", statisticService.getApplications());
	}
	
	@RequestMapping("/application/*/monitor")
	public String monitorApplication(){
		return "monitor-application";
	}

	@RequestMapping("/application/*/sequence/*/thresholds")
	public String thresholds(){
		return "thresholds";
	}
	
	@RequestMapping("/application/*/sequence/*/monitor")
	public String monitorsequence(){
		return "monitor-sequence";
	}
	
	@RequestMapping("/rest/application/{applicationName}/monitor")
	public @ResponseBody String monitorApplicationRest(
			@RequestParam(defaultValue="1") boolean filterOn,
			@RequestParam(defaultValue="1000") int limit,
			@PathVariable String applicationName
		){
		JsonArray jsonArray = new JsonArray();
		statisticService.getLogSequenceStatistics(applicationName, limit);
		return jsonArray.toString();
	}
	
	@RequestMapping("/rest/application/{applicationName}/sequence/{sequenceName}/monitor")
	public @ResponseBody String monitorsequenceRest(
			@RequestParam(defaultValue="1") boolean filterOn,
			@RequestParam(defaultValue="1000") int limit,
			@PathVariable String applicationName,
			@PathVariable String sequenceName
		){
		JsonArray jsonArray = new JsonArray();
		statisticService.getLogSequenceStatistics(applicationName, sequenceName, limit);
		return jsonArray.toString();
	}

	@RequestMapping("/")
	public String getMain(){
		return "main";
	}

}
