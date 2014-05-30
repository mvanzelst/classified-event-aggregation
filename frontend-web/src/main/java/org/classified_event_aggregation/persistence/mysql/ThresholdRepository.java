package org.classified_event_aggregation.persistence.mysql;

import org.classified_event_aggregation.domain.DimensionlessStatisticType;
import org.classified_event_aggregation.domain.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdRepository extends JpaRepository<Threshold, Long>  {

	Threshold findByApplicationNameAndSequenceNameAndDimensionlessStatisticType(String applicationName, String sequenceName, DimensionlessStatisticType dimensionlessStatisticType);
}
