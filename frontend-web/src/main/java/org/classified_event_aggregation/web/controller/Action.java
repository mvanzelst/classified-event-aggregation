package org.classified_event_aggregation.web.controller;


import java.util.HashSet;
import java.util.Set;

import org.springframework.core.convert.support.ConversionServiceFactory;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

@Controller
public class Action {
	
	@RequestMapping("/")
	public String streamgraph() {
		return "streamgraph";
	}
	
	@RequestMapping("/rickshaw")
	public String rickshaw(){
		return "rickshaw";
	}

	@RequestMapping("/classification/{classificationKey}/unique_values")
	public @ResponseBody String getValuesPerClassificationKey(
			@PathVariable String classificationKey,
			@RequestParam(value = "start", defaultValue = "") String startParam,
			@RequestParam(value = "end", defaultValue = "") String endParam,
			@RequestParam(value = "reverse", defaultValue = "false") String reverseParam,
			@RequestParam(value = "limit", defaultValue = "1000") String limitParam
		){
		GenericConversionService service = new DefaultConversionService();
		Long start = service.convert(startParam, Long.class);
		Long end = service.convert(endParam, Long.class);
		Boolean reverse = service.convert(reverseParam, Boolean.class);
		Long limit = service.convert(limitParam, Long.class);

		Session session = getCassandraConnection();
		Select query = QueryBuilder
			.select("classification_value")
			.from("event_counters_by_classification_key")
			.where(QueryBuilder.eq("period_type_name", "year"))
				.and(QueryBuilder.eq("classification_key", classificationKey))
			.orderBy((!reverse ? QueryBuilder.asc("period_start") : QueryBuilder.desc("period_start")));

		if(start != null)
			query.where(QueryBuilder.gt("period_start", start));

		if(end != null)
			query.where(QueryBuilder.lte("period_start", end));

		ResultSet result = session.execute(query);
		Set<String> set = new HashSet<String>();
		for (Row row : result) {
			set.add(row.getString(0));
		}
		JsonArray arr = new JsonArray();
		for (String string : set) {
			if(--limit < 0){
				break;
			}
			arr.add(new JsonPrimitive(string));
		}
		return arr.toString();
	}
	
	private Session getCassandraConnection(){
		Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
		return cluster.connect("cea");
	}
}
