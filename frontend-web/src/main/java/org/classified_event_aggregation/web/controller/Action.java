package org.classified_event_aggregation.web.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Action {

	@RequestMapping("/hello")
	public @ResponseBody String helloWorld() {
		return "helloWorld";
	}

}
