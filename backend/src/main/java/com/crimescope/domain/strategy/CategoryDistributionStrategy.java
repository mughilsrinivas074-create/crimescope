package com.crimescope.domain.strategy;

import com.crimescope.domain.enums.SeverityLevel;
import com.crimescope.domain.model.CrimeRecord;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy calculating category breakdown, percentage shares,
 * and severity-weighted threat indices across Tamil Nadu jurisdictions.
 */
@Component("categoryDistributionStrategy")
public class CategoryDistributionStrategy implements CrimeAnalyticsStrategy {

    @Override
    public StrategyResult evaluate(List<CrimeRecord> records, Map<String, Object> parameters) {
        if (records == null || records.isEmpty()) {
            return new StrategyResult(getStrategyIdentifier(), "No records available for category distribution.",
                    Map.of("totalCases", 0), Map.of(), 0.0);
        }

        Map<String, List<CrimeRecord>> byCategory = records.stream()
                .filter(r -> r.getCategory() != null)
                .collect(Collectors.groupingBy(r -> r.getCategory().getName()));

        long grandTotalCases = records.stream().mapToLong(r -> r.getCaseCount() != null ? r.getCaseCount() : 0).sum();
        double severityWeightedScore = 0.0;
        List<Map<String, Object>> categoryList = new ArrayList<>();

        for (Map.Entry<String, List<CrimeRecord>> entry : byCategory.entrySet()) {
            String catName = entry.getKey();
            List<CrimeRecord> cRecords = entry.getValue();
            long cases = cRecords.stream().mapToLong(r -> r.getCaseCount() != null ? r.getCaseCount() : 0).sum();

            SeverityLevel severity = cRecords.getFirst().getCategory().getSeverityLevel();
            int weight = severity != null ? severity.getWeight() : 2;

            double proportion = grandTotalCases > 0 ? ((double) cases / grandTotalCases) * 100.0 : 0.0;
            severityWeightedScore += (cases * weight);

            Map<String, Object> cData = new LinkedHashMap<>();
            cData.put("categoryName", catName);
            cData.put("caseCount", cases);
            cData.put("sharePercentage", Math.round(proportion * 10.0) / 10.0);
            cData.put("severityLevel", severity != null ? severity.name() : "MEDIUM");
            cData.put("weight", weight);

            categoryList.add(cData);
        }

        categoryList.sort((a, b) -> Long.compare((Long) b.get("caseCount"), (Long) a.get("caseCount")));

        double severityIndex = grandTotalCases > 0 ? (severityWeightedScore / grandTotalCases) : 0.0;

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalCategoriesRepresented", byCategory.size());
        metrics.put("grandTotalCases", grandTotalCases);
        metrics.put("severityWeightedIndex", Math.round(severityIndex * 100.0) / 100.0);
        metrics.put("leadingCategory", categoryList.isEmpty() ? "None" : categoryList.getFirst().get("categoryName"));

        Map<String, Object> vizData = new LinkedHashMap<>();
        vizData.put("distribution", categoryList);

        String summary = String.format("Category distribution across %d distinct classifications with an aggregate Severity Index of %.2f/4.00.",
                byCategory.size(), severityIndex);

        return new StrategyResult(getStrategyIdentifier(), summary, metrics, vizData, severityIndex);
    }

    @Override
    public String getStrategyIdentifier() {
        return "CATEGORY_DISTRIBUTION";
    }
}
