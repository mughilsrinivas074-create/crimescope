package com.crimescope.domain.strategy;

import com.crimescope.domain.model.CrimeRecord;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete strategy calculating historical trajectory, yearly momentum,
 * and mathematical percentage changes with explicit zero-denominator protection.
 */
@Component("trendForecastingStrategy")
public class TrendForecastingStrategy implements CrimeAnalyticsStrategy {

    @Override
    public StrategyResult evaluate(List<CrimeRecord> records, Map<String, Object> parameters) {
        if (records == null || records.isEmpty()) {
            return new StrategyResult(getStrategyIdentifier(), "No historical records available for trend analysis.",
                    Map.of("dataPoints", 0), Map.of(), 0.0);
        }

        // Group by year
        Map<Integer, List<CrimeRecord>> byYear = records.stream()
                .filter(r -> r.getReportingYear() != null)
                .collect(Collectors.groupingBy(CrimeRecord::getReportingYear, TreeMap::new, Collectors.toList()));

        List<Map<String, Object>> yearlyTrends = new ArrayList<>();
        Long previousYearCases = null;
        double totalTrajectorySlope = 0.0;
        int trendSteps = 0;

        for (Map.Entry<Integer, List<CrimeRecord>> entry : byYear.entrySet()) {
            int year = entry.getKey();
            List<CrimeRecord> yRecords = entry.getValue();
            long totalCases = yRecords.stream().mapToLong(r -> r.getCaseCount() != null ? r.getCaseCount() : 0).sum();
            long arrests = yRecords.stream().mapToLong(r -> r.getArrestCount() != null ? r.getArrestCount() : 0).sum();

            Double yoyChangePct = null;
            if (previousYearCases != null) {
                if (previousYearCases > 0) {
                    yoyChangePct = ((double) (totalCases - previousYearCases) / previousYearCases) * 100.0;
                    totalTrajectorySlope += yoyChangePct;
                    trendSteps++;
                } else if (totalCases > 0) {
                    yoyChangePct = 100.0; // Baseline transition from 0 to positive
                } else {
                    yoyChangePct = 0.0;
                }
            }

            Map<String, Object> yearPoint = new LinkedHashMap<>();
            yearPoint.put("year", year);
            yearPoint.put("totalCases", totalCases);
            yearPoint.put("totalArrests", arrests);
            yearPoint.put("yoyChangePct", yoyChangePct != null ? Math.round(yoyChangePct * 10.0) / 10.0 : null);
            yearPoint.put("recordCount", yRecords.size());

            yearlyTrends.add(yearPoint);
            previousYearCases = totalCases;
        }

        double averageAnnualGrowth = trendSteps > 0 ? (totalTrajectorySlope / trendSteps) : 0.0;

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("startYear", byYear.keySet().stream().findFirst().orElse(null));
        metrics.put("endYear", byYear.keySet().stream().reduce((first, second) -> second).orElse(null));
        metrics.put("totalObservedYears", byYear.size());
        metrics.put("averageAnnualGrowthPct", Math.round(averageAnnualGrowth * 10.0) / 10.0);

        Map<String, Object> vizData = new LinkedHashMap<>();
        vizData.put("yearlyTrends", yearlyTrends);

        String trajectoryLabel = averageAnnualGrowth > 5.0 ? "Upward Shift" :
                averageAnnualGrowth < -5.0 ? "Decreasing Trend" : "Stable Trajectory";

        String summary = String.format("Historical trajectory spanning %d years shows an average annual change of %.1f%% (%s).",
                byYear.size(), averageAnnualGrowth, trajectoryLabel);

        return new StrategyResult(getStrategyIdentifier(), summary, metrics, vizData, averageAnnualGrowth);
    }

    @Override
    public String getStrategyIdentifier() {
        return "TREND_FORECASTING";
    }
}
