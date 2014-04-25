package org.classified_event_aggregation.web.controller;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.classified_event_aggregation.domain.LogSequenceStatistics;
import org.classified_event_aggregation.domain.Threshold;
import org.classified_event_aggregation.persistence.mysql.ThresholdRepository;
import org.classified_event_aggregation.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.gson.JsonArray;

@Controller
public class Action {

	@Autowired
	private StatisticService statisticService;
	
	@Autowired
	private ThresholdRepository thresholdRepository;
	
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
		List<Map<String, Object>> algorithms = new ArrayList<>();
		
		
		Map<String, Object> durationAlgorithm = new HashMap<>();
		durationAlgorithm.put("algorithmName", "durations");
		durationAlgorithm.put("stats", statisticService.getDurations(applicationName, sequenceName, 1000, null, null, true).toString());		
		Threshold t = thresholdRepository.findByApplicationNameAndSequenceNameAndAlgorithmName(
			applicationName,
			sequenceName, 
			"durations"
		);
		
		if(t != null)
			durationAlgorithm.put("threshold", t);
		
		durationAlgorithm.put("algorithmDisplayName", "Durations");
		algorithms.add(durationAlgorithm);
		
		
		Map<String, Object> numExceptions = new HashMap<>();
		numExceptions.put("algorithmName", "numExceptions");
		numExceptions.put("stats", statisticService.getNumExceptions(applicationName, sequenceName, 1000, null, null, true).toString());		
		Threshold t2 = thresholdRepository.findByApplicationNameAndSequenceNameAndAlgorithmName(
			applicationName,
			sequenceName, 
			"numExceptions"
		);
		
		if(t2 != null)
			numExceptions.put("threshold", t2);
		
		numExceptions.put("algorithmDisplayName", "Number of exceptions");
		algorithms.add(numExceptions);
		
		
		Map<String, Object> standardScoreOfNumExceptionsAlgorithm = new HashMap<>();
		standardScoreOfNumExceptionsAlgorithm.put("algorithmName", "standardScoreOfNumExceptions");
		standardScoreOfNumExceptionsAlgorithm.put("stats", statisticService.getStandardScoresOfNumExceptions(applicationName, sequenceName, 1000, null, null, true).toString());		
		Threshold t3 = thresholdRepository.findByApplicationNameAndSequenceNameAndAlgorithmName(
			applicationName,
			sequenceName, 
			"standardScoreOfNumExceptions"
		);
		
		if(t3 != null)
			standardScoreOfNumExceptionsAlgorithm.put("threshold", t3);
		
		standardScoreOfNumExceptionsAlgorithm.put("algorithmDisplayName", "Standard scores of number of exceptions");
		algorithms.add(standardScoreOfNumExceptionsAlgorithm);
		
		Map<String, Object> standardScoreOfDurationAlgorithm = new HashMap<>();
		standardScoreOfDurationAlgorithm.put("algorithmName", "standardScoreOfDuration");
		standardScoreOfDurationAlgorithm.put("stats", statisticService.getStandardScoresOfDuration(applicationName, sequenceName, 1000, null, null, true).toString());		
		Threshold t4 = thresholdRepository.findByApplicationNameAndSequenceNameAndAlgorithmName(
			applicationName,
			sequenceName, 
			"standardScoreOfDuration"
		);
		
		if(t4 != null)
			standardScoreOfDurationAlgorithm.put("threshold", t4);
		
		standardScoreOfDurationAlgorithm.put("algorithmDisplayName", "Standard scores of durations");
		algorithms.add(standardScoreOfDurationAlgorithm);
		
		model.addAttribute("algorithms", algorithms);
		
		
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
	
	@RequestMapping(method=RequestMethod.GET, value = "/rest/threshold", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> getThreshold(@ModelAttribute Threshold threshold){
		Threshold t = thresholdRepository.findByApplicationNameAndSequenceNameAndAlgorithmName(
			threshold.getApplicationName(), 
			threshold.getSequenceName(), 
			threshold.getAlgorithmName()
		);
		
		if(t == null){
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND); 
		} else {
			return new ResponseEntity<String>(t.toJson().toString(), HttpStatus.OK); 
		}
	}
	
	@RequestMapping(method=RequestMethod.DELETE, value = "/rest/threshold")
	@ResponseStatus(HttpStatus.OK)
	public void deleteThreshold(@ModelAttribute Threshold threshold){
		thresholdRepository.delete(threshold.getId());
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/rest/threshold", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> createThreshold(@ModelAttribute Threshold threshold){
		if(threshold.getId() != null)
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		
		Threshold t = thresholdRepository.save(threshold);
		return new ResponseEntity<>(t.toJson().toString(), HttpStatus.CREATED);
	}
	
	@RequestMapping(method=RequestMethod.PUT, value = "/rest/threshold")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<String> updateThreshold(@ModelAttribute Threshold threshold){
		if(threshold.getId() == null)
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		
		Threshold t = thresholdRepository.save(threshold);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@RequestMapping("/")
	public String getMain(){
		return "main";
	}

}
