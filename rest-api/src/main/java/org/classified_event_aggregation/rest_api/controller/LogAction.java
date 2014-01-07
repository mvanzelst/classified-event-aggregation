package org.classified_event_aggregation.rest_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LogAction {

	@RequestMapping("/hello")
	public @ResponseBody String helloWorld() {
		return "helloWorld";
	}

}
