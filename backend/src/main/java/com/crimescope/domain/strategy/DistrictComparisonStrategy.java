package com.crimescope.domain.strategy;

import com.crimescope.domain.model.CrimeRecord;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete strategy for comparing crime metrics across multiple Tamil Nadu districts.
 * Demonstrates mathematical safety: handles zero denominators, prevents raw count fallacies.
 */
@Component("districtComparisonStrategy")
public class DistrictComparisonStrategy implements CrimeAnalyticsStrategy {

    @Override
    public StrategyResult evaluate(List<CrimeRecord> records, Map<String, Object> parameters) {
        if (records == null || records.isEmpty()) {
            return new StrategyResult(getStrategyIdentifier(), "No records available for district comparison.",
                    Map.of("totalRecords", 0), Map.of(), 0.0);
        }

        // Group records by District Name
        Map<String, List<CrimeRecord>> byDistrict = records.stream()
                .filter(r -> r.getDistrict() != null)
                .collect(Collectors.groupingBy(r -> r.getDistrict().getName()));

        Map<String, Object> districtMetrics = new LinkedHashMap<>();
        List<Map<String, Object>> comparisonTable = new ArrayList<>();
        long totalOverallCases = 0;

        for (Map.Entry<String, List<CrimeRecord>> entry : byDistrict.entrySet()) {
            String districtName = entry.getKey();
            List<CrimeRecord> districtRecords = entry.getValue();

            long totalCases = districtRecords.stream().mapToLong(r -> r.getCaseCount() != null ? r.getCaseCount() : 0).sum();
            long totalArrests = districtRecords.stream().mapToLong(r -> r.getArrestCount() != null ? r.getArrestCount() : 0).sum();
            long totalConvictions = districtRecords.stream().mapToLong(r -> r.getConvictedCount() != null ? r.getConvictedCount() : 0).sum();
            long totalChargesheeted = districtRecords.stream().mapToLong(r -> r.getChargesheetedCount() != null ? r.getChargesheetedCount() : 0).sum();

            totalOverallCases += totalCases;

            // Zero denominator safety
            Double clearanceRate = totalCases > 0 ? Math.min(100.0, ((double) totalChargesheeted / totalCases) * 100.0) : null;
            Double convictionRate = totalCases > 0 ? Math.min(100.0, ((double) totalConvictions / totalCases) * 100.0) : null;

            Map<String, Object> dData = new LinkedHashMap<>();
            dData.put("districtName", districtName);
            dData.put("totalCases", totalCases);
            dData.put("totalArrests", totalArrests);
            dData.put("totalConvictions", totalConvictions);
            dData.put("clearanceRatePct", clearanceRate != null ? Math.round(clearanceRate * 10.0) / 10.0 : 0.0);
            dData.put("convictionRatePct", convictionRate != null ? Math.round(convictionRate * 10.0) / 10.0 : 0.0);
            dData.put("recordCount", districtRecords.size());

            comparisonTable.add(dData);
            districtMetrics.put(districtName, dData);
        }

        comparisonTable.sort((a, b) -> Long.compare((Long) b.get("totalCases"), (Long) a.get("totalCases")));

        Map<String, Object> numericalMetrics = new LinkedHashMap<>();
        numericalMetrics.put("districtCount", byDistrict.size());
        numericalMetrics.put("totalOverallCases", totalOverallCases);
        numericalMetrics.put("averageCasesPerDistrict", byDistrict.isEmpty() ? 0 : Math.round((double) totalOverallCases / byDistrict.size()));

        Map<String, Object> vizData = new LinkedHashMap<>();
        vizData.put("comparisonTable", comparisonTable);

        String summary = String.format("Comparative analysis of %d Tamil Nadu districts encompassing %d registered cases.",
                byDistrict.size(), totalOverallCases);

        return new StrategyResult(getStrategyIdentifier(), summary, numericalMetrics, vizData, (double) totalOverallCases);
    }

    @Override
    public String getStrategyIdentifier() {
        return "DISTRICT_COMPARISON";
    }
}
