package org.classified_event_aggregation.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.classified_event_aggregation.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UserRepository {

	private final static Map<UUID, User> users = new HashMap<UUID, User>();

	static {
		// TODO fill with some real users
		List<User> userList = Arrays.asList(new User[]{
			new User(UUID.randomUUID(), "User1", "firstName1", "surName1", "address1", "country1", new Date()),
			new User(UUID.randomUUID(), "User2", "firstName2", "surName2", "address2", "country2", new Date()),
			new User(UUID.randomUUID(), "User3", "firstName3", "surName3", "address3", "country3", new Date()),
			new User(UUID.randomUUID(), "User4", "firstName4", "surName4", "address4", "country4", new Date()),
			new User(UUID.randomUUID(), "User5", "firstName5", "surName5", "address5", "country5", new Date()),
			new User(UUID.randomUUID(), "User6", "firstName6", "surName6", "address6", "country6", new Date()),
			new User(UUID.randomUUID(), "User7", "firstName7", "surName7", "address7", "country7", new Date())
		});
		for (User user : userList) {
			users.put(user.getUuid(), user);
		}
	}

	public User find(UUID uuid) {
		return users.get(uuid);
	}

	public Collection<User> list(){
		return users.values();
	}

}
