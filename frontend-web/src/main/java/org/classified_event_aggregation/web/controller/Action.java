package org.classified_event_aggregation.web.controller;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.classified_event_aggregation.domain.DimensionlessStatisticType;
import org.classified_event_aggregation.domain.LogSequenceStatistics;
import org.classified_event_aggregation.domain.Threshold;
import org.classified_event_aggregation.persistence.cassandra.LogMessageStore;
import org.classified_event_aggregation.persistence.mysql.ThresholdRepository;
import org.classified_event_aggregation.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Controller
public class Action {

	@Autowired
	private StatisticService statisticService;
	
	@Autowired
	private LogMessageStore logMessageStore;
	
	@Autowired
	private ThresholdRepository thresholdRepository;
	
	@ModelAttribute
	public void getModelAttributes(Model model, HttpServletRequest request) throws UnsupportedEncodingException{
		model.addAttribute("currenturl", URLDecoder.decode(request.getRequestURI(), "UTF-8"));
		model.addAttribute("applications", statisticService.getApplications());
	}
	
	@RequestMapping("/")
	public String index(){
		return "main";
	}
	
	@RequestMapping("/application/monitor")
	public String monitorApplication(
			@RequestParam String applicationName,
			@RequestParam(required = false) String sequenceName,
			Model model
		){
		List<LogSequenceStatistics> durationStatistics = statisticService.getDerivedStatistic(applicationName, sequenceName, 1000, -1L, -1L, true, DimensionlessStatisticType.STANDARD_SCORE_OF_SEQUENCE_DURATION);
		durationStatistics = Lists.newArrayList(Iterables.filter(durationStatistics, new Predicate<LogSequenceStatistics>() {
			@Override
			public boolean apply(LogSequenceStatistics lss) {
				return lss.getStatistics().get("stdScore").getAsDouble() >= 6.0;
			}
		}));
		
		List<LogSequenceStatistics> exceptionStatistics = statisticService.getDerivedStatistic(applicationName, sequenceName, 1000, -1L, -1L, true, DimensionlessStatisticType.STANDARD_SCORE_OF_NUMBER_OF_EXCEPTIONS);
		exceptionStatistics = Lists.newArrayList(Iterables.filter(exceptionStatistics, new Predicate<LogSequenceStatistics>() {
			@Override
			public boolean apply(LogSequenceStatistics lss) {
				return lss.getStatistics().get("stdScore").getAsDouble() >= 6.0;
			}
		}));
		
		Function<LogSequenceStatistics, Long> orderByTimestamp = new Function<LogSequenceStatistics, Long>() {
			@Override
			public Long apply(LogSequenceStatistics arg0) {
				return arg0.getEndTimestamp();
			}
		};
		Ordering<LogSequenceStatistics> ordering = Ordering.natural().onResultOf(orderByTimestamp);
		ImmutableSortedSet<LogSequenceStatistics> orderedList = ImmutableSortedSet.orderedBy(ordering).addAll(durationStatistics).addAll(exceptionStatistics).build();
		model.addAttribute("logSequenceStatistics", orderedList);
		
		Map<String, List<String>> logMessagesBySequenceId = new HashMap<String, List<String>>();
		for (LogSequenceStatistics logSequenceStatistics : orderedList) {
			logMessagesBySequenceId.put(logSequenceStatistics.getSequenceId(), logMessageStore.getLogMessages(logSequenceStatistics.getSequenceId()));
		}
		model.addAttribute("logMessagesBySequenceId", logMessagesBySequenceId);
		return "monitor";
	}

	
	@RequestMapping("/application/sequence/thresholds")
	public String thresholds(
		@RequestParam String applicationName,
		@RequestParam String sequenceName,
		Model model
	){
		// Duration
		List<Map<String, Object>> dimensionlessStatistics = new ArrayList<Map<String,Object>>();
		Map<String, Object> dimensionlessStatisticType1 = new HashMap<String, Object>();
		dimensionlessStatisticType1.put("name", "Standard score of duration");
		dimensionlessStatisticType1.put("type", "STANDARD_SCORE_OF_SEQUENCE_DURATION");
		dimensionlessStatisticType1.put("threshold", thresholdRepository.findByApplicationNameAndSequenceNameAndDimensionlessStatisticType(applicationName, sequenceName, DimensionlessStatisticType.STANDARD_SCORE_OF_SEQUENCE_DURATION));
		List<LogSequenceStatistics> logSequenceStatistics = statisticService.getDerivedStatistic(applicationName, sequenceName, 1000, -1L, -1L, true, DimensionlessStatisticType.STANDARD_SCORE_OF_SEQUENCE_DURATION);
		JsonArray arr = new JsonArray();
		for (LogSequenceStatistics logSequenceStatisticsObject : logSequenceStatistics) {
			arr.add(logSequenceStatisticsObject.getStatistics().getAsJsonPrimitive("stdScore"));
		}
		dimensionlessStatisticType1.put("stats", arr);

		// Num exceptions
		Map<String, Object> dimensionlessStatisticType2 = new HashMap<String, Object>();
		dimensionlessStatisticType2.put("name", "Standard score of number of exceptions");
		dimensionlessStatisticType2.put("type", "STANDARD_SCORE_OF_NUMBER_OF_EXCEPTIONS");
		dimensionlessStatisticType2.put("threshold", thresholdRepository.findByApplicationNameAndSequenceNameAndDimensionlessStatisticType(applicationName, sequenceName, DimensionlessStatisticType.STANDARD_SCORE_OF_NUMBER_OF_EXCEPTIONS));
		logSequenceStatistics = statisticService.getDerivedStatistic(applicationName, sequenceName, 1000, -1L, -1L, true, DimensionlessStatisticType.STANDARD_SCORE_OF_NUMBER_OF_EXCEPTIONS);
		arr = new JsonArray();
		for (LogSequenceStatistics logSequenceStatisticsObject : logSequenceStatistics) {
			arr.add(logSequenceStatisticsObject.getStatistics().getAsJsonPrimitive("stdScore"));
		}
		
		dimensionlessStatisticType2.put("stats", arr);

		dimensionlessStatistics.add(dimensionlessStatisticType1);
		dimensionlessStatistics.add(dimensionlessStatisticType2);

		model.addAttribute("dimensionlessStatistics", dimensionlessStatistics);
		model.addAttribute("applicationName", applicationName);
		model.addAttribute("sequenceName", sequenceName);
		return "thresholds";
	}

