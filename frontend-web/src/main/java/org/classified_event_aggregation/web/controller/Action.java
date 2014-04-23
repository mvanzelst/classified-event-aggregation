package org.classified_event_aggregation.web.controller;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.classified_event_aggregation.model.LogSequenceStatistics;
import org.classified_event_aggregation.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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

	@RequestMapping("/application/{applicationName}/sequence/{sequenceName}/thresholds")
	public String thresholds(
			@PathVariable String applicationName,
			@PathVariable String sequenceName,
			Model model
		){
		model.addAttribute("durations", statisticService.getDurations(applicationName, sequenceName, 1000, null, null, true).toString());
		model.addAttribute("standardScoreOfDuration", statisticService.getStandardScoresOfDuration(applicationName, sequenceName, 1000, null, null, true).toString());
		model.addAttribute("numExceptions", statisticService.getNumExceptions(applicationName, sequenceName, 1000, null, null, true).toString());
		model.addAttribute("standardScoreOfNumExceptions", statisticService.getStandardScoresOfNumExceptions(applicationName, sequenceName, 1000, null, null, true).toString());
		return "thresholds";
	}

	@RequestMapping("/application/*/sequence/*/monitor")
	public String monitorsequence(){
		return "monitor-sequence";
	}
	
	@RequestMapping(value = "/rest/application/{applicationName}/monitor", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String monitorApplicationRest(
			@RequestParam(defaultValue="1") boolean filterOn,
			@RequestParam(defaultValue="1000") int limit,
			@PathVariable String applicationName
		){
		JsonArray jsonArray = new JsonArray();
		List<LogSequenceStatistics> logSequenceStatistics = statisticService.getLogSequenceStatistics(applicationName, limit, null, null, true);
		for (LogSequenceStatistics logSequenceStatisticObject : logSequenceStatistics) {
			jsonArray.add(logSequenceStatisticObject.toJSON());
		}
		return jsonArray.toString();
	}

	@RequestMapping(value = "/rest/application/{applicationName}/sequence/{sequenceName}/monitor", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String monitorSequenceRest(
			@RequestParam(defaultValue="1") boolean filterOn,
			@RequestParam(defaultValue="1000") int limit,
			@PathVariable String applicationName,
			@PathVariable String sequenceName
		){
		JsonArray jsonArray = new JsonArray();
		List<LogSequenceStatistics> logSequenceStatistics = statisticService.getLogSequenceStatistics(applicationName, sequenceName, limit, null, null, true);
		for (LogSequenceStatistics logSequenceStatisticObject : logSequenceStatistics) {
			jsonArray.add(logSequenceStatisticObject.toJSON());
		}
		return jsonArray.toString();
	}
	
	@RequestMapping(value = "/rest/application/{applicationName}/sequence/{sequenceName}/algorithm/{algorithmName}/monitor", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String monitorAlgorithmRest(
			@RequestParam(defaultValue="1") boolean filterOn,
			@RequestParam(defaultValue="1000") int limit,
			@PathVariable String applicationName,
			@PathVariable String sequenceName,
			@PathVariable String algorithmName
		){
		JsonArray jsonArray = new JsonArray();
		List<LogSequenceStatistics> logSequenceStatistics = statisticService.getLogSequenceStatistics(applicationName, sequenceName, algorithmName, limit, null, null, true);
		for (LogSequenceStatistics logSequenceStatisticObject : logSequenceStatistics) {
			jsonArray.add(logSequenceStatisticObject.toJSON());
		}
		return jsonArray.toString();
	}
	
	@RequestMapping(value = "/rest/application/{applicationName}/sequence/{sequenceName}/distinct_algorithm_names", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String monitorAlgorithmRest(
			@PathVariable String applicationName,
			@PathVariable String sequenceName
		){
		JsonArray jsonArray = new JsonArray();
		List<String> algorithmNames = statisticService.getDistinctAlgorithmNames(applicationName, sequenceName);
		for (String algorithmName : algorithmNames) {
			jsonArray.add(new JsonPrimitive(algorithmName));
		}
		return jsonArray.toString();
	}

	@RequestMapping("/")
	public String getMain(){
		return "main";
	}

}
