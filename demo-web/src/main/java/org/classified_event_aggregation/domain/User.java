package org.classified_event_aggregation.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.google.gson.JsonObject;

public class User {
	
	private UUID uuid;
	private String userName;
	private String firstName;
	private String surName;
	private String address;
	private String country;
	private Date birthDay;

	public UUID getUuid() {
		return uuid;
	}

	public User(UUID uuid, String userName, String firstName, String surName,
			String address, String country, Date birthDay) {
		this.uuid = uuid;
		this.userName = userName;
		this.firstName = firstName;
		this.surName = surName;
		this.address = address;
		this.country = country;
		this.birthDay = birthDay;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(Date birthDay) {
		this.birthDay = birthDay;
	}

	public JsonObject toJSON(){
		JsonObject job = new JsonObject();
		job.addProperty("id", uuid.toString());
		job.addProperty("userName", userName);
		job.addProperty("firstName", firstName);
		job.addProperty("surName", surName);
		job.addProperty("address", address);
		job.addProperty("country", country);
		job.addProperty("birthDay", new SimpleDateFormat("yyyy-MM-dd").format(birthDay));
		return job;
	}
}