	@RequestMapping(value = "/rest/application/monitor", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String monitorApplicationRest(
			@RequestParam(defaultValue="1") boolean filterOn,
			@RequestParam(defaultValue="1000") int limit,
			@RequestParam String applicationName,
			@RequestParam(required = false, defaultValue="") String sequenceName,
			@RequestParam DimensionlessStatisticType dimensionlessStatisticType,
			@RequestParam(required = false, defaultValue="-1") Long start
	) throws UnsupportedEncodingException {
		List<LogSequenceStatistics> logSequenceStatistics = statisticService.getDerivedStatistic(applicationName, sequenceName, limit, start, -1L, false, dimensionlessStatisticType);
		
		if(logSequenceStatistics.size() > 0)
			start = Iterables.getLast(logSequenceStatistics).getEndTimestamp() + 1; // Start of next page is end of this page
		
		JsonArray jar = new JsonArray();
		for (LogSequenceStatistics logSequenceStatisticsObject : logSequenceStatistics) {
			jar.add(logSequenceStatisticsObject.toJSON());
		}

		JsonObject job = new JsonObject();
		job.add("log_sequence_stats", jar);
		job.addProperty("next", 
			String.format(
				"/rest/application/monitor?applicationName=%s&sequenceName=%s&dimensionlessStatisticType=%s&limit=%s&filterOn=%s&start=%s",
				UriUtils.encodeQueryParam(applicationName, "UTF-8"),
				UriUtils.encodeQueryParam(sequenceName, "UTF-8"),
				UriUtils.encodeQueryParam(dimensionlessStatisticType.name(), "UTF-8"),
				"" + limit,
				filterOn ? "1" : "",
				"" + start
			)
		);
		return job.toString();
	}
	@RequestMapping(value = "/txt", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String export(
			@RequestParam(defaultValue="1") boolean filterOn,
			@RequestParam(defaultValue="1000") int limit,
			@RequestParam String applicationName,
			@RequestParam(required = false, defaultValue="") String sequenceName,
			@RequestParam DimensionlessStatisticType dimensionlessStatisticType,
			@RequestParam(required = false, defaultValue="-1") Long start
	) throws UnsupportedEncodingException {
		List<LogSequenceStatistics> logSequenceStatistics = statisticService.getDerivedStatistic(applicationName, sequenceName, limit, start, -1L, false, dimensionlessStatisticType);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Value, StdDev, Standard Score\n");
		
		for (LogSequenceStatistics logSequenceStatisticsObject : logSequenceStatistics) {
			switch (dimensionlessStatisticType) {
			case STANDARD_SCORE_OF_SEQUENCE_DURATION:
				sb.append(logSequenceStatisticsObject.getStatistics().getAsJsonPrimitive("duration").getAsDouble());
				sb.append(",");
				break;
			case STANDARD_SCORE_OF_NUMBER_OF_EXCEPTIONS:
				sb.append(logSequenceStatisticsObject.getStatistics().getAsJsonPrimitive("num_exceptions").getAsDouble());
				sb.append(",");
				break;
			}
			sb.append(logSequenceStatisticsObject.getStatistics().getAsJsonPrimitive("standard_deviation").getAsDouble());
			sb.append(",");
			sb.append(logSequenceStatisticsObject.getStatistics().getAsJsonPrimitive("stdScore").getAsDouble());
			sb.append("\n");
		}
		
		return sb.toString();
	}

}
