package org.classified_event_aggregation.persistence.mysql;

import org.classified_event_aggregation.domain.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdRepository extends JpaRepository<Threshold, Long>  {

	Threshold findByApplicationNameAndSequenceNameAndAlgorithmName(String applicationName, String sequenceName, String algorithmName);
}
