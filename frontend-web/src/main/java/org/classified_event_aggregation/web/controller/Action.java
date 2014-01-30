package org.classified_event_aggregation.web.controller;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.gson.Gson;
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


	@RequestMapping("/classification/{classificationKey}")
	public @ResponseBody String getValuesPerClassificationKey(@PathVariable String classificationKey){
		Session session = getCassandraConnection();
		PreparedStatement stmt = session.prepare(
			"SELECT classification_value " +
			"FROM event_counters_by_classification_key " +
			"WHERE " + 
				"period_type_name = 'year' AND " + 
				"classification_key = ?;"
		);
		ResultSet result = session.execute(
			stmt.bind(classificationKey)
		);
		
		Set<String> set = new HashSet<String>();
		for (Row row : result) {
			set.add(row.getString(0));
		}
		JsonArray arr = new JsonArray();
		for (String string : set) {
			arr.add(new JsonPrimitive(string));
		}
		return arr.toString();
	}
	
	private Session getCassandraConnection(){
		Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
		return cluster.connect("cea");
	}
}
