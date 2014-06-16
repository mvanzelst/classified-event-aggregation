package org.classified_event_aggregation.web.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.classified_event_aggregation.domain.LogMessage;
import org.classified_event_aggregation.domain.LogSequence;
import org.classified_event_aggregation.domain.User;
import org.classified_event_aggregation.repository.UserRepository;
import org.classified_event_aggregation.service.LogSequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Action {

	private static long delay = 800L;
	private static boolean throwException = false;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private LogSequenceService logSequenceService;

	@RequestMapping("/")
	public String index(Model model){
		Collection<User> users = userRepository.list();
		Map<String, String> userNameByUuid = new HashMap<String, String>();
		for (User user : users) {
			userNameByUuid.put(user.getUuid().toString(), user.getUserName());
		}
		model.addAttribute("userNameByUuid", userNameByUuid);
		return "main";
	}

	@RequestMapping(value="/rest/user/{uuid}", produces="application/json")
	public @ResponseBody String monitorApplication(@PathVariable UUID uuid){
		List<LogMessage> logMessages = new ArrayList<LogMessage>();
		String sequenceId = UUID.randomUUID().toString();
		// Log start of action
		logMessages.add(new LogMessage("demo-web", "Starting sequence #SEQUENCE_NAME:GET:/rest/user/{uuid} #LOG_LEVEL:INFO #SEQUENCE_STATUS:STARTED #SEQUENCE_ID:" + sequenceId, System.currentTimeMillis()));
		try { 
			try {
				long randomSleep = (long) ((Math.abs(new Random().nextGaussian()) + 1) * 0.9 * delay);
				System.out.println(randomSleep);
				Thread.sleep(randomSleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			User user = userRepository.find(uuid);
			if(user == null || throwException){
				DataAccessException e = new DataAccessException("Failed to load user") {};
				logMessages.add(new LogMessage("demo-web", ExceptionUtils.getStackTrace(e) + " #LOG_LEVEL:ERROR #SEQUENCE_ID:" + sequenceId, System.currentTimeMillis()));
				throw e;
			}
			return user.toJSON().toString();

		} finally {
			// Log end of action
			logMessages.add(new LogMessage("demo-web", "Finished sequence #LOG_LEVEL:INFO #SEQUENCE_STATUS:FINISHED #SEQUENCE_ID:" + sequenceId, System.currentTimeMillis()));
			logSequenceService.store(logMessages);
		}
	}
	
	@RequestMapping(value="/rest/delay/{delay}")
	public @ResponseBody void setDelay(@PathVariable long delay){
		Action.delay = delay;
	}
	
	@RequestMapping(value="/rest/exception/{throwException}")
	public @ResponseBody void setThrowException(@PathVariable boolean throwException){
		Action.throwException = throwException;
	}

}
