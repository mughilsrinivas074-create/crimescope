package com.crimescope.domain.strategy;

import com.crimescope.domain.model.CrimeRecord;
import java.util.List;
import java.util.Map;

/**
 * Strategy interface demonstrating Abstraction and Polymorphism in OOP.
 * Different analytical algorithms implement this interface interchangeably.
 */
public interface CrimeAnalyticsStrategy {

    /**
     * Executes the analytical algorithm against a bounded collection of CrimeRecord entities.
     *
     * @param records List of crime records retrieved from the database
     * @param parameters Contextual parameters (e.g. selected years, base district)
     * @return Formatted strategy result containing numerical metrics and visualization datasets
     */
    StrategyResult evaluate(List<CrimeRecord> records, Map<String, Object> parameters);

    /**
     * Returns the unique identifier of the strategy.
     */
    String getStrategyIdentifier();
}
