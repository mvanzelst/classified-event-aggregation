package org.classified_event_aggregation.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.google.gson.JsonObject;

@Entity
@Table(
	name="threshold", 
	uniqueConstraints = {
		@UniqueConstraint(
				columnNames={"dimensionlessStatisticType", "applicationName", "sequenceName"}
		)
	}
)
public class Threshold {

	@Id
	@GeneratedValue
	private Long id;
	
	@Size(max = 255)
	private DimensionlessStatisticType dimensionlessStatisticType;
	
	@Size(max = 255)
	private String applicationName;
	
	@Size(max = 255)
	private String sequenceName;
	
	private Double thresholdValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DimensionlessStatisticType getDimensionlessStatisticType() {
		return dimensionlessStatisticType;
	}

	public void setDimensionlessStatisticType(
			DimensionlessStatisticType dimensionlessStatisticType) {
		this.dimensionlessStatisticType = dimensionlessStatisticType;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public Double getThresholdValue() {
		return thresholdValue;
	}

	public void setThresholdValue(Double thresholdValue) {
		this.thresholdValue = thresholdValue;
	}
	
	public JsonObject toJson(){
		JsonObject output = new JsonObject();
		
		// For a new object this field can be empty 
		if(id != null)
			output.addProperty("id", id);
		
		output.addProperty("dimensionlessStatisticType", dimensionlessStatisticType.name());
		output.addProperty("applicationName", applicationName);
		output.addProperty("sequenceName", sequenceName);
		output.addProperty("thresholdValue", thresholdValue);
		return output;
	}
	
	public static Threshold fromJson(JsonObject job){
		Threshold output = new Threshold();

		// For a new object this field can be empty 
		if(job.has("id"))
			output.setId(job.getAsJsonPrimitive("id").getAsLong());

		output.setDimensionlessStatisticType(DimensionlessStatisticType.valueOf(job.getAsJsonPrimitive("dimensionlessStatisticType").getAsString()));
		output.setApplicationName(job.getAsJsonPrimitive("applicationName").getAsString());		
		output.setSequenceName(job.getAsJsonPrimitive("sequenceName").getAsString());
		output.setThresholdValue(job.getAsJsonPrimitive("thresholdValue").getAsDouble());
		return output;
	}
}
